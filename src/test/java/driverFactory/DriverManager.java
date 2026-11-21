package driverFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * DriverManager implements the Singleton Design Pattern (Thread-Safe Double-Checked Locking)
 * to manage the lifecycle of WebDriver instances across test runs.
 */
public class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static volatile DriverManager instance;
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Private constructor prevents direct instantiation (Singleton)
    private DriverManager() {
        log.info("DriverManager Singleton instance initialized");
    }

    /**
     * Retrieves the Singleton instance of DriverManager using Double-Checked Locking.
     */
    public static DriverManager getInstance() {
        if (instance == null) {
            synchronized (DriverManager.class) {
                if (instance == null) {
                    instance = new DriverManager();
                }
            }
        }
        return instance;
    }

    /**
     * Gets the active WebDriver instance for the current thread.
     * If no driver is active, creates a new one using default browser ("chrome").
     */
    public WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            initDriver("chrome", false);
        }
        return driverThreadLocal.get();
    }

    /**
     * Initializes a WebDriver instance for the current thread based on browser type.
     *
     * @param browser "chrome", "firefox", or "edge"
     * @param headless true if running in headless mode
     */
    public WebDriver initDriver(String browser, boolean headless) {
        log.info("Initializing WebDriver for browser: {} | headless: {}", browser, headless);
        WebDriver driver;
        String b = (browser == null ? "chrome" : browser).toLowerCase().trim();

        switch (b) {
            case "firefox":
                FirefoxOptions fOptions = new FirefoxOptions();
                if (headless) fOptions.addArguments("-headless");
                fOptions.addArguments("-private");
                driver = new FirefoxDriver(fOptions);
                break;
            case "edge":
                EdgeOptions eOptions = new EdgeOptions();
                if (headless) eOptions.addArguments("--headless=new");
                eOptions.addArguments("--inprivate");
                eOptions.addArguments("--disable-notifications");
                driver = new EdgeDriver(eOptions);
                break;
            case "chrome":
            default:
                ChromeOptions cOptions = new ChromeOptions();
                if (headless) {
                    cOptions.addArguments("--headless=new");
                }
                cOptions.addArguments("--incognito");
                cOptions.addArguments("--disable-notifications");
                cOptions.addArguments("--disable-popup-blocking");
                cOptions.addArguments("--disable-blink-features=AutomationControlled");
                cOptions.addArguments("--window-size=1920,1080");
                cOptions.addArguments("--remote-allow-origins=*");
                cOptions.addArguments("--no-sandbox");
                cOptions.addArguments("--disable-dev-shm-usage");
                driver = new ChromeDriver(cOptions);
                break;
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        log.info("WebDriver created and registered to thread: {}", Thread.currentThread().getId());
        return driver;
    }

    /**
     * Safely quits the driver instance for the current thread and cleans up the reference.
     */
    public void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            log.info("Quitting WebDriver for thread: {}", Thread.currentThread().getId());
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Exception while quitting driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}

