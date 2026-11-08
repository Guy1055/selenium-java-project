package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FavoritesPage extends BasePage {
    public static final By NAMES = testId("product-name");
    public static final By DELETE = testId("delete");
    public static final By TITLE = testId("page-title");

    public FavoritesPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/favorites");
    }
}
