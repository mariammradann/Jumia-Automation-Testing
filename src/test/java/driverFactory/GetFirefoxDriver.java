package driverFactory;

import org.openqa.selenium.WebDriver;

public class GetFirefoxDriver implements DriverFactory {

    public static WebDriver getDriver() {
        return DriverManager.getInstance().initDriver("firefox", false);
    }

    public static void quitDriver() {
        DriverManager.getInstance().quitDriver();
    }
}
