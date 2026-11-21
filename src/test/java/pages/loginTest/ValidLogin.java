package pages.loginTest;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.baseTest.BaseTest;
import pages.loginPage.LoginPage;

public class ValidLogin extends BaseTest {
    @Test
    public void testValidLoginNavigation() {
        driver.get("https://www.jumia.com.eg/customer/account/login/");
        LoginPage loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form is not displayed!");
    }
}
