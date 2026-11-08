package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ForgotPasswordPage extends BasePage {
    @FindBy(css = "[data-test='email']")
    public WebElement email;

    @FindBy(css = "[data-test='forgot-password-submit']")
    public WebElement submit;

    @FindBy(css = "[data-test='email-error']")
    public WebElement emailError;

    @FindBy(css = ".alert-success")
    public WebElement confirmation;

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/forgot-password");
        waitVisible(submit);
    }
}
