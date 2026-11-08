package com.toolshop.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CartPage extends BasePage {
    @FindBy(css = "[data-test='product-title']")
    public WebElement title;

    @FindBy(css = "[data-test='product-title']")
    public List<WebElement> titles;

    @FindBy(css = "[data-test='product-quantity']")
    public WebElement quantity;

    @FindBy(css = "[data-test='line-price']")
    public WebElement linePrice;

    @FindBy(css = "[data-test='cart-total']")
    public WebElement total;

    @FindBy(css = "[data-test='continue-shopping']")
    public WebElement continueShopping;

    @FindBy(xpath = "//*[contains(text(),'The cart is empty')]")
    public WebElement emptyMessage;

    /** Last resort: the remove control is an unlabelled anchor with no test id or role. */
    @FindBy(css = "tbody tr a.btn-danger")
    private WebElement remove;

    private final NavBar nav;

    public CartPage(WebDriver driver) {
        super(driver);
        nav = new NavBar(driver);
    }

    public void open() {
        click(nav.cart);
        waitVisible(title);
    }

    /**
     * The cart input updates on the change event, so commit with Tab. One atomic retried action,
     * because clearing the field first would fire a change and re-render the row.
     */
    public void setQuantity(int value) {
        wait.until(d -> {
            quantity.sendKeys(Keys.chord(Keys.CONTROL, "a"), String.valueOf(value), Keys.TAB);
            return true;
        });
    }

    public void removeFirstItem() {
        click(remove);
    }
}
