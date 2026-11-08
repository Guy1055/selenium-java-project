package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ComparisonPage extends BasePage {
    @FindBy(css = "[data-test='comparison-table']")
    public WebElement table;

    @FindBy(css = "[data-test='remove-product']")
    public WebElement removeButton;

    @FindBy(css = "[data-test='remove-product']")
    public List<WebElement> removeButtons;

    @FindBy(css = "[data-test='comparison-empty']")
    public WebElement empty;

    public ComparisonPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/comparison");
    }
}
