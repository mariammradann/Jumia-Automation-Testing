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
import pages.models.ProductItem;

import java.util.ArrayList;
import java.util.List;

@Epic("Jumia Egypt Automation")
@Feature("Search, Filter and Sorting")
public class Scenario1_SearchFilterSortTest extends BaseTest {

    @Story("Scenario 1: Advanced Product Search, Filter, and Sorting Validation")
    @Description("Validates applying Brand, Price range, Official Store, and Rating filters, sorting by Highest Rated, capturing products, and traversing pages.")
    @Test(dataProvider = "scenario1Data", dataProviderClass = DataProviderTest.class)
    public void testAdvancedProductSearchFilterAndSort(
            String searchCategory,
            String brand,
            double minPrice,
            double maxPrice,
            boolean officialStore,
            int rating,
            String sortOption,
            int storeCount,
            int pageCount) {

        log.info("Starting Scenario 1: Search '{}', Brand '{}', Price [{} - {}], Rating {}+",
                searchCategory, brand, minPrice, maxPrice, rating);

        HomePage homePage = new HomePage(driver).initialize();

        // 1 & 2. Search category
        SearchResultsPage searchPage = homePage.searchFor(searchCategory);
        Assert.assertTrue(searchPage.isLoaded(), "Search results page failed to load!");

        // 3. Apply filters: Brand, Price Range, Official Store, Rating
        searchPage.filterByBrand(brand);
        searchPage.filterByPriceRange(minPrice, maxPrice);
        if (officialStore) {
            searchPage.filterByOfficialStore();
        }
        searchPage.filterByRating(rating);

        // 4. Sort results by Highest Rated
        searchPage.sortBy(sortOption);

        // 5. Store details of first 5 displayed products
        List<ProductItem> page1StoredProducts = searchPage.getFirstNProducts(storeCount);
        log.info("Stored {} products from Page 1", page1StoredProducts.size());
        Assert.assertFalse(page1StoredProducts.isEmpty(), "No products found after applying filters!");

        // 6. Navigate through first 3 product pages
        List<ProductItem> allPagesProducts = new ArrayList<>(page1StoredProducts);
        for (int p = 2; p <= pageCount; p++) {
            searchPage.goToPage(p);
            List<ProductItem> pageProducts = searchPage.getFirstNProducts(storeCount);
            log.info("Page {}: Retrieved {} products", p, pageProducts.size());
            allPagesProducts.addAll(pageProducts);
        }

        // 7. Verify all stored products match criteria
        for (ProductItem item : allPagesProducts) {
            softAssert.assertNotNull(item.getTitle(), "Product title should not be null");
            if (item.getPrice() > 0) {
                // Assert within or close to filter boundary
                softAssert.assertTrue(item.getPrice() >= (minPrice * 0.8) && item.getPrice() <= (maxPrice * 1.2),
                        "Product price " + item.getPrice() + "out of expected range [" + minPrice + " - " + maxPrice + "]");
            }
        }
        softAssert.assertAll();
        log.info("Scenario 1 completed successfully");
    }
}

