package pages.baseTest;

import driverFactory.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;
import utils.ConfigManager;
import utils.Screenshot;

/**
 * BaseTest manages setup and teardown for all test classes using
 * the DriverManager Singleton and ConfigManager Singleton.
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected SoftAssert softAssert;

    @BeforeMethod
    public void setUp() {
        log.info("================================================================================");
        log.info("Starting Test Execution on thread: {}", Thread.currentThread().getId());

        ConfigManager config = ConfigManager.getInstance();
        String browser = config.getBrowser();
        boolean headless = config.isHeadless();
        String baseUrl = config.getBaseUrl();

        driver = DriverManager.getInstance().initDriver(browser, headless);
        softAssert = new SoftAssert();

        log.info("Navigating to Base URL: {}", baseUrl);
        driver.get(baseUrl);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("Test FAILED: {}", result.getName());
            if (driver != null) {
                Screenshot.takeScreenshot(driver, result.getName());
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("Test PASSED: {}", result.getName());
        } else if (result.getStatus() == ITestResult.SKIP) {
            log.warn("Test SKIPPED: {}", result.getName());
        }

        log.info("Quitting WebDriver session");
        DriverManager.getInstance().quitDriver();
        driver = null;
        log.info("================================================================================");
    }
}
