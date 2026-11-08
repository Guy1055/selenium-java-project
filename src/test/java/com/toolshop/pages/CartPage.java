package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {
    public static final By TITLES = testId("product-title");
    public static final By QUANTITIES = testId("product-quantity");
    public static final By LINE_PRICES = testId("line-price");
    public static final By TOTAL = testId("cart-total");
    public static final By CONTINUE_SHOPPING = testId("continue-shopping");
    public static final By EMPTY_MESSAGE = By.xpath("//*[contains(text(),'The cart is empty')]");
    /** Last resort: the remove control is an unlabelled anchor with no test id or role. */
    private static final By REMOVE = By.cssSelector("tbody tr a.btn-danger");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        click(NavBar.CART);
        waitVisible(TITLES);
    }

    /**
     * The cart input updates on the change event, so commit with Tab. One atomic retried action,
     * because clearing the field first would fire a change and re-render the row.
     */
    public void setQuantity(int quantity) {
        wait.until(d -> {
            d.findElement(QUANTITIES).sendKeys(Keys.chord(Keys.CONTROL, "a"), String.valueOf(quantity), Keys.TAB);
            return true;
        });
    }

    public void removeFirstItem() {
        click(REMOVE);
    }
}
