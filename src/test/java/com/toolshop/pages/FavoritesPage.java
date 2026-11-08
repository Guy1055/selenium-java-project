package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class FavoritesPage extends BasePage {
    @FindBy(css = "[data-test='product-name']")
    public WebElement name;

    @FindBy(css = "[data-test='product-name']")
    public List<WebElement> names;

    @FindBy(css = "[data-test='delete']")
    public WebElement delete;

    @FindBy(css = "[data-test='page-title']")
    public WebElement title;

    public FavoritesPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/favorites");
    }
}
