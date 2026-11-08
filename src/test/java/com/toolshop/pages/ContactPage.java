package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.nio.file.Path;

public class ContactPage extends BasePage {
    @FindBy(css = "[data-test='contact-submit']")
    public WebElement submit;

    @FindBy(css = "[data-test='first-name']")
    public WebElement firstName;

    @FindBy(css = "[data-test='last-name']")
    public WebElement lastName;

    @FindBy(css = "[data-test='email']")
    public WebElement email;

    @FindBy(css = "[data-test='subject']")
    public WebElement subject;

    @FindBy(css = "[data-test='message']")
    public WebElement message;

    @FindBy(css = "[data-test='attachment']")
    public WebElement attachment;

    @FindBy(xpath = "//*[@role='alert'][contains(normalize-space(.), \"Thanks for your message\")]")
    public WebElement confirmation;

    public ContactPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/contact");
        waitVisible(submit);
    }

    public static By error(String field) {
        return testId(field + "-error");
    }

    public void fillGuest(String firstName, String lastName, String email) {
        type(this.firstName, firstName);
        type(this.lastName, lastName);
        type(this.email, email);
    }

    public void fillMessage(String subject, String message) {
        selectValue(this.subject, subject);
        type(this.message, message);
        // The message control validates on blur.
        this.message.sendKeys(Keys.TAB);
    }

    public void attach(Path file) {
        attachment.sendKeys(file.toAbsolutePath().toString());
    }
}
