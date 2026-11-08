package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AdminOrderPage extends BasePage {
    @FindBy(css = "[data-test='invoice-number']")
    public WebElement invoiceNumber;

    @FindBy(css = "[data-test='order-status']")
    public WebElement status;

    @FindBy(css = "[data-test='update-status-submit']")
    public WebElement updateStatus;

    @FindBy(css = ".alert-success")
    public WebElement success;

    public AdminOrderPage(WebDriver driver) {
        super(driver);
    }

    public void open(String id) {
        goTo("/admin/orders/edit/" + id);
    }
}
