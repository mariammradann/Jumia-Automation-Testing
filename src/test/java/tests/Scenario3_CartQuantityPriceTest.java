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

import java.util.List;

@Epic("Jumia Egypt Automation")
@Feature("Cart Calculations")
public class Scenario3_CartQuantityPriceTest extends BaseTest {

    @Story("Scenario 3: Cart Quantity and Price Calculation Validation")
    @Description("Validates unit price recording, incrementing quantity (1->2->3) with subtotal recalculation, and decrementing back to 1.")
    @Test(dataProvider = "scenario3Data", dataProviderClass = DataProviderTest.class)
    public void testCartQuantityAndPriceCalculation(String searchQuery, int q1, int q2, int q3, int finalQty) {
        log.info("Starting Scenario 3: Quantity cycle [{} -> {} -> {} -> {}] for '{}'", q1, q2, q3, finalQty, searchQuery);

        HomePage homePage = new HomePage(driver).initialize();
        SearchResultsPage searchPage = homePage.searchFor(searchQuery);

        // 1. Add product to cart
        ProductDetailsPage pdp = searchPage.clickProductAtIndex(0);
        CartPage cartPage = pdp.addToCartAndGoToCart();

        // 2. Record unit price
        List<CartItem> items = cartPage.getCartItems();
        Assert.assertFalse(items.isEmpty(), "Cart is empty!");
        double unitPrice = items.get(0).getUnitPrice();
        log.info("Recorded Unit Price: {} EGP", unitPrice);
        Assert.assertTrue(unitPrice > 0, "Unit price must be positive!");

        // 3 & 4. Increase quantity from 1 -> 2 -> 3 and verify subtotal after each change
        int[] quantitiesToTest = new int[]{q2, q3};
        for (int qty : quantitiesToTest) {
            cartPage.updateItemQuantity(0, qty);
            double expectedSubtotal = unitPrice * qty;
            double actualTotal = cartPage.getTotalPrice();
            for (int t = 0; t < 3 && actualTotal < (expectedSubtotal * 0.95); t++) {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                actualTotal = cartPage.getTotalPrice();
            }
            log.info("Qty: {} | Expected Subtotal: {} | Actual Total: {}", qty, expectedSubtotal, actualTotal);
            softAssert.assertTrue(actualTotal >= (expectedSubtotal * 0.95),
                    "Subtotal mismatch for quantity " + qty);
        }

        // 5. Decrease quantity back to 1
        cartPage.updateItemQuantity(0, finalQty);
        double resetTotal = cartPage.getTotalPrice();
        for (int t = 0; t < 3 && resetTotal > (unitPrice * 1.15); t++) {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            resetTotal = cartPage.getTotalPrice();
        }
        log.info("Decreased to 1 | Final Total: {} (Expected approx: {})", resetTotal, unitPrice);
        softAssert.assertTrue(resetTotal >= (unitPrice * 0.95) && resetTotal <= (unitPrice * 1.15),
                "Total did not recalculate correctly after decreasing quantity to 1");

        softAssert.assertAll();
        log.info("Scenario 3 completed successfully");
    }
}

