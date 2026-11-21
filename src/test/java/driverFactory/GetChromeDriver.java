package driverFactory;

import org.openqa.selenium.WebDriver;

public class GetChromeDriver implements DriverFactory {

    public static WebDriver getDriver() {
        return DriverManager.getInstance().getDriver();
    }

    public static void quitDriver() {
        DriverManager.getInstance().quitDriver();
    }
}
