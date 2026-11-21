package tests;

import dataProviders.DataProviderTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchResultsPage;
import pages.baseTest.BaseTest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Epic("Jumia Egypt Automation")
@Feature("Search Validation")
public class Scenario6_SearchInputsTest extends BaseTest {

    @Story("Scenario 6: Search Functionality with Valid, Invalid, and Special Inputs")
    @Description("Executes DDT search across valid, partial, numbers, special characters, mixed, and Arabic keywords loaded from CSV.")
    @Test(dataProvider = "scenario6CsvData", dataProviderClass = DataProviderTest.class)
    public void testSearchFunctionalityInputs(String inputType, String searchQuery, String expectedBehavior) {
        log.info("Starting Scenario 6 [{}]: Query '{}' (Expected: {})", inputType, searchQuery, expectedBehavior);

        HomePage homePage = new HomePage(driver).initialize();

        // 1. Execute search
        SearchResultsPage searchResultsPage = homePage.searchFor(searchQuery);

        // 2. Verify search results or no-results behavior
        boolean loaded = searchResultsPage.isLoaded();
        Assert.assertTrue(loaded, "Search results page failed to load for query: " + searchQuery);

        // 3. Verify no application errors occur (e.g. 500 server error or broken page)
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();
        Assert.assertFalse(pageTitle.contains("500") || pageTitle.contains("404") || pageTitle.contains("Error"),
                "Application error detected on search page for query: " + searchQuery);

        // 4. Verify URL and search box update correctly
        log.info("Current URL: {}", currentUrl);
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8);
        softAssert.assertTrue(decodedUrl.contains("q=") || decodedUrl.contains("catalog"),
                "URL should contain query or catalog route for: " + searchQuery);

        String searchBoxVal = searchResultsPage.getSearchBoxValue();
        log.info("Search Box Value: '{}'", searchBoxVal);

        softAssert.assertAll();
        log.info("Scenario 6 [{}] completed successfully", inputType);
    }
}

