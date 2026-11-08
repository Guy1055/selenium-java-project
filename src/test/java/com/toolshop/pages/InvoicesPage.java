package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class InvoicesPage extends BasePage {
    @FindBy(css = "[data-test='page-title']")
    public WebElement title;

    @FindBy(css = "tbody tr")
    public WebElement row;

    @FindBy(css = "tbody tr")
    public List<WebElement> rows;

    @FindBy(css = "[data-test='invoice-number']")
    public WebElement invoiceNumber;

    @FindBy(css = "[data-test='download-invoice']")
    public WebElement download;

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
