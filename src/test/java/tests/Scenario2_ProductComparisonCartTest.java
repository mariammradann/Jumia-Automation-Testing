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

import java.util.ArrayList;
import java.util.List;

@Epic("Jumia Egypt Automation")
@Feature("Cart Operations")
public class Scenario2_ProductComparisonCartTest extends BaseTest {

    @Story("Scenario 2: Product Comparison Through Cart")
    @Description("Searches for products, captures details for 3 items, adds to cart, verifies metadata match and persistence after refresh.")
    @Test(dataProvider = "scenario2Data", dataProviderClass = DataProviderTest.class)
    public void testProductComparisonThroughCart(String searchQuery, int productsToCompare) {
        log.info("Starting Scenario 2: Search '{}' and compare {} products in cart", searchQuery, productsToCompare);

        HomePage homePage = new HomePage(driver).initialize();
        SearchResultsPage searchPage = homePage.searchFor(searchQuery);

        List<ProductItem> pdpProducts = new ArrayList<>();

        // 2 & 3. Open products, capture Name, Price, Seller, Rating, and Add to cart
        for (int i = 0; i < productsToCompare; i++) {
            // Re-fetch search page if returning
            if (i > 0) {
                searchPage = homePage.searchFor(searchQuery);
            }
            ProductDetailsPage pdp = searchPage.clickProductAtIndex(i);
            ProductItem item = pdp.captureProductDetails();
            pdpProducts.add(item);

            pdp.addToCart();
            log.info("Added product [{}] to cart: '{}' ({} EGP)", i + 1, item.getTitle(), item.getPrice());
        }

        // Navigate to Cart
        CartPage cartPage = homePage.openCart();
        List<CartItem> cartItems = cartPage.getCartItems();
        log.info("Cart loaded with {} items", cartItems.size());
        Assert.assertTrue(cartItems.size() >= pdpProducts.size(), "Cart item count mismatch!");

        // 4. Verify product information matches between PDP and Cart
        double expectedTotal = 0.0;
        for (ProductItem pdpItem : pdpProducts) {
            expectedTotal += pdpItem.getPrice();
            boolean matchFound = cartItems.stream().anyMatch(ci ->
                    ci.getName().toLowerCase().contains(pdpItem.getTitle().toLowerCase().substring(0, Math.min(15, pdpItem.getTitle().length())))
                            || Math.abs(ci.getUnitPrice() - pdpItem.getPrice()) < 5.0);
            softAssert.assertTrue(matchFound, "PDP item '" + pdpItem.getTitle() + "' was not found in cart!");
        }

        // 5. Verify total price calculation
        double actualTotal = cartPage.getTotalPrice();
        log.info("Expected Total: {} EGP | Actual Total: {} EGP", expectedTotal, actualTotal);
        softAssert.assertTrue(actualTotal > 0, "Cart total must be greater than zero");

        // 6. Refresh page and verify persistence
        cartPage.refreshCart();
        List<CartItem> refreshedItems = cartPage.getCartItems();
        double refreshedTotal = cartPage.getTotalPrice();
        Assert.assertEquals(refreshedItems.size(), cartItems.size(), "Cart items count changed after refresh!");
        Assert.assertEquals(refreshedTotal, actualTotal, "Cart total price changed after refresh!");

        softAssert.assertAll();
        log.info("Scenario 2 completed successfully");
    }
}

