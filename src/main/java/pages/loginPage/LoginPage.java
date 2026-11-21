package pages.loginPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.BasePage;

/**
 * LoginPage represents the Jumia customer login page.
 */
public class LoginPage extends BasePage {

    private final By emailInput = By.cssSelector("input[name='email'], input#input_identifier_value, input[type='email']");
    private final By continueButton = By.cssSelector("button[type='submit'], form button.btn._prim");
    private final By passwordInput = By.cssSelector("input[name='password'], input[type='password']");
    private final By loginButton = By.cssSelector("button#login-button, button[type='submit']");
    private final By errorMessage = By.cssSelector(".alert-danger, .error, .to-error, .-df.-i-ctr.-fs12.-r");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public LoginPage clickContinue() {
        click(continueButton);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public void clickLogin() {
        click(loginButton);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isLoginFormDisplayed() {
        return isElementDisplayed(emailInput);
    }
}
