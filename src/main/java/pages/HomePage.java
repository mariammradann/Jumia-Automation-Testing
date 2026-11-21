package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * HomePage represents the Jumia Egypt landing page and global header navigation.
 */
public class HomePage extends BasePage {

    // Locators
    private final By searchInput = By.cssSelector(
        "input#fi-q, " +
        "input[name='q'], " +
        "input[placeholder*='Search'], " +
        "input[placeholder*='search'], " +
        "input[aria-label*='Search'], " +
        "input[type='search'], " +
        ".header input[type='text'], " +
        "form[action*='catalog'] input[type='text']"
    );
    private final By searchButton = By.cssSelector("button._search, button[type='submit'], form button.btn._prim");
    private final By cartButton = By.cssSelector(
        "a[href*='/cart'], a[href='/cart/'], button[href*='/cart'], " +
        "a.-df.-i-ctr[href*='cart'], a[data-gtm-action='cart'], " +
        "svg[data-icon='cart'] ~ a, a[aria-label*='cart'], " +
        "a[title*='Cart'], a[role='link'][href*='cart']"
);
    private final By cartBadge = By.cssSelector("a[href*='/cart'] span.-b, a[href*='/cart'] .badge");
    private final By accountDropdown = By.cssSelector("label[for='dpdw-login'], .dpdw-login, a[href*='/customer/account']");
    private final By wishlistLink = By.cssSelector("a[href*='/customer/wishlist'], a[href*='wishlist']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Initializes homepage by dismissing popups and cookies.
     */
    public HomePage initialize() {
        dismissPopupsAndBanners();
        return this;
    }

    /**
     * Searches for a product by keyword and returns the SearchResultsPage.
     *
     * @param keyword search keyword
     * @return SearchResultsPage
     */
    public SearchResultsPage searchFor(String keyword) {
        log.info("Performing search for: '{}'", keyword);
        dismissPopupsAndBanners();

        boolean searchSuccess = false;
        try {
            List<WebElement> inputs = driver.findElements(searchInput);
            if (!inputs.isEmpty() && inputs.get(0).isDisplayed()) {
                WebElement input = inputs.get(0);
                input.clear();
                input.sendKeys(keyword);
                List<WebElement> buttons = driver.findElements(searchButton);
                if (!buttons.isEmpty() && buttons.get(0).isDisplayed()) {
                    buttons.get(0).click();
                } else {
                    input.sendKeys(Keys.ENTER);
                }
                sleep(1500);
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("catalog") || currentUrl.contains("q=")) {
                    searchSuccess = true;
                }
            }
        } catch (Exception e) {
            log.warn("Standard search attempt failed: {}", e.getMessage());
        }

        if (!searchSuccess) {
            log.info("Applying direct catalog search URL fallback for: '{}'", keyword);
            String encoded = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
            driver.get("https://www.jumia.com.eg/catalog/?q=" + encoded);
            sleep(1500);
        }

        dismissPopupsAndBanners();
        return new SearchResultsPage(driver);
    }

    /**
     * Opens the cart page.
     *
     * @return CartPage
     */
    public CartPage openCart() {
        log.info("Navigating to Cart");
        dismissPopupsAndBanners();
        // Try to click the cart link/button in the header
        try {
            List<WebElement> carts = driver.findElements(cartButton);
            for (WebElement el : carts) {
                if (el.isDisplayed() && el.isEnabled()) {
                    jsClick(el);
                    sleep(2000);
                    if (driver.getCurrentUrl().contains("/cart")) {
                        return new CartPage(driver);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Cart button click failed: {}", e.getMessage());
        }
        // Reliable fallback: navigate directly to cart URL
        log.info("Navigating directly to cart URL as fallback");
        driver.get("https://www.jumia.com.eg/cart/");
        sleep(2000);
        dismissPopupsAndBanners();
        return new CartPage(driver);
    }

    /**
     * Returns current cart count badge value.
     */
    public int getCartBadgeCount() {
        try {
            String badgeText = getText(cartBadge);
            if (!badgeText.isEmpty()) {
                return Integer.parseInt(badgeText.replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {}
        return 0;
    }
}

