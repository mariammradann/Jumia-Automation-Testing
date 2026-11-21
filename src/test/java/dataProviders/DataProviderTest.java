package dataProviders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;
import utils.CSVFileManager;
import utils.ConfigManager;
import utils.ExcelFileManager;
import utils.jsonFileManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DataProviderTest manages Data-Driven Testing (DDT) inputs loaded from
 * JSON, CSV, Excel, and Properties files.
 */
public class DataProviderTest {
    private static final Logger log = LogManager.getLogger(DataProviderTest.class);

    private static final String JSON_PATH = "src/main/resources/file.json";
    private static final String CSV_PATH = "src/main/resources/file.csv";
    private static final String EXCEL_PATH = "src/main/resources/file.xlsx";

    /**
     * Scenario 1: Advanced Search, Filter & Sort Data (From JSON)
     */
    @DataProvider(name = "scenario1Data")
    public static Object[][] getScenario1Data() {
        log.info("Loading Scenario 1 DDT data from JSON");
        try {
            jsonFileManager json = new jsonFileManager(JSON_PATH);
            LinkedHashMap<String, Object> map = json.getKeyAndValueByKey("scenario1");
            return new Object[][]{
                    {
                            map.get("searchCategory").toString(),
                            map.get("brand").toString(),
                            Double.parseDouble(map.get("minPrice").toString()),
                            Double.parseDouble(map.get("maxPrice").toString()),
                            Boolean.parseBoolean(map.get("officialStore").toString()),
                            Integer.parseInt(map.get("rating").toString()),
                            map.get("sortOption").toString(),
                            Integer.parseInt(map.get("storeCount").toString()),
                            Integer.parseInt(map.get("pageCount").toString())
                    }
            };
        } catch (Exception e) {
            log.warn("Fallback to default Scenario 1 data: {}", e.getMessage());
            return new Object[][]{
                    {"Samsung Mobile", "Samsung", 10000.0, 30000.0, true, 4, "rating", 5, 3}
            };
        }
    }

    /**
     * Scenario 2: Product Comparison Data (From JSON)
     */
    @DataProvider(name = "scenario2Data")
    public static Object[][] getScenario2Data() {
        log.info("Loading Scenario 2 DDT data from JSON");
        try {
            jsonFileManager json = new jsonFileManager(JSON_PATH);
            LinkedHashMap<String, Object> map = json.getKeyAndValueByKey("scenario2");
            return new Object[][]{
                    {map.get("searchQuery").toString(), Integer.parseInt(map.get("productsToCompare").toString())}
            };
        } catch (Exception e) {
            return new Object[][]{
                    {"Wireless Headphones", 3}
            };
        }
    }

    /**
     * Scenario 3: Cart Quantity Calculation Data (From JSON / Excel)
     */
    @DataProvider(name = "scenario3Data")
    public static Object[][] getScenario3Data() {
        log.info("Loading Scenario 3 DDT data");
        try {
            jsonFileManager json = new jsonFileManager(JSON_PATH);
            LinkedHashMap<String, Object> map = json.getKeyAndValueByKey("scenario3");
            return new Object[][]{
                    {map.get("searchQuery").toString(), 1, 2, 3, 1}
            };
        } catch (Exception e) {
            return new Object[][]{
                    {"Smart Watch", 1, 2, 3, 1}
            };
        }
    }

    /**
     * Scenario 4: Mixed Cart Operations Data (From JSON)
     */
    @DataProvider(name = "scenario4Data")
    public static Object[][] getScenario4Data() {
        log.info("Loading Scenario 4 DDT data");
        try {
            jsonFileManager json = new jsonFileManager(JSON_PATH);
            LinkedHashMap<String, Object> map = json.getKeyAndValueByKey("scenario4");
            @SuppressWarnings("unchecked")
            List<String> products = (List<String>) map.get("products");
            return new Object[][]{
                    {products.toArray(new String[0]), 2, map.get("removeProduct").toString()}
            };
        } catch (Exception e) {
            return new Object[][]{
                    {new String[]{"Mobile Phone", "Headphones", "Power Bank"}, 2, "Headphones"}
            };
        }
    }

    /**
     * Scenario 5: Guest Checkout Data (From JSON)
     */
    @DataProvider(name = "scenario5Data")
    public static Object[][] getScenario5Data() {
        log.info("Loading Scenario 5 DDT data");
        try {
            jsonFileManager json = new jsonFileManager(JSON_PATH);
            LinkedHashMap<String, Object> map = json.getKeyAndValueByKey("scenario5");
            return new Object[][]{
                    {map.get("product").toString(), map.get("guestEmail").toString()}
            };
        } catch (Exception e) {
            return new Object[][]{
                    {"Power Bank", "test.guest.jumia@gmail.com"}
            };
        }
    }

    /**
     * Scenario 6: Search Functionality with Diverse Inputs (From CSV file.csv)
     */
    @DataProvider(name = "scenario6CsvData")
    public static Object[][] getScenario6CsvData() {
        log.info("Loading Scenario 6 DDT data from CSV");
        List<Object[]> rows = new ArrayList<>();
        try {
            CSVFileManager csv = new CSVFileManager(CSV_PATH);
            List<String[]> allRows = csv.getRows();
            for (String[] record : allRows) {
                if (record.length >= 3) {
                    rows.add(new Object[]{record[0], record[1], record[2]});
                }
            }
        } catch (Exception e) {
            log.warn("Fallback to hardcoded CSV rows: {}", e.getMessage());
            rows.add(new Object[]{"Valid", "Samsung", "ResultsFound"});
            rows.add(new Object[]{"Partial", "Sam", "ResultsFound"});
            rows.add(new Object[]{"Numbers", "12345678", "NoCrash"});
            rows.add(new Object[]{"SpecialChar", "@#$%^&*", "NoCrash"});
            rows.add(new Object[]{"Arabic", "سامسونج", "ResultsFound"});
            rows.add(new Object[]{"Mixed", "Samsung 2026 هاتف", "Processed"});
        }
        return rows.toArray(new Object[0][]);
    }

    /**
     * Scenario 7: Wishlist Validation Data
     */
    @DataProvider(name = "scenario7Data")
    public static Object[][] getScenario7Data() {
        Object[][] data = new Object[][]{
                {new String[]{"Shoes", "Watch"}}
        };
        return data;
    }

    /**
     * Scenario 8: Pagination & Consistency Data
     */
    @DataProvider(name = "scenario8Data")
    public static Object[][] getScenario8Data() {
        Object[][] data = new Object[][]{
                {"Laptop", new int[]{1, 2, 3}}
        };
        return data;
    }

    /**
     * Dedicated Budget Cart Constraint Data (13,000 EGP, max 4 products, 5% tolerance)
     */
    @DataProvider(name = "budgetCartData")
    public static Object[][] getBudgetCartData() {
        ConfigManager cfg = ConfigManager.getInstance();
        double target = cfg.getTargetBudget(); // 13000.0
        double tolerance = cfg.getBudgetTolerance(); // 0.05
        int maxProducts = cfg.getMaxProducts(); // 4
        double minAllowed = cfg.getMinAllowedBudget(); // 12350.0

        Object[][] data = new Object[][]{
                {target, tolerance, minAllowed, maxProducts}
        };
        return data;
    }
}
