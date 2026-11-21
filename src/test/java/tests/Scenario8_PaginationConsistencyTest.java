package tests;

import dataProviders.DataProviderTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductDetailsPage;
import pages.SearchResultsPage;
import pages.baseTest.BaseTest;
import pages.models.ProductItem;

import java.util.List;

@Epic("Jumia Egypt Automation")
@Feature("Pagination and Consistency")
public class Scenario8_PaginationConsistencyTest extends BaseTest {

        @Story("Scenario 8: Pagination and Product Consistency Validation")
        @Description("Validates state persistence across pagination traversal (Page 1 -> 2 -> 3 -> 1) and verifies first product details page.")
        @Test(dataProvider = "scenario8Data", dataProviderClass = DataProviderTest.class)
        public void testPaginationAndProductConsistency(String searchQuery, int[] pages) {
                log.info("Starting Scenario 8: Pagination consistency for '{}' across pages {}", searchQuery, pages);

                HomePage homePage = new HomePage(driver).initialize();

                // 1. Search for Laptop
                SearchResultsPage searchPage = homePage.searchFor(searchQuery);

                // 2. Store first product from page 1
                List<ProductItem> page1Products = searchPage.getFirstNProducts(1);
                Assert.assertFalse(page1Products.isEmpty(), "No products found on page 1!");
                ProductItem firstProductInitial = page1Products.get(0);
                log.info("Stored Page 1 first product: '{}' ({} EGP)",
                                firstProductInitial.getTitle(), firstProductInitial.getPrice());

                // 3. Navigate to pages 2 and 3
                searchPage.goToPage(2);
                Assert.assertEquals(searchPage.getCurrentPageNumber(), 2, "Failed to navigate to page 2");

                searchPage.goToPage(3);
                Assert.assertEquals(searchPage.getCurrentPageNumber(), 3, "Failed to navigate to page 3");

                // 4. Return to page 1
                searchPage.goToPage(1);
                Assert.assertEquals(searchPage.getCurrentPageNumber(), 1, "Failed to return to page 1");

                // 5. Verify product ordering remains unchanged
                List<ProductItem> page1ReturnedProducts = searchPage.getFirstNProducts(1);
                Assert.assertFalse(page1ReturnedProducts.isEmpty(), "No products found after returning to page 1!");
                ProductItem firstProductReturned = page1ReturnedProducts.get(0);
                log.info("Page 1 first product upon return: '{}' ({} EGP)",
                                firstProductReturned.getTitle(), firstProductReturned.getPrice());

                Assert.assertEquals(firstProductReturned.getTitle(), firstProductInitial.getTitle(),
                                "First product ordering changed after returning to page 1!");

                // 6. Open stored product and verify details page loads successfully
                ProductDetailsPage pdp = searchPage.clickProductAtIndex(0);
                String pdpTitle = pdp.getProductTitle();
                double pdpPrice = pdp.getProductPrice();
                log.info("PDP Loaded: '{}' | Price: {} EGP", pdpTitle, pdpPrice);

                boolean titleMatch = pdpTitle.toLowerCase()
                                .contains(firstProductInitial.getTitle().toLowerCase().substring(0,
                                                Math.min(10, firstProductInitial.getTitle().length())))
                                || firstProductInitial.getTitle().toLowerCase().contains(
                                                pdpTitle.toLowerCase().substring(0, Math.min(10, pdpTitle.length())))
                                || Math.abs(pdpPrice - firstProductInitial.getPrice()) < 50.0;
                Assert.assertTrue(titleMatch,
                                "Opened PDP product title ('" + pdpTitle + "') does not match stored product ('"
                                                + firstProductInitial.getTitle() + "')!");

                log.info("Scenario 8 completed successfully");
        }
}
