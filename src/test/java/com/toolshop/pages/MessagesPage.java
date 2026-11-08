package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MessagesPage extends BasePage {
    public static final By ROWS = By.cssSelector("tbody tr");
    public static final By REPLY_BOX = testId("message");
    public static final By REPLY_SUBMIT = testId("reply-submit");

    public MessagesPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/messages");
    }

    /** Waits for the message text: submitting a reply before it renders throws in the app. */
    public void openDetails(String id, String messageText) {
        goTo("/account/messages/" + id);
        waitVisible(textOnPage(messageText));
    }

    public static By textOnPage(String text) {
        return By.xpath("//*[normalize-space(text())=\"" + text + "\"]");
    }
}
