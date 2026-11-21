package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * CheckoutLoginPage represents the login/identification prompt encountered by
 * guest users
 * when proceeding to checkout.
 */
public class CheckoutLoginPage extends BasePage {

    // Locators
    private final By emailOrPhoneInput = By.cssSelector(
            "input.mdc-text-field__input, input[name='email'], input#input_identifier_value, input[type='email'], input[name='identification'], form input[type='text'], input");
    private final By continueButton = By.cssSelector(
            "button[type='submit'], form button.btn._prim, button.btn-continue, button.mdc-button, button");
    private final By identificationHeader = By.xpath(
            "//h2[contains(., 'Welcome') or contains(., 'Identification') or contains(., 'Login') or contains(., 'تسجيل الدخول')] | //form[contains(@action, 'login')] | //*[contains(text(), 'Welcome to Jumia')]");
    private final By backToCartLink = By.cssSelector("a[href*='/cart'], a.-df[href*='cart']");

    public CheckoutLoginPage(WebDriver driver) {
        super(driver);
        dismissPopupsAndBanners();
    }

    /**
     * Verifies that the login / signup identification prompt is displayed for guest
     * checkout.
     */
    public boolean isLoginPromptDisplayed() {
        dismissPopupsAndBanners();
        String currentUrl = driver.getCurrentUrl();
        boolean urlMatches = currentUrl.contains("login") || currentUrl.contains("identification")
                || currentUrl.contains("customer/account");
        boolean inputMatches = isElementDisplayed(emailOrPhoneInput) || isElementDisplayed(identificationHeader);
        log.info("Guest checkout login prompt check: URL matches={}, Input visible={}", urlMatches, inputMatches);
        return urlMatches || inputMatches;
    }

    /**
     * Enters guest email into login/identification field.
     */
    public CheckoutLoginPage enterEmailOrPhone(String value) {
        log.info("Entering identification email: {}", value);
        try {
            WebElement input = findElement(emailOrPhoneInput, java.time.Duration.ofSeconds(10));
            input.clear();
            input.sendKeys(value);
        } catch (Exception e) {
            log.warn("Standard type failed, trying JS fallback: {}", e.getMessage());
            try {
                jsExecutor.executeScript(
                        "var el = document.querySelector('input.mdc-text-field__input, input[type=\"text\"], input');" +
                                "if(el){ el.value = arguments[0]; el.dispatchEvent(new Event('input', {bubbles:true})); el.dispatchEvent(new Event('change', {bubbles:true})); }",
                        value);
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    /**
     * Clicks continue button on identification page.
     */
    public CheckoutLoginPage clickContinue() {
        log.info("Clicking Continue on identification page");
        click(continueButton);
        sleep(1500);
        return this;
    }

    /**
     * Navigates back to the Cart and returns CartPage.
     */
    public CartPage navigateBackToCart() {
        log.info("Navigating back to Cart");
        try {
            driver.navigate().back();
            sleep(2000);
            if (!driver.getCurrentUrl().contains("cart")) {
                driver.get("https://www.jumia.com.eg/cart/");
            }
        } catch (Exception e) {
            driver.get("https://www.jumia.com.eg/cart/");
        }
        sleep(2000);
        return new CartPage(driver);
    }
}
