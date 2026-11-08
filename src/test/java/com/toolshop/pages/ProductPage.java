package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ProductPage extends BasePage {
    @FindBy(css = "[data-test='product-name']")
    public WebElement name;

    @FindBy(css = "[data-test='unit-price']")
    public WebElement unitPrice;

    @FindBy(css = "[data-test='product-description']")
    public WebElement description;

    @FindBy(css = "[data-test='quantity']")
    public WebElement quantity;

    @FindBy(css = "[data-test='increase-quantity']")
    public WebElement increase;

    @FindBy(css = "[data-test='decrease-quantity']")
    public WebElement decrease;

    @FindBy(css = "[data-test='add-to-cart']")
    public WebElement addToCartButton;

    @FindBy(css = "[data-test='add-to-favorites']")
    public WebElement addToFavorites;

    @FindBy(css = "[data-test='cart-quantity']")
    public WebElement cartBadge;

    @FindBy(css = "[data-test='out-of-stock']")
    public WebElement outOfStock;

    @FindBy(css = "[data-test='spec-row']")
    public List<WebElement> specRows;

    /** Related cards have no test id; they are the only product cards on this page. */
    @FindBy(css = "a.card")
    public WebElement related;

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public void open(String id) {
        goTo("/product/" + id);
        waitVisible(name);
    }

    public void addToCart() {
        click(addToCartButton);
    }

    public void addProductToCart(String id) {
        open(id);
        addToCart();
        waitVisible(alertWith("added to shopping cart"));
    }
}
