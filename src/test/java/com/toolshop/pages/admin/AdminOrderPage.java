package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminOrderPage extends BasePage {
    public static final By INVOICE_NUMBER = testId("invoice-number");
    public static final By STATUS = testId("order-status");
    public static final By UPDATE_STATUS = testId("update-status-submit");
    public static final By SUCCESS = By.cssSelector(".alert-success");

    public AdminOrderPage(WebDriver driver) {
        super(driver);
    }

    public void open(String id) {
        goTo("/admin/orders/edit/" + id);
    }
}
