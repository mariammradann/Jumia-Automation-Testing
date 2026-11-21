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
@Feature("Cart Operations")
public class Scenario4_MixedCartOperationsTest extends BaseTest {

    @Story("Scenario 4: Mixed Cart Operations with Multiple Products")
    @Description("Adds multiple products, updates quantity, removes an item, adds recommendation, validates totals and refresh persistence.")
    @Test(dataProvider = "scenario4Data", dataProviderClass = DataProviderTest.class)
    public void testMixedCartOperations(String[] productsToAdd, int updatedMobileQty, String itemToRemove) {
        log.info("Starting Scenario 4: Mixed Cart Operations with {}", (Object) productsToAdd);

        HomePage homePage = new HomePage(driver).initialize();

        // 1. Add Mobile Phone, Headphones, and Power Bank to cart
        for (String productKeyword : productsToAdd) {
            SearchResultsPage searchPage = homePage.searchFor(productKeyword);
            ProductDetailsPage pdp = searchPage.clickProductByTitleKeyword(productKeyword);
            pdp.addToCart();
            log.info("Added '{}' to cart", productKeyword);
        }

        // Open cart
        CartPage cartPage = homePage.openCart();
        List<CartItem> initialItems = cartPage.getCartItems();
        log.info("Cart loaded with {} items", initialItems.size());
        Assert.assertTrue(initialItems.size() >= productsToAdd.length, "Not all products were added to cart!");

        // 2. Increase Mobile Phone quantity to 2
        // Assuming Mobile Phone is at index 0 (first added)
        cartPage.updateItemQuantity(0, updatedMobileQty);
        log.info("Updated quantity of item [0] to {}", updatedMobileQty);

        // 3. Remove Headphones (index 1)
        cartPage.removeItem(1);
        log.info("Removed '{}' from cart", itemToRemove);

        // 4. Add another item from recommendations
        cartPage.addRecommendedProductToCart(0);

        // 5. Verify quantities and total calculations
        double currentTotal = cartPage.getTotalPrice();
        log.info("Current Total after modifications: {} EGP", currentTotal);
        Assert.assertTrue(currentTotal > 0, "Cart total must be greater than zero!");

        // 6. Refresh page and verify persistence
        cartPage.refreshCart();
        double persistedTotal = cartPage.getTotalPrice();
        List<CartItem> persistedItems = cartPage.getCartItems();
        log.info("Persisted Total after refresh: {} EGP (Items: {})", persistedTotal, persistedItems.size());
        Assert.assertEquals(persistedTotal, currentTotal, "Cart total changed after refresh!");

        log.info("Scenario 4 completed successfully");
    }
}

