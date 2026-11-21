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
import pages.WishlistModalPage;
import pages.baseTest.BaseTest;

@Epic("Jumia Egypt Automation")
@Feature("Wishlist Validation")
public class Scenario7_WishlistGuestTest extends BaseTest {

    @Story("Scenario 7: Wishlist Validation (Guest User)")
    @Description("Opens product pages as guest, clicks wishlist button, verifies authentication prompt, and ensures product details remain visible.")
    @Test(dataProvider = "scenario7Data", dataProviderClass = DataProviderTest.class)
    public void testGuestWishlistValidation(String[] productKeywords) {
        log.info("Starting Scenario 7: Wishlist guest validation for {} products", productKeywords.length);

        HomePage homePage = new HomePage(driver).initialize();

        for (int i = 0; i < productKeywords.length; i++) {
            if (i > 0) {
                driver.get("https://www.jumia.com.eg/");
                homePage.initialize();
            }
            String keyword = productKeywords[i];
            log.info("Product [{}]: Searching for '{}'", i + 1, keyword);

            SearchResultsPage searchPage = homePage.searchFor(keyword);
            ProductDetailsPage pdp = searchPage.clickProductAtIndex(0);

            // 1. Open a product page
            String productTitle = pdp.getProductTitle();
            log.info("Product opened: '{}'", productTitle);
            Assert.assertFalse(productTitle.isEmpty(), "Product title should be visible");

            // 2. Click Add to Wishlist
            WishlistModalPage wishlistPage = pdp.addToWishlist();

            // 3. Verify login prompt appears
            boolean isLoginPromptShown = wishlistPage.isLoginPromptDisplayed();
            log.info("Guest wishlist login prompt displayed: {}", isLoginPromptShown);
            softAssert.assertTrue(isLoginPromptShown, "Login prompt or redirect must appear when guest clicks wishlist!");

            // 4. Verify product details remain visible (or if redirected, verify page state)
            boolean detailsVisible = wishlistPage.isProductDetailsStillVisible()
                    || driver.getCurrentUrl().contains("login")
                    || driver.getCurrentUrl().contains("my.jumia.com")
                    || driver.getCurrentUrl().contains("identification");
            softAssert.assertTrue(detailsVisible, "Product context was lost after wishlist click!");

            // Dismiss modal or navigate back for next iteration
            wishlistPage.dismissLoginModal();
        }

        softAssert.assertAll();
        log.info("Scenario 7 completed successfully");
    }
}

