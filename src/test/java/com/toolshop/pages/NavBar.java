package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class NavBar extends BasePage {
    public static final By HOME = testId("nav-home");
    public static final By CATEGORIES = testId("nav-categories");
    public static final By CONTACT = testId("nav-contact");
    public static final By SIGN_IN = testId("nav-sign-in");
    public static final By USER_MENU = testId("nav-menu");
    public static final By SIGN_OUT = testId("nav-sign-out");
    public static final By CART = testId("nav-cart");
    private static final By REPORTS = By.id("reportsDropdown");

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public void openCategory(String id) {
        click(CATEGORIES);
        click(testId(id));
    }

    public void logOut() {
        click(USER_MENU);
        click(SIGN_OUT);
    }

    public void openUserMenuItem(String id) {
        click(USER_MENU);
        click(testId(id));
    }

    /** Report links live in a nested menu that opens on hover; clicking it closes the parent. */
    public void openReport(String id) {
        click(USER_MENU);
        waitVisible(REPORTS);
        new Actions(driver).moveToElement(driver.findElement(REPORTS)).perform();
        click(testId(id));
    }
}
