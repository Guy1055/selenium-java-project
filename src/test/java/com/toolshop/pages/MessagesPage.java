package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class MessagesPage extends BasePage {
    @FindBy(css = "tbody tr")
    public WebElement row;

    @FindBy(css = "tbody tr")
    public List<WebElement> rows;

    @FindBy(css = "[data-test='message']")
    public WebElement replyBox;

    @FindBy(css = "[data-test='reply-submit']")
    public WebElement replySubmit;

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
