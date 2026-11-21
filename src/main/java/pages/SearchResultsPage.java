package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.models.ProductItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SearchResultsPage represents the catalog listing page with products, filters,
 * sorting, and pagination.
 */
public class SearchResultsPage extends BasePage {

    // Locators
    private final By productCards = By.cssSelector("article.prd, article.c-prd, article.-df");
    private final By searchInput = By.cssSelector("input#fi-q, input[name='q']");
    private final By noResultsBanner = By.cssSelector(".-pvm.-fs16, .card.-tac, ._no-res, h2.-pvm");
    private final By sortDropdown = By
            .cssSelector("a[aria-label*='Sort'], .sort-trigger, label[for='dpdw-sort'], a.-df.-i-ctr[href*='sort=']");
    private final By paginationLinks = By.cssSelector("a.pg, a[aria-label*='Page']");
    private final By minPriceInput = By
            .cssSelector("input[name='min'].pi, input[name='min'], #price input[name='min']");
    private final By maxPriceInput = By
            .cssSelector("input[name='max'].pi, input[name='max'], #price input[name='max']");
    private final By priceFilterSubmit = By
            .cssSelector("form#price button, form[data-type='price'] button, button[aria-label='Filter Price']");
    private final By officialStoreCheckbox = By.xpath(
            "//a[contains(@aria-label, 'Official Store') or contains(text(), 'Official Store') or contains(@href, 'official_store=1')]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
        dismissPopupsAndBanners();
    }

    /** Checks if the search results page loaded without errors. */
    public boolean isLoaded() {
        return isElementDisplayed(searchInput) || !driver.findElements(productCards).isEmpty() || hasNoResults();
    }

    /** Checks if a 'no results found' message or empty catalog is displayed. */
    public boolean hasNoResults() {
        if (isElementDisplayed(noResultsBanner)) {
            return true;
        }
        return driver.findElements(productCards).isEmpty();
    }

    /** Gets the search input value currently displayed in the search box. */
    public String getSearchBoxValue() {
        try {
            WebElement input = driver.findElement(searchInput);
            return input.getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    /** Applies the Brand filter by name (e.g. "Samsung"). */
    public SearchResultsPage filterByBrand(String brandName) {
        log.info("Applying Brand filter: {}", brandName);
        dismissPopupsAndBanners();
        // Build the XPath safely – no stray character literals
        String xpath = "//a[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + brandName.toLowerCase() + "')"
                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + brandName.toLowerCase() + "')"
                + " or contains(@href,'/" + brandName.toLowerCase() + "/')]";

        By brandLocator = By.xpath(xpath);
        try {
            WebElement brandLink = wait.until(ExpectedConditions.presenceOfElementLocated(brandLocator));
            scrollToElement(brandLocator);
            jsClick(brandLink);
            waitForPageReload();
        } catch (Exception e) {
            log.warn("Could not click brand filter via standard locator, trying fallback query parameter: {}",
                    e.getMessage());
            String currentUrl = driver.getCurrentUrl();
            String brandParam = brandName.toLowerCase().replace(" ", "-");
            if (!currentUrl.contains(brandParam)) {
                driver.get(currentUrl + (currentUrl.contains("?") ? "&" : "?") + "brand=" + brandParam);
                waitForPageReload();
            }
        }
        return this;
    }

    /** Applies price range filter (min and max). */
    public SearchResultsPage filterByPriceRange(double min, double max) {
        log.info("Applying Price filter: {} to {} EGP", min, max);
        dismissPopupsAndBanners();
        try {
            List<WebElement> minInputs = driver.findElements(minPriceInput);
            List<WebElement> maxInputs = driver.findElements(maxPriceInput);
            if (!minInputs.isEmpty() && !maxInputs.isEmpty()) {
                WebElement minEl = minInputs.get(0);
                WebElement maxEl = maxInputs.get(0);
                scrollToElement(minPriceInput);
                minEl.clear();
                minEl.sendKeys(String.valueOf((int) min));
                maxEl.clear();
                maxEl.sendKeys(String.valueOf((int) max));
                List<WebElement> submits = driver.findElements(priceFilterSubmit);
                if (!submits.isEmpty() && submits.get(0).isDisplayed()) {
                    jsClick(submits.get(0));
                } else {
                    maxEl.sendKeys(Keys.ENTER);
                }
                waitForPageReload();
            } else {
                applyPriceFilterViaUrl(min, max);
            }
        } catch (Exception e) {
            log.warn("Fallback to direct price URL filter: {}", e.getMessage());
            applyPriceFilterViaUrl(min, max);
        }
        return this;
    }

    private void applyPriceFilterViaUrl(double min, double max) {
        String currentUrl = driver.getCurrentUrl();
        String priceParam = "price=" + (int) min + "-" + (int) max;
        if (!currentUrl.contains("price=")) {
            driver.get(currentUrl + (currentUrl.contains("?") ? "&" : "?") + priceParam);
            waitForPageReload();
        }
    }

    /** Applies Official Store filter. */
    public SearchResultsPage filterByOfficialStore() {
        log.info("Applying Official Store filter");
        dismissPopupsAndBanners();
        try {
            List<WebElement> officialElements = driver.findElements(officialStoreCheckbox);
            if (!officialElements.isEmpty()) {
                scrollToElement(officialStoreCheckbox);
                jsClick(officialElements.get(0));
                waitForPageReload();
            } else {
                String currentUrl = driver.getCurrentUrl();
                if (!currentUrl.contains("official_store=1")) {
                    driver.get(currentUrl + (currentUrl.contains("?") ? "&" : "?") + "official_store=1");
                    waitForPageReload();
                }
            }
        } catch (Exception e) {
            log.warn("Official store filter exception: {}", e.getMessage());
        }
        return this;
    }

    /** Applies customer rating filter (e.g. 4 stars & above). */
    public SearchResultsPage filterByRating(int stars) {
        log.info("Applying Rating filter: {} stars and above", stars);
        dismissPopupsAndBanners();
        By ratingLocator = By
                .xpath("//a[contains(@href, 'rating=" + stars + "') or contains(@aria-label, '" + stars + "stars')]");
        try {
            List<WebElement> ratingElements = driver.findElements(ratingLocator);
            if (!ratingElements.isEmpty()) {
                scrollToElement(ratingLocator);
                jsClick(ratingElements.get(0));
                waitForPageReload();
            } else {
                String currentUrl = driver.getCurrentUrl();
                if (!currentUrl.contains("rating=" + stars)) {
                    driver.get(currentUrl + (currentUrl.contains("?") ? "&" : "?") + "rating=" + stars);
                    waitForPageReload();
                }
            }
        } catch (Exception e) {
            log.warn("Rating filter exception: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Sorts catalog results by option ("rating" / "Highest Rated", "lowest-price",
     * "highest-price", "newest").
     */
    public SearchResultsPage sortBy(String sortOption) {
        log.info("Sorting results by: {}", sortOption);
        dismissPopupsAndBanners();
        String sortKey = sortOption.toLowerCase().contains("rate") ? "rating"
                : sortOption.toLowerCase().contains("low") ? "lowest-price"
                        : sortOption.toLowerCase().contains("high") ? "highest-price" : sortOption;
        By sortLink = By.xpath("//a[contains(@href, 'sort=" + sortKey
                + "') or contains(text(), 'Highest Rated') or contains(text(), 'Product Rating')]");
        try {
            List<WebElement> elements = driver.findElements(sortLink);
            if (!elements.isEmpty()) {
                scrollToElement(sortLink);
                jsClick(elements.get(0));
                waitForPageReload();
            } else {
                String currentUrl = driver.getCurrentUrl();
                if (!currentUrl.contains("sort=" + sortKey)) {
                    driver.get(currentUrl + (currentUrl.contains("?") ? "&" : "?") + "sort=" + sortKey);
                    waitForPageReload();
                }
            }
        } catch (Exception e) {
            log.warn("Sort exception: {}", e.getMessage());
        }
        return this;
    }

    /** Navigates to a specific page number. */
    public SearchResultsPage goToPage(int pageNumber) {
        log.info("Navigating to catalog page: {}", pageNumber);
        dismissPopupsAndBanners();
        By pageLink = By.xpath("//a[contains(@class, 'pg') and (@aria-label='Page " + pageNumber + "' or text()='"
                + pageNumber + "')]");
        try {
            List<WebElement> links = driver.findElements(pageLink);
            if (!links.isEmpty()) {
                scrollToElement(pageLink);
                jsClick(links.get(0));
                waitForPageReload();
            } else {
                String currentUrl = driver.getCurrentUrl();
                String targetUrl = currentUrl.replaceAll("page=\\d+", "page=" + pageNumber);
                if (!targetUrl.contains("page=")) {
                    targetUrl += (targetUrl.contains("?") ? "&" : "?") + "page=" + pageNumber;
                }
                driver.get(targetUrl);
                waitForPageReload();
            }
        } catch (Exception e) {
            log.warn("Exception during pagination navigation: {}", e.getMessage());
        }
        return this;
    }

    public int getCurrentPageNumber() {
        try {
            String url = driver.getCurrentUrl();
            if (url.contains("page=")) {
                String pageStr = url.substring(url.indexOf("page=") + 5).split("&|#")[0];
                return Integer.parseInt(pageStr);
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    private void waitForPageReload() {
        sleep(1500);
        dismissPopupsAndBanners();
    }

    /**
     * Clicks on a product whose title contains the given keyword
     * (case‑insensitive).
     */
    public ProductDetailsPage clickProductByTitleKeyword(String keyword) {
        log.info("Clicking product with keyword: {}", keyword);
        dismissPopupsAndBanners();
        List<WebElement> cards = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            cards = driver.findElements(productCards);
            if (!cards.isEmpty())
                break;
            cards = driver.findElements(By.cssSelector("article, a.core, .-gallery article, .c-prd"));
            if (!cards.isEmpty())
                break;
            sleep(1500);
        }
        if (cards == null || cards.isEmpty()) {
            throw new NoSuchElementException("No product cards found on the page");
        }
        List<WebElement> matched = new ArrayList<>();
        String normKeyword = keyword.toLowerCase().replace("-", " ").trim();
        for (WebElement c : cards) {
            try {
                List<WebElement> productLinks = c
                        .findElements(By.cssSelector("a.core[href*='.html'], a[href*='.html']"));
                if (productLinks.isEmpty())
                    continue;
                String href = productLinks.get(0).getAttribute("href");
                if (href == null || !href.contains(".html") || href.contains("/mlp-") || href.contains("-store"))
                    continue;
                String cardText = c.getText().toLowerCase();
                if (cardText.contains(normKeyword)
                        || cardText.replaceAll("\\s+", "").contains(normKeyword.replace(" ", ""))) {
                    matched.add(c);
                }
            } catch (Exception ignored) {
            }
        }
        if (matched.isEmpty()) {
            throw new NoSuchElementException("No product matching keyword '" + keyword + "' found");
        }
        WebElement targetCard = matched.get(0);
        try {
            WebElement link = targetCard
                    .findElement(By.cssSelector("a.core[href*='.html'], a[href*='.html'], a.core, a"));
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                driver.get(href);
            } else {
                jsClick(link);
            }
        } catch (Exception e) {
            jsClick(targetCard);
        }
        sleep(2000);
        return new ProductDetailsPage(driver);
    }

    /**
     * Clicks on the product card at a specified index to open ProductDetailsPage.
     */
    public ProductDetailsPage clickProductAtIndex(int index) {
        log.info("Clicking product at index: {}", index);
        dismissPopupsAndBanners();
        List<WebElement> cards = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            cards = driver.findElements(productCards);
            if (!cards.isEmpty())
                break;
            cards = driver.findElements(By.cssSelector("article, a.core, .-gallery article, .c-prd"));
            if (!cards.isEmpty())
                break;
            sleep(1500);
        }
        List<WebElement> productCardElements = new ArrayList<>();
        List<String> productHrefs = new ArrayList<>();
        if (cards != null) {
            for (WebElement c : cards) {
                try {
                    List<WebElement> productLinks = c
                            .findElements(By.cssSelector("a.core[href*='.html'], a[href*='.html']"));
                    if (productLinks.isEmpty())
                        continue;
                    String href = productLinks.get(0).getAttribute("href");
                    if (href == null || !href.contains(".html") || href.contains("/mlp-") || href.contains("-store"))
                        continue;
                    String cardText = c.getText().toLowerCase();
                    if (cardText.contains("official store") || cardText.contains("متجر رسمي") ||
                            cardText.contains("out of stock") || cardText.contains("غير متوفر")) {
                        continue;
                    }
                    productCardElements.add(c);
                    productHrefs.add(href);
                } catch (Exception ignored) {
                }
            }
        }
        if (!productHrefs.isEmpty() && index < productHrefs.size()) {
            String targetHref = productHrefs.get(index);
            log.info("Navigating directly to verified product URL: {}", targetHref);
            driver.get(targetHref);
            sleep(2500);
            return new ProductDetailsPage(driver);
        }
        if (!productCardElements.isEmpty()) {
            cards = productCardElements;
        }
        if (cards == null || cards.isEmpty() || index >= cards.size()) {
            throw new IndexOutOfBoundsException(
                    "Card index " + index + " exceeds available cards " + (cards != null ? cards.size() : 0));
        }
        WebElement card = cards.get(index);
        String href = null;
        try {
            WebElement link = card.findElement(By.cssSelector("a.core[href*='.html'], a[href*='.html'], a.core, a"));
            href = link.getAttribute("href");
        } catch (Exception ignored) {
        }
        if (href != null && !href.isEmpty()) {
            driver.get(href);
        } else {
            WebElement link = card.findElement(By.cssSelector("a.core, a.name, a"));
            jsClick(link);
        }
        sleep(2000);
        return new ProductDetailsPage(driver);
    }

    /** Returns up to the first N product items from the current search results. */
    public List<ProductItem> getFirstNProducts(int n) {
        List<ProductItem> items = new ArrayList<>();
        try {
            List<WebElement> cards = driver.findElements(productCards);
            int limit = Math.min(n, cards.size());
            for (int i = 0; i < limit; i++) {
                WebElement card = cards.get(i);
                String title = "";
                try {
                    title = card.findElement(By.cssSelector("h3, .name")).getText();
                } catch (Exception ignored) {
                }
                String priceRaw = "";
                try {
                    priceRaw = card.findElement(By.cssSelector(".prc, .price, .price .value")).getText();
                } catch (Exception ignored) {
                }
                double price = parsePrice(priceRaw);
                String href = "";
                try {
                    href = card.findElement(By.cssSelector("a.core[href*='.html'], a[href*='.html']"))
                            .getAttribute("href");
                } catch (Exception ignored) {
                }
                ProductItem pi = new ProductItem();
                pi.setTitle(title);
                pi.setPrice(price);
                pi.setPriceRaw(priceRaw);
                pi.setProductUrl(href);
                items.add(pi);
            }
        } catch (Exception e) {
            log.warn("Failed to extract first N products: {}", e.getMessage());
        }
        return items;
    }

    public static double parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isEmpty())
            return 0.0;
        try {
            String cleaned = rawPrice.replaceAll("[^0-9.]", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double parseRating(String rawRating) {
        if (rawRating == null || rawRating.isEmpty())
            return 0.0;
        try {
            String cleaned = rawRating.replaceAll("[^0-9.]", " ").trim().split("\\s+")[0];
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
