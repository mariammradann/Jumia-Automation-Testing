package pages.models;

import java.util.Objects;

/**
 * Model representing an item in the Jumia shopping cart.
 */
public class CartItem {
    private String name;
    private double unitPrice;
    private double subtotal;
    private int quantity;
    private String sku;

    public CartItem() {}

    public CartItem(String name, double unitPrice, double subtotal, int quantity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(name, cartItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

