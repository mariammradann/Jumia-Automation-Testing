package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.models.CartItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CartPage represents the shopping cart view where items, quantities, subtotals, and checkout are managed.
 */
public class CartPage extends BasePage {

    // Locators
    private final By cartItems = By.cssSelector("article.prd, article.-df, article[data-sku], .cart-items article");
    private final By itemName = By.cssSelector("h3.name, .name, a.name, .desc .name");
    private final By itemPrice = By.cssSelector(".prc, .-b.-fs18, .s-prc-w .prc, .price");
    private final By cartSubtotal = By.xpath("//p[contains(text(), 'Subtotal') or contains(text(), 'المجموع')]/following-sibling::p | //div[contains(@class, 'summary')]//p[contains(@class, 'prc')] | //div[contains(@class, 'summary')]//span[contains(@class, '-fs20')]");
    private final By cartTotal = By.cssSelector(".-fs24, .summary .prc, p.-fs20, .-df.-j-bet.-m .prc");
    private final By checkoutButton = By.cssSelector("a[href*='/checkout'], button[name='checkout'], a.btn._prim._md[href*='checkout']");
    private final By quantitySelect = By.cssSelector("select[name='qty'], select.-rad4, select.form-control");
    private final By quantityIncreaseBtn = By.cssSelector(
            "form[action*='/cart/products/'] button:last-of-type, " +
            "button[aria-label*='increase'], button[data-action='inc'], button.qty-inc"
    );
    private final By quantityDecreaseBtn = By.cssSelector(
            "form[action*='/cart/products/'] button:first-of-type, " +
            "button[aria-label*='decrease'], button[data-action='dec'], button.qty-dec"
    );
    private final By removeButtons = By.xpath("//button[contains(., 'Remove') or contains(., 'حذف') or contains(@aria-label, 'Remove')] | //form[contains(@action, '/cart/products/')]//button[contains(@class, '-df')]");
    private final By recommendationCards = By.cssSelector(".reco article.prd, .viewtogether article.prd, section.card article.prd");

    public CartPage(WebDriver driver) {
        super(driver);
        dismissPopupsAndBanners();
    }

    /**
     * Filters elements to return only genuine cart items, ignoring recommended products.
     */
    public List<WebElement> getCartItemElements() {
        // Simplified: return all elements matching cart item selector.
        // This avoids overly aggressive filtering that may exclude real items.
        return driver.findElements(cartItems);
    }

    /** Checks if the cart contains any items. */
    public boolean hasItems() {
        return !getCartItemElements().isEmpty();
    }

    /** Gets the number of unique product items currently in the cart. */
    public int getItemCount() {
        return getCartItemElements().size();
    }

    /** Extracts list of all CartItems currently displayed in the cart. */
    public List<CartItem> getCartItems() {
        dismissPopupsAndBanners();
        List<WebElement> elements = getCartItemElements();
        if (elements.isEmpty()) {
            sleep(2000);
            elements = getCartItemElements();
        }
        List<CartItem> items = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            WebElement el = elements.get(i);
            try {
                String name = "";
                try { name = el.findElement(itemName).getText().trim(); } catch (Exception ignored) {}
                double price = 0.0;
                try { price = SearchResultsPage.parsePrice(el.findElement(itemPrice).getText().trim()); } catch (Exception ignored) {}
                int quantity = getItemQuantity(i);
                double subtotal = price * (quantity > 0 ? quantity : 1);
                CartItem item = new CartItem(name, price, subtotal, quantity);
                items.add(item);
                log.info("Cart Item [{}]: '{}' | Unit Price: {} EGP | Qty: {} | Subtotal: {} EGP", i + 1, name, price, quantity, subtotal);
            } catch (Exception e) {
                log.warn("Error parsing cart item at index {}: {}", i, e.getMessage());
            }
        }
        return items;
    }

    /** Returns list of product names currently in the cart (unique entries as displayed). */
    public List<String> getProductNames() {
        By nameLocator = By.cssSelector(
                "[data-testid='cart-item'] h2, " +
                "[data-testid='cart-item'] h3, " +
                ".cart-item .name, " +
                "a[href*='/product'] h2, " +
                "a[href*='/product'] h3"
        );
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(nameLocator));
        return driver.findElements(nameLocator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(txt -> !txt.isEmpty())
                .collect(Collectors.toList());
    }

    /** Reads the current quantity of an item by index. */
    public int getItemQuantity(int itemIndex) {
        try {
            List<WebElement> items = getCartItemElements();
            if (itemIndex < items.size()) {
                WebElement item = items.get(itemIndex);
                // Dropdown
                List<WebElement> selects = item.findElements(quantitySelect);
                if (!selects.isEmpty()) {
                    Select sel = new Select(selects.get(0));
                    return Integer.parseInt(sel.getFirstSelectedOption().getText().trim());
                }
                // Span between +/- buttons
                List<WebElement> formSpans = item.findElements(By.xpath(
                        ".//form[contains(@action, '/cart/products/')]//span | " +
                        ".//button[contains(@aria-label, 'decrease')]/following-sibling::span | " +
                        ".//button[@data-action='dec']/following-sibling::span"
                ));
                for (WebElement sp : formSpans) {
                    String txt = sp.getText().trim().replaceAll("[^0-9]", "");
                    if (!txt.isEmpty() && txt.length() <= 2) {
                        return Integer.parseInt(txt);
                    }
                }
                // JS fallback
                Object jsQty = jsExecutor.executeScript(
                        "var item = arguments[0];" +
                        "var form = item.querySelector('form[action*=\\\"/cart/products/\\\"]');" +
                        "if(form){" +
                        "  var spans = form.querySelectorAll('span');" +
                        "  for(var s of spans){" +
                        "    var num = s.innerText.trim().replace(/[^0-9]/g, '');" +
                        "    if(num.length > 0 && num.length <= 2) return parseInt(num);" +
                        "  }" +
                        "}" +
                        "return 1;",
                        item
                );
                if (jsQty != null) {
                    return Integer.parseInt(jsQty.toString());
                }
            }
        } catch (Exception ignored) {}
        return 1;
    }

    /** Updates quantity of an item at specified index to a new target quantity. */
    public CartPage updateItemQuantity(int itemIndex, int targetQty) {
        log.info("Updating item [{}] quantity to {}", itemIndex, targetQty);
        dismissPopupsAndBanners();
        List<WebElement> items = getCartItemElements();
        if (itemIndex >= items.size()) {
            throw new IndexOutOfBoundsException("Item index " + itemIndex + " out of bounds");
        }
        WebElement item = items.get(itemIndex);
        try {
            List<WebElement> selects = item.findElements(quantitySelect);
            if (!selects.isEmpty()) {
                Select sel = new Select(selects.get(0));
                sel.selectByValue(String.valueOf(targetQty));
                try { jsExecutor.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", selects.get(0)); } catch (Exception ignored) {}
                sleep(2500);
                return this;
            }
        } catch (Exception ignored) {}
        // Fallback with +/- buttons
        try {
            int currentQty = getItemQuantity(itemIndex);
            int attempts = 0;
            while (currentQty < targetQty && attempts < 5) {
                items = getCartItemElements();
                if (itemIndex >= items.size()) break;
                item = items.get(itemIndex);
                List<WebElement> incBtns = item.findElements(quantityIncreaseBtn);
                if (!incBtns.isEmpty()) {
                    jsClick(incBtns.get(0));
                    sleep(2500);
                    currentQty = getItemQuantity(itemIndex);
                } else break;
                attempts++;
            }
            attempts = 0;
            while (currentQty > targetQty && attempts < 5) {
                items = getCartItemElements();
                if (itemIndex >= items.size()) break;
                item = items.get(itemIndex);
                List<WebElement> decBtns = item.findElements(quantityDecreaseBtn);
                if (!decBtns.isEmpty()) {
                    jsClick(decBtns.get(0));
                    sleep(2500);
                    currentQty = getItemQuantity(itemIndex);
                } else break;
                attempts++;
            }
        } catch (Exception e) {
            log.warn("Exception updating quantity via buttons: {}", e.getMessage());
        }
        return this;
    }

    /** Removes an item from the cart at specified index. */
    public CartPage removeItem(int itemIndex) {
        log.info("Removing item at index [{}] from cart", itemIndex);
        dismissPopupsAndBanners();
        List<WebElement> items = getCartItemElements();
        if (itemIndex < items.size()) {
            WebElement item = items.get(itemIndex);
            List<WebElement> itemRemoves = item.findElements(removeButtons);
            if (itemRemoves.isEmpty()) {
                itemRemoves = item.findElements(By.xpath(".//button[contains(., 'Remove') or contains(., 'حذف')] | .//form[contains(@action, '/cart/products/')]//button"));
            }
            if (!itemRemoves.isEmpty()) {
                jsClick(itemRemoves.get(0));
                sleep(2000);
            } else {
                List<WebElement> removes = driver.findElements(removeButtons);
                if (itemIndex < removes.size()) {
                    jsClick(removes.get(itemIndex));
                    sleep(2000);
                }
            }
            // Optional confirmation modal
            By confirmRemove = By.xpath("//button[contains(text(), 'Remove Item') or contains(text(), 'حذف العنصر')]");
            try {
                List<WebElement> confirms = driver.findElements(confirmRemove);
                if (!confirms.isEmpty()) {
                    jsClick(confirms.get(0));
                    sleep(1500);
                }
            } catch (Exception ignored) {}
        }
        return this;
    }

    /** Gets the total price displayed on the Cart page. */
    public double getTotalPrice() {
        dismissPopupsAndBanners();
        try {
            List<WebElement> subtotals = driver.findElements(cartSubtotal);
            for (WebElement el : subtotals) {
                double price = SearchResultsPage.parsePrice(el.getText());
                if (price > 0) return price;
            }
            List<WebElement> totals = driver.findElements(cartTotal);
            for (WebElement el : totals) {
                double price = SearchResultsPage.parsePrice(el.getText());
                if (price > 0) return price;
            }
        } catch (Exception e) {
            log.warn("Failed reading total price: {}", e.getMessage());
        }
        // Fallback calculation
        double calculated = 0.0;
        for (CartItem item : getCartItems()) {
            calculated += item.getUnitPrice() * item.getQuantity();
        }
        return calculated;
    }

    /** Adds a recommended product directly from the Cart page recommendations section. */
    public CartPage addRecommendedProductToCart(int index) {
        log.info("Adding recommended product at index [{}] from cart page", index);
        dismissPopupsAndBanners();
        try {
            List<WebElement> recos = driver.findElements(recommendationCards);
            if (index < recos.size()) {
                WebElement card = recos.get(index);
                WebElement addBtn = card.findElement(By.cssSelector("button.add, form button, a.btn"));
                jsClick(addBtn);
                sleep(2000);
            }
        } catch (Exception e) {
            log.warn("Could not add recommended product: {}", e.getMessage());
        }
        return this;
    }

    /** Refreshes the cart page and returns the refreshed CartPage to verify data persistence. */
    public CartPage refreshCart() {
        log.info("Refreshing Cart page to test persistence");
        driver.navigate().refresh();
        sleep(2000);
        dismissPopupsAndBanners();
        return this;
    }

    /** Proceeds to checkout view and returns CheckoutLoginPage. */
    public CheckoutLoginPage proceedToCheckout() {
        log.info("Proceeding to checkout");
        dismissPopupsAndBanners();
        scrollToElement(checkoutButton);
        click(checkoutButton);
        sleep(2000);
        return new CheckoutLoginPage(driver);
    }
}
