package driverFactory;

import org.openqa.selenium.WebDriver;

public interface DriverFactory {

    static WebDriver getDriver() {
        return DriverManager.getInstance().getDriver();
    }

    static WebDriver getDriver(String browser, boolean headless) {
        return DriverManager.getInstance().initDriver(browser, headless);
    }

    static void quitDriver() {
        DriverManager.getInstance().quitDriver();
    }
}
