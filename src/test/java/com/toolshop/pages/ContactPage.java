package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;

public class ContactPage extends BasePage {
    public static final By SUBMIT = testId("contact-submit");
    public static final By FIRST_NAME = testId("first-name");
    public static final By LAST_NAME = testId("last-name");
    public static final By EMAIL = testId("email");
    public static final By SUBJECT = testId("subject");
    public static final By MESSAGE = testId("message");
    public static final By ATTACHMENT = testId("attachment");
    public static final By CONFIRMATION = alertWith("Thanks for your message");

    public ContactPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/contact");
        waitVisible(SUBMIT);
    }

    public static By error(String field) {
        return testId(field + "-error");
    }

    public void fillGuest(String firstName, String lastName, String email) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(EMAIL, email);
    }

    public void fillMessage(String subject, String message) {
        selectValue(SUBJECT, subject);
        type(MESSAGE, message);
        // The message control validates on blur.
        driver.findElement(MESSAGE).sendKeys(Keys.TAB);
    }

    public void attach(Path file) {
        driver.findElement(ATTACHMENT).sendKeys(file.toAbsolutePath().toString());
    }
}
