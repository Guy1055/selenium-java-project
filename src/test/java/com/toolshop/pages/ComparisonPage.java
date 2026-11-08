package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ComparisonPage extends BasePage {
    public static final By TABLE = testId("comparison-table");
    public static final By REMOVE_BUTTONS = testId("remove-product");
    public static final By EMPTY = testId("comparison-empty");

    public ComparisonPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/comparison");
    }
}
