package driverFactory;

import org.openqa.selenium.WebDriver;

public class GetEdgeDriver implements DriverFactory {

    public static WebDriver getDriver() {
        return DriverManager.getInstance().initDriver("edge", false);
    }

    public static void quitDriver() {
        DriverManager.getInstance().quitDriver();
    }
}
