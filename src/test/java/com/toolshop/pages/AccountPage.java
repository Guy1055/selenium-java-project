package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AccountPage extends BasePage {
    @FindBy(css = "[data-test='page-title']")
    public WebElement title;

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account");
    }

    public static By link(String section) {
        return testId("nav-" + section);
    }
}
