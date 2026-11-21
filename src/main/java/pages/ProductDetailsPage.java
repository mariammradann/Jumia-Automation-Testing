package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.models.ProductItem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDetailsPage represents the product detail view (PDP).
 */
public class ProductDetailsPage extends BasePage {

    // Locators
    private final By productTitle = By.cssSelector("h1.-fs20, h1.-fs24, .-pts.-fs20, section.col10 h1, .col10 h1, h1[data-title], article h1, h1");
    private final By productPrice = By.cssSelector("span[data-price='true'], .-b.-ubpt, .prc.-fs24");
    private final By sellerName = By.xpath("//div[contains(., 'Seller Information') or contains(., 'معلومات البائع')]/following-sibling::div//p[contains(@class, '-m')] | //p[contains(@class, '-m -pbm')]");
    private final By productRating = By.cssSelector(".stars._m, .-df.-i-ctr.-fs12 .stars, .rv-avg");
    private final By addToCartButton = By.cssSelector(
        "form#add-to-cart button.add, " +
        "button.add.btn._prim, " +
        "button[name='action'][value='in'], " +
        "button.-mhm.-pvs.-fs16.-btn.add, " +
        "button[data-action='add'], " +
        "button.btn._prim[data-action], " +
        ".-phs.-pvs button.add, " +
        "section.-df.-j-end button, " +
        "button[data-gtm-label='add_to_cart'], " +
        "button[aria-label*='Add to cart'], " +
        "button[aria-label*='add to cart']"
    );
    private final By addToWishlistButton = By.cssSelector(
        "a#wishlist, " +
        "a[data-track-onclick='wishlist'], " +
        "a._wslt, " +
        "button[data-track-onclick='wishlist'], " +
        "button[aria-label*='wishlist'], " +
        "button[aria-label*='Wishlist'], " +
        "button[aria-label*='المفضلة'], " +
        "form[action*='wishlist'] button, " +
        "button.-wslt, " +
        ".-wslt, " +
        "svg[data-icon='heart']"
    );
    private final By popupGoToCartButton = By.xpath("//a[contains(@href, '/cart') and (contains(text(), 'Go to cart') or contains(text(), 'View cart') or contains(text(), 'عربة التسوق'))] | //button[contains(text(), 'Go to cart')]");
    private final By popupCloseButton = By.xpath("//button[contains(@aria-label, 'close') or contains(text(), 'Close') or contains(text(), 'Continue shopping')]");
    private final By recommendationCards = By.cssSelector(".reco article.prd, section.card article.prd, .cola article.prd");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
        dismissPopupsAndBanners();
    }

    /**
     * Gets the product name/title.
     */
    public String getProductTitle() {
        try {
            List<WebElement> h1s = driver.findElements(productTitle);
            for (WebElement h1 : h1s) {
                String t = h1.getText().trim();
                if (!t.isEmpty() && !t.equalsIgnoreCase("my.jumia.com.eg") && !t.equalsIgnoreCase("jumia") && !t.toLowerCase().contains("jumia.com")) {
                    return t;
                }
            }
        } catch (Exception ignored) {}
        return getText(productTitle);
    }

    /**
     * Gets the parsed numeric product price.
     */
    public double getProductPrice() {
        String raw = getText(productPrice);
        return SearchResultsPage.parsePrice(raw);
    }

    /**
     * Gets the raw price text.
     */
    public String getProductPriceRaw() {
        return getText(productPrice);
    }

    /**
     * Gets the seller name.
     */
    public String getSellerName() {
        String seller = getText(sellerName);
        if (seller.isEmpty()) {
            return "Jumia";
        }
        return seller;
    }

    /**
     * Gets the numeric rating.
     */
    public double getRating() {
        String raw = getText(productRating);
        return SearchResultsPage.parseRating(raw);
    }

    /**
     * Captures all details of the current product as a ProductItem model.
     */
    public ProductItem captureProductDetails() {
        dismissPopupsAndBanners();
        String title = getProductTitle();
        double price = getProductPrice();
        String rawPrice = getProductPriceRaw();
        String seller = getSellerName();
        double rating = getRating();
        String url = driver.getCurrentUrl();

        ProductItem item = new ProductItem(title, price, rawPrice, seller, rating, "", url);
        log.info("Captured PDP Details -> Title: '{}' | Price: {} EGP | Seller: '{}' | Rating: {}",
                title, price, seller, rating);
        return item;
    }

    /**
     * Adds current product to cart and closes confirmation modal or leaves it ready.
     */
    public ProductDetailsPage addToCart() {
        log.info("Adding product to cart");
        dismissPopupsAndBanners();

        // Wait for page to fully load
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}

        // Pre-select size / variation if required
        try {
            List<WebElement> variations = driver.findElements(By.cssSelector(".-rad4.-pam, .size-selector label, input[name='size'] + label, .sku-selector .opt, .variation-option, .-fs14.-m"));
            for (WebElement var : variations) {
                if (var.isDisplayed()) {
                    jsClick(var);
                    sleep(1000);
                    break;
                }
            }
        } catch (Exception ignored) {}

        boolean added = false;

        // Attempt 1: Standard CSS selector click
        try {
            scrollToElement(addToCartButton);
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            WebElement btn = shortWait.until(
                ExpectedConditions.elementToBeClickable(addToCartButton)
            );
            btn.click();
            sleep(1500);
            added = true;
            log.info("Add to Cart clicked via CSS selector");
        } catch (Exception e) {
            log.warn("CSS Add to Cart failed: {}", e.getMessage());
        }

        // Attempt 2: JavaScript-based button finder
        // After attempting to click Add to Cart, wait for any confirmation popup and close it explicitly before proceeding
            if (added) {
                try {
                    // Wait up to 5 seconds for a known close button in the add-to-cart confirmation modal
                    WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement closeBtn = popupWait.until(
                            ExpectedConditions.elementToBeClickable(popupCloseButton)
                    );
                    if (closeBtn != null && closeBtn.isDisplayed()) {
                        jsClick(closeBtn);
                        log.info("Closed Add-to-Cart confirmation popup via explicit wait");
                    }
                } catch (Exception e) {
                    // No popup appeared; this is fine
                    log.debug("Add-to-Cart popup not detected or already closed: {}", e.getMessage());
                }
            }
            // Proceed with existing fallback handling if needed
            if (!added) {
            try {
                WebElement btn = (WebElement) jsExecutor.executeScript(
                    "var form = document.querySelector('form#add-to-cart, form[action*=\"/cart/products/\"]');" +
                    "if(form){" +
                    "  var b = form.querySelector('button[type=\"submit\"], button');" +
                    "  if(b) return b;" +
                    "}" +
                    "var btns = document.querySelectorAll('button, a.btn');" +
                    "for(var b of btns){" +
                    "  var t = (b.innerText||b.textContent||'').toLowerCase().trim();" +
                    "  if(b.offsetParent!==null && (" +
                    "    t==='add to cart' || t==='buy now' || t==='add to bag' ||" +
                    "    t==='أضف إلى عربة التسوق' || t==='أضف إلى السلة' ||" +
                    "    (b.name==='action' && b.value==='in')" +
                    "  )) return b;" +
                    "}" +
                    "return null;"
                );
                if (btn != null) {
                    jsExecutor.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                    jsExecutor.executeScript("arguments[0].click();", btn);
                    sleep(2000);
                    added = true;
                    log.info("Add to Cart clicked via JavaScript finder");
                }
            } catch (Exception e) {
                log.warn("JS Add to Cart finder failed: {}", e.getMessage());
            }
        }

        // Attempt 3: XPath fallback
        // After attempting to click Add to Cart, wait for any confirmation popup and close it explicitly before proceeding
            if (added) {
                try {
                    // Wait up to 5 seconds for a known close button in the add-to-cart confirmation modal
                    WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement closeBtn = popupWait.until(
                            ExpectedConditions.elementToBeClickable(popupCloseButton)
                    );
                    if (closeBtn != null && closeBtn.isDisplayed()) {
                        jsClick(closeBtn);
                        log.info("Closed Add-to-Cart confirmation popup via explicit wait");
                    }
                } catch (Exception e) {
                    // No popup appeared; this is fine
                    log.debug("Add-to-Cart popup not detected or already closed: {}", e.getMessage());
                }
            }
            // Proceed with existing fallback handling if needed
            if (!added) {
            try {
                By xpathFallback = By.xpath(
                    "//form[contains(@id,'add-to-cart') or contains(@action,'/cart/products/')]//button | " +
                    "//button[contains(@class,'add') or (@name='action' and @value='in') or " +
                    "contains(normalize-space(.),'Add to Cart') or " +
                    "contains(normalize-space(.),'Buy Now') or " +
                    "contains(normalize-space(.),'أضف إلى عربة التسوق') or " +
                    "contains(normalize-space(.),'أضف إلى السلة')]"
                );
                List<WebElement> btns = driver.findElements(xpathFallback);
                for (WebElement btn : btns) {
                    if (btn.isDisplayed()) {
                        jsExecutor.executeScript("arguments[0].click();", btn);
                        sleep(2000);
                        added = true;
                        log.info("Add to Cart clicked via XPath fallback");
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("XPath Add to Cart fallback failed: {}", e.getMessage());
            }
        }

        // After attempting to click Add to Cart, wait for any confirmation popup and close it explicitly before proceeding
            if (added) {
                try {
                    // Wait up to 5 seconds for a known close button in the add-to-cart confirmation modal
                    WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement closeBtn = popupWait.until(
                            ExpectedConditions.elementToBeClickable(popupCloseButton)
                    );
                    if (closeBtn != null && closeBtn.isDisplayed()) {
                        jsClick(closeBtn);
                        log.info("Closed Add-to-Cart confirmation popup via explicit wait");
                    }
                } catch (Exception e) {
                    // No popup appeared; this is fine
                    log.debug("Add-to-Cart popup not detected or already closed: {}", e.getMessage());
                }
            }
            // Proceed with existing fallback handling if needed
            if (!added) {
            log.warn("Could not click Add to Cart - product may already be in cart or page structure changed");
        } else {
            sleep(3500);
            handleAddToCartPopup(false);
        }
        return this;
    }

    /**
     * Adds product to cart and immediately proceeds to Cart page.
     */
    public CartPage addToCartAndGoToCart() {
        log.info("Adding product to cart and proceeding to cart");
        addToCart();

        // Try clicking popup 'Go to cart' button if present
        List<WebElement> goToCartBtns = driver.findElements(popupGoToCartButton);
        if (!goToCartBtns.isEmpty() && goToCartBtns.get(0).isDisplayed()) {
            jsClick(goToCartBtns.get(0));
        } else {
            driver.get("https://www.jumia.com.eg/cart/");
        }
        sleep(3000);
        return new CartPage(driver);
    }

    private void handleAddToCartPopup(boolean navigateToCart) {
        try {
            if (navigateToCart) {
                List<WebElement> btns = driver.findElements(popupGoToCartButton);
                if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                    jsClick(btns.get(0));
                    return;
                }
            } else {
                List<WebElement> closeBtns = driver.findElements(popupCloseButton);
                if (!closeBtns.isEmpty() && closeBtns.get(0).isDisplayed()) {
                    jsClick(closeBtns.get(0));
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Clicks the Wishlist button on the PDP.
     * For guest users, this triggers the login prompt / authentication redirect.
     */
    public WishlistModalPage addToWishlist() {
        log.info("Clicking Add to Wishlist");
        dismissPopupsAndBanners();
        try {
            scrollToElement(addToWishlistButton);
            click(addToWishlistButton);
        } catch (Exception e) {
            log.warn("Standard wishlist click failed: {}, trying JS fallback", e.getMessage());
            try {
                jsExecutor.executeScript(
                    "var btn = document.querySelector('a#wishlist, a[data-track-onclick=\"wishlist\"], a._wslt, button[aria-label*=\"wishlist\"], button[data-track-onclick=\"wishlist\"], form[action*=\"wishlist\"] button, .-wslt');" +
                    "if(btn) { btn.scrollIntoView({block: 'center'}); btn.click(); }"
                );
            } catch (Exception ignored) {}
        }
        sleep(2000);
        return new WishlistModalPage(driver);
    }

    /**
     * Retrieves titles of recommended products displayed on the page.
     */
    public List<String> getRecommendationTitles() {
        List<WebElement> cards = driver.findElements(recommendationCards);
        List<String> titles = new ArrayList<>();
        for (WebElement card : cards) {
            try {
                titles.add(card.findElement(By.cssSelector("h3.name, .name")).getText().trim());
            } catch (Exception ignored) {}
        }
        return titles;
    }

    /**
     * Directly navigates to Cart page.
     */
    public CartPage goToCart() {
        driver.get("https://www.jumia.com.eg/cart/");
        return new CartPage(driver);
    }
}

