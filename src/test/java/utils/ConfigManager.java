package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigManager implements the Singleton Pattern to load and provide
 * centralized application configurations and budget rules.
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static volatile ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        String configPath = "src/main/resources/config.properties";
        try (FileInputStream fis = new FileInputStream(configPath)) {
            properties.load(fis);
            log.info("Configurations loaded from {}", configPath);
        } catch (IOException e) {
            log.warn("Could not load {}. Using defaults. Error: {}", configPath, e.getMessage());
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public String getProperty(String key, String defaultValue) {
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.trim().isEmpty()) {
            return systemProp.trim();
        }
        return properties.getProperty(key, defaultValue);
    }

    public String getBaseUrl() {
        return getProperty("baseUrl", "https://www.jumia.com.eg/");
    }

    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "false"));
    }

    public int getExplicitWait() {
        return Integer.parseInt(getProperty("explicitWait", "15"));
    }

    public double getTargetBudget() {
        return Double.parseDouble(getProperty("targetBudget", "13000.0"));
    }

    public double getBudgetTolerance() {
        return Double.parseDouble(getProperty("budgetTolerance", "0.05"));
    }

    public int getMaxProducts() {
        return Integer.parseInt(getProperty("maxProducts", "4"));
    }

    /**
     * Calculates the minimum allowable budget based on target budget and tolerance percentage.
     * For 13,000 EGP with 5% tolerance: 13,000 * (1 - 0.05) = 12,350 EGP.
     */
    public double getMinAllowedBudget() {
        return getTargetBudget() * (1.0 - getBudgetTolerance());
    }
}
