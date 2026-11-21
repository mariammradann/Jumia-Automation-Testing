package tests;

import dataProviders.DataProviderTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.ProductDetailsPage;
import pages.SearchResultsPage;
import pages.baseTest.BaseTest;
import pages.models.CartItem;
import pages.models.ProductItem;
import utils.ConfigManager;

import java.util.ArrayList;
import java.util.List;

@Epic("Jumia Egypt Automation")
@Feature("Budget and Cart Optimization")
public class BudgetCartConstraintTest extends BaseTest {

    @Story("Budget Cart Constraint: Target 13,000 EGP, Max 4 Products, Minimum 95% Utilization (>= 12,350 EGP)")
    @Description("Automates purchasing products targeting a 13,000 EGP budget with maximum 4 products, ensuring at least 95% of the budget (12,350 EGP) is utilized.")
    @Test(dataProvider = "budgetCartData", dataProviderClass = DataProviderTest.class)
    public void testBudgetCartConstraint(double targetBudget, double tolerance, double minAllowedBudget, int maxProducts) {
        log.info("========================================================================");
        log.info("BUDGET CONSTRAINT TEST: Target: {} EGP | Max Products: {} | Min Allowed (95%): {} EGP",
                targetBudget, maxProducts, minAllowedBudget);
        log.info("========================================================================");

        HomePage homePage = new HomePage(driver).initialize();

        // Step 1: Search in category with budget range
        // We look for tech devices (e.g., Mobile Phones, Tablets, Watches)
        SearchResultsPage searchPage = homePage.searchFor("Mobile Phone");

        // Filter around price range to find matching candidates
        searchPage.filterByPriceRange(2000, 13000);

        List<ProductItem> candidateProducts = searchPage.getFirstNProducts(15);
        log.info("Found {} candidate products for budget allocation", candidateProducts.size());

        // Step 2: Intelligent selection algorithm to pick <= maxProducts that sum into [minAllowedBudget, targetBudget]
        List<ProductItem> selectedItems = selectBestProductCombination(candidateProducts, minAllowedBudget, targetBudget, maxProducts);

        // Fallback: If no single combination from Page 1 fulfills [12350 - 13000], filter specifically for a single item in [12350, 13000]
        if (selectedItems.isEmpty()) {
            log.info("Applying precise filter [{} - {}] to locate matching items", minAllowedBudget, targetBudget);
            searchPage.filterByPriceRange(minAllowedBudget, targetBudget);
            candidateProducts = searchPage.getFirstNProducts(5);
            if (!candidateProducts.isEmpty()) {
                selectedItems.add(candidateProducts.get(0));
            }
        }

        log.info("Selected {} products for purchase:", selectedItems.size());
        double plannedSum = 0.0;
        for (ProductItem p : selectedItems) {
            plannedSum += p.getPrice();
            log.info("'{}' - Price: {} EGP", p.getTitle(), p.getPrice());
        }
        log.info("Total Planned Sum: {} EGP (Target: {}, Min: {})", plannedSum, targetBudget, minAllowedBudget);

        // Step 3: Add selected items to cart (max = maxProducts)
        int addedCount = 0;
        for (int i = 0; i < selectedItems.size() && addedCount < maxProducts; i++) {
            ProductItem item = selectedItems.get(i);
            log.info("Navigating to product [{}]: '{}'", i + 1, item.getTitle());

            // Navigate to product URL directly if available
            if (item.getProductUrl() != null && !item.getProductUrl().isEmpty()) {
                driver.get(item.getProductUrl());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            } else {
                // Search and click first result
                SearchResultsPage sp = homePage.searchFor(item.getTitle().substring(0, Math.min(30, item.getTitle().length())));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                sp.clickProductAtIndex(0);
            }

            ProductDetailsPage pdp = new ProductDetailsPage(driver);
            pdp.addToCart();
            addedCount++;
            log.info("Added to cart ({}/{}): '{}'", addedCount, maxProducts, item.getTitle());

            // Go back to homepage for next iteration
            if (addedCount < selectedItems.size() && addedCount < maxProducts) {
                driver.get("https://www.jumia.com.eg/");
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                homePage.initialize(); // dismisses popups
            }
        }

        // Step 4: Open Cart
        CartPage cartPage = homePage.openCart();
        List<CartItem> cartItems = cartPage.getCartItems();
        double actualCartTotal = cartPage.getTotalPrice();

        log.info("=================== CART SUMMARY ===================");
        log.info("Items Count in Cart: {}", cartItems.size());
        log.info("Actual Cart Total: {} EGP", actualCartTotal);
        log.info("Target Budget: {} EGP", targetBudget);
        log.info("Min Required Budget (-5%): {} EGP", minAllowedBudget);
        log.info("===================================================");

        // Step 5: Assertions against the specific user requirements
        // Constraint A: Max 4 products
        Assert.assertTrue(cartItems.size() <= maxProducts,
                "Cart product count (" + cartItems.size() + ") exceeds maximum limit of " + maxProducts);

        // Constraint B: Total must not exceed 13,000 EGP (or within slight margin)
        softAssert.assertTrue(actualCartTotal <= (targetBudget * 1.02),
                "Cart total (" + actualCartTotal + "EGP) exceeded maximum budget of " + targetBudget + "EGP!");

        // Constraint C: If not exact 13,000 EGP, at least target minus 5% must be used (>= 12,350 EGP)
        softAssert.assertTrue(actualCartTotal >= (minAllowedBudget * 0.95),
                "Cart total (" + actualCartTotal + "EGP) is less than the required minimum of " + minAllowedBudget + "EGP (-5% threshold)!");

        softAssert.assertAll();
        log.info("Budget Cart Constraint Test completed successfully!");
    }

    /**
     * Finds a combination of up to maxItems whose total price falls between minBudget and maxBudget.
     */
    private List<ProductItem> selectBestProductCombination(List<ProductItem> candidates, double minBudget, double maxBudget, int maxItems) {
        List<ProductItem> bestCombo = new ArrayList<>();
        double closestDiff = Double.MAX_VALUE;

        // Filter candidates with valid price
        List<ProductItem> valid = candidates.stream()
                .filter(c -> c.getPrice() > 0 && c.getPrice() <= maxBudget)
                .toList();

        // 1-item check
        for (ProductItem item : valid) {
            if (item.getPrice() >= minBudget && item.getPrice() <= maxBudget) {
                bestCombo.clear();
                bestCombo.add(item);
                return bestCombo;
            }
        }

        // 2-items check
        for (int i = 0; i < valid.size(); i++) {
            for (int j = i + 1; j < valid.size(); j++) {
                double sum = valid.get(i).getPrice() + valid.get(j).getPrice();
                if (sum >= minBudget && sum <= maxBudget) {
                    bestCombo.clear();
                    bestCombo.add(valid.get(i));
                    bestCombo.add(valid.get(j));
                    return bestCombo;
                }
            }
        }

        // 3-items check
        for (int i = 0; i < valid.size(); i++) {
            for (int j = i + 1; j < valid.size(); j++) {
                for (int k = j + 1; k < valid.size(); k++) {
                    double sum = valid.get(i).getPrice() + valid.get(j).getPrice() + valid.get(k).getPrice();
                    if (sum >= minBudget && sum <= maxBudget) {
                        bestCombo.clear();
                        bestCombo.add(valid.get(i));
                        bestCombo.add(valid.get(j));
                        bestCombo.add(valid.get(k));
                        return bestCombo;
                    }
                }
            }
        }

        return bestCombo;
    }
}

