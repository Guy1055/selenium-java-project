package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import com.toolshop.pages.MessagesPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AdminMessagePage extends BasePage {
    @FindBy(css = "[data-test='status']")
    public WebElement status;

    @FindBy(css = "[data-test='message']")
    public WebElement replyBox;

    @FindBy(css = "[data-test='reply-submit']")
    public WebElement replySubmit;

    public AdminMessagePage(WebDriver driver) {
        super(driver);
    }

    /** Waits for the message text: acting before it renders throws in the app. */
    public void open(String id, String messageText) {
        goTo("/admin/messages/" + id);
        waitVisible(MessagesPage.textOnPage(messageText));
    }
}
