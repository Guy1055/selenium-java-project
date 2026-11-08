package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductPage extends BasePage {
    public static final By NAME = testId("product-name");
    public static final By UNIT_PRICE = testId("unit-price");
    public static final By DESCRIPTION = testId("product-description");
    public static final By QUANTITY = testId("quantity");
    public static final By INCREASE = testId("increase-quantity");
    public static final By DECREASE = testId("decrease-quantity");
    public static final By ADD_TO_CART = testId("add-to-cart");
    public static final By ADD_TO_FAVORITES = testId("add-to-favorites");
    public static final By CART_BADGE = testId("cart-quantity");
    public static final By OUT_OF_STOCK = testId("out-of-stock");
    public static final By SPEC_ROWS = testId("spec-row");
    /** Related cards have no test id; they are the only product cards on this page. */
    public static final By RELATED = By.cssSelector("a.card");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public void open(String id) {
        goTo("/product/" + id);
        waitVisible(NAME);
    }

    public void addToCart() {
        click(ADD_TO_CART);
    }

    public void addProductToCart(String id) {
        open(id);
        addToCart();
        waitVisible(alertWith("added to shopping cart"));
    }
}
