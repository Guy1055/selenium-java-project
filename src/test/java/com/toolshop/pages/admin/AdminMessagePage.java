package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import com.toolshop.pages.MessagesPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminMessagePage extends BasePage {
    public static final By STATUS = testId("status");
    public static final By REPLY_BOX = testId("message");
    public static final By REPLY_SUBMIT = testId("reply-submit");

    public AdminMessagePage(WebDriver driver) {
        super(driver);
    }

    /** Waits for the message text: acting before it renders throws in the app. */
    public void open(String id, String messageText) {
        goTo("/admin/messages/" + id);
        waitVisible(MessagesPage.textOnPage(messageText));
    }
}
