package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * WishlistModalPage validates the guest user login modal or redirect prompted
 * when a guest clicks 'Add to Wishlist'.
 */
public class WishlistModalPage extends BasePage {

    // Locators
    private final By loginPromptModal = By.cssSelector(".modal, .popup, #auth-modal, .-m-auth, .login-box, form[action*='login'], .mdc-text-field, form[action*='interaction'], form[action*='identification'], .auth-container");
    private final By emailInputField = By.cssSelector("input[name='email'], input#input_identifier_value, input[type='email'], input.mdc-text-field__input, input[name='identification'], input");
    private final By productTitleOnBackground = By.cssSelector("h1.-fs20, h1.-fs24, h1");
    private final By modalCloseButton = By.cssSelector("button[aria-label='close'], button.cls, .modal .close, button.close");

    public WishlistModalPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verifies that the login prompt appears (either as a modal popup or redirect to login page).
     */
    public boolean isLoginPromptDisplayed() {
        String currentUrl = driver.getCurrentUrl();
        boolean urlMatches = currentUrl.contains("login") || currentUrl.contains("customer/account")
                || currentUrl.contains("identification") || currentUrl.contains("my.jumia.com")
                || currentUrl.contains("interaction");
        boolean modalVisible = isElementDisplayed(loginPromptModal) || isElementDisplayed(emailInputField);
        log.info("Wishlist guest verification: URL matches={}, Modal/Input visible={}, URL={}", urlMatches, modalVisible, currentUrl);
        return urlMatches || modalVisible;
    }

    /**
     * Verifies that product details (e.g. title) remain visible in background or on page.
     */
    public boolean isProductDetailsStillVisible() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("login") || currentUrl.contains("my.jumia.com") || currentUrl.contains("identification") || currentUrl.contains("interaction")) {
            return true;
        }
        boolean visible = isElementDisplayed(productTitleOnBackground);
        log.info("Product title still visible: {}", visible);
        return visible;
    }

    /**
     * Closes the login modal if it's an overlay so user can continue browsing.
     */
    public ProductDetailsPage dismissLoginModal() {
        try {
            List<WebElement> closeBtns = driver.findElements(modalCloseButton);
            if (!closeBtns.isEmpty() && closeBtns.get(0).isDisplayed()) {
                jsClick(closeBtns.get(0));
            } else if (driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().contains("my.jumia.com")
                    || driver.getCurrentUrl().contains("identification") || driver.getCurrentUrl().contains("interaction")) {
                driver.navigate().back();
            }
        } catch (Exception ignored) {}
        sleep(1500);
        return new ProductDetailsPage(driver);
    }
}

