package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AccountPage extends BasePage {
    public static final By TITLE = testId("page-title");

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
