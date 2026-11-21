package pages.models;

import java.util.Objects;

/**
 * Model representing a product item extracted from Jumia search results or product details page.
 */
public class ProductItem {
    private String title;
    private double price;
    private String priceRaw;
    private String seller;
    private double rating;
    private String sku;
    private String productUrl;
    private int pageNumber;

    public ProductItem() {}

    public ProductItem(String title, double price, String priceRaw, String seller, double rating, String sku, String productUrl) {
        this.title = title;
        this.price = price;
        this.priceRaw = priceRaw;
        this.seller = seller;
        this.rating = rating;
        this.sku = sku;
        this.productUrl = productUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceRaw() {
        return priceRaw;
    }

    public void setPriceRaw(String priceRaw) {
        this.priceRaw = priceRaw;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public String toString() {
        return "ProductItem{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", priceRaw='" + priceRaw + '\'' +
                ", seller='" + seller + '\'' +
                ", rating=" + rating +
                ", sku='" + sku + '\'' +
                ", pageNumber=" + pageNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductItem that = (ProductItem) o;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}

