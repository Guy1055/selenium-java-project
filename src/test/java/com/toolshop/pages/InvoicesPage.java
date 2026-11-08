package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InvoicesPage extends BasePage {
    public static final By TITLE = testId("page-title");
    public static final By ROWS = By.cssSelector("tbody tr");
    public static final By INVOICE_NUMBER = testId("invoice-number");
    public static final By DOWNLOAD = testId("download-invoice");

    public InvoicesPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/invoices");
    }

    public void openDetails(String id) {
        goTo("/account/invoices/" + id);
    }
}
