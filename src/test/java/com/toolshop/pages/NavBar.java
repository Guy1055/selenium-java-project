package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class NavBar extends BasePage {
    @FindBy(css = "[data-test='nav-home']")
    public WebElement home;

    @FindBy(css = "[data-test='nav-categories']")
    public WebElement categories;

    @FindBy(css = "[data-test='nav-contact']")
    public WebElement contact;

    @FindBy(css = "[data-test='nav-sign-in']")
    public WebElement signIn;

    @FindBy(css = "[data-test='nav-menu']")
    public WebElement userMenu;

    @FindBy(css = "[data-test='nav-sign-out']")
    public WebElement signOut;

    @FindBy(css = "[data-test='nav-cart']")
    public WebElement cart;

    @FindBy(id = "reportsDropdown")
    private WebElement reports;

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public void openCategory(String id) {
        click(categories);
        click(testId(id));
    }

    public void logOut() {
        click(userMenu);
        click(signOut);
    }

    public void openUserMenuItem(String id) {
        click(userMenu);
        click(testId(id));
    }

    /** Report links live in a nested menu that opens on hover; clicking it closes the parent. */
    public void openReport(String id) {
        click(userMenu);
        waitVisible(reports);
        new Actions(driver).moveToElement(reports).perform();
        click(testId(id));
    }
}
