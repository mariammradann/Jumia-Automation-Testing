package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * BasePage encapsulates common WebDriver operations and provides fluent,
 * reusable interaction methods for all page objects.
 */
public class BasePage {
    protected static final Logger log = LogManager.getLogger(BasePage.class);

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor jsExecutor;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.actions = new Actions(driver);
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement findElement(By locator) {
        return findElement(locator, Duration.ofSeconds(15));
    }

    public WebElement findElement(By locator, Duration duration) {
        WebDriverWait customWait = new WebDriverWait(driver, duration);
        log.debug("Waiting for element: {}", locator);
        try {
            return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Element not visible within {} seconds: {}", duration.getSeconds(), locator);
            throw e;
        }
    }

    public List<WebElement> findElements(By locator) {
        return findElements(locator, Duration.ofSeconds(10));
    }

    public List<WebElement> findElements(By locator, Duration duration) {
        WebDriverWait customWait = new WebDriverWait(driver, duration);
        log.debug("Waiting for elements: {}", locator);
        try {
            customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return driver.findElements(locator);
        } catch (TimeoutException e) {
            log.warn("Elements not found within {} seconds: {}", duration.getSeconds(), locator);
            return Collections.emptyList();
        }
    }

    public void click(By locator) {
        log.info("Clicking on element: {}", locator);
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (Exception e) {
            log.warn("Standard click failed for {}, trying JS click: {}", locator, e.getMessage());
            jsClick(locator);
        }
    }

    public void jsClick(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            jsExecutor.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
            jsExecutor.executeScript("arguments[0].click();", element);
            log.debug("JS click executed for: {}", locator);
        } catch (Exception e) {
            log.error("JS click failed for {}: {}", locator, e.getMessage());
            throw e;
        }
    }

    public void jsClick(WebElement element) {
        try {
            jsExecutor.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
            jsExecutor.executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            log.error("JS click on WebElement failed: {}", e.getMessage());
            throw e;
        }
    }

    public void type(By locator, String text) {
        log.info("Typing '{}' into element: {}", text, locator);
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    public String getText(By locator) {
        try {
            WebElement element = findElement(locator);
            String text = element.getText().trim();
            log.debug("Retrieved text from {}: '{}'", locator, text);
            return text;
        } catch (Exception e) {
            log.warn("Could not get text from {}: {}", locator, e.getMessage());
            return "";
        }
    }

    public String getAttribute(By locator, String attributeName) {
        try {
            WebElement element = driver.findElement(locator);
            return element.getAttribute(attributeName);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isElementDisplayed(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void scrollToElement(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            jsExecutor.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        } catch (Exception e) {
            log.debug("Could not scroll to element: {}", locator);
        }
    }

    public void dismissPopupsAndBanners() {
        sleep(500); // small pause to let page settle
        // Dismiss Jumia newsletter popup if displayed
        By newsletterClose = By.cssSelector(
            "button[aria-label='newsletter_popup_close-cta'], " +
            "button.cls, .popup-close, #pop button, " +
            "button[aria-label='close'], " +
            "div.modal button.cls, " +
            "button[data-dismiss='modal']"
        );
        try {
            List<WebElement> closeButtons = driver.findElements(newsletterClose);
            for (WebElement btn : closeButtons) {
                if (btn.isDisplayed()) {
                    log.info("Dismissing newsletter popup");
                    jsClick(btn);
                    sleep(500);
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Dismiss cookie banner
        By cookieAccept = By.xpath(
            "//button[contains(., 'Accept cookies') or contains(., 'Accept') or " +
            "contains(., 'موافق') or contains(., 'Agree') or contains(., 'OK') or contains(., 'Got it')]"
        );
        try {
            List<WebElement> cookies = driver.findElements(cookieAccept);
            for (WebElement btn : cookies) {
                if (btn.isDisplayed()) {
                    log.info("Accepting cookies banner");
                    jsClick(btn);
                    sleep(500);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean navigateToPage(String redirectedUrl) {
        String currentUrl = driver.getCurrentUrl();
        log.debug("Current URL: {}", currentUrl);
        boolean matches = Objects.equals(currentUrl, redirectedUrl);
        if (!matches) {
            log.warn("Expected URL {} but was {}", redirectedUrl, currentUrl);
        }
        return matches;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void waitForUrlContains(String text, Duration timeout) {
        WebDriverWait waitUrl = new WebDriverWait(driver, timeout);
        waitUrl.until(ExpectedConditions.urlContains(text));
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
