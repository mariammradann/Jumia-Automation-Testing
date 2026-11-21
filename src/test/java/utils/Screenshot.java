package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Screenshot {

    private static final Logger log = LogManager.getLogger(Screenshot.class);

    /**
     * Takes a screenshot of the current page and saves it with the specified test name.
     *
     * @param driver the active WebDriver instance
     * @param testName name of the test or descriptive label
     * @return saved File instance, or null if driver is null or error occurred
     */
    public static File takeScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            log.warn("Cannot take screenshot: driver is null");
            return null;
        }

        String safeTestName = (testName == null || testName.trim().isEmpty()) ? "Screenshot" : testName;
        log.info("Taking screenshot for test: {}", safeTestName);
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path targetDir = Paths.get("screenshots");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetFile = targetDir.resolve(safeTestName + "_" + timestamp + ".png");
            Files.copy(source.toPath(), targetFile);
            log.info("Screenshot saved: {}", targetFile.toAbsolutePath());
            return targetFile.toFile();
        } catch (IOException e) {
            log.error("Failed saving screenshot file for test {}", safeTestName, e);
            return null;
        } catch (WebDriverException e) {
            log.error("Failed to take screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Takes a screenshot with default name "Screenshot".
     *
     * @param driver the active WebDriver instance
     * @return saved File instance
     */
    public static File takeScreenshot(WebDriver driver) {
        return takeScreenshot(driver, "Screenshot");
    }
}
