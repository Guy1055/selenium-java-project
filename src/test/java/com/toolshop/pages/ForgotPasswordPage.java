package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ForgotPasswordPage extends BasePage {
    public static final By EMAIL = testId("email");
    public static final By SUBMIT = testId("forgot-password-submit");
    public static final By EMAIL_ERROR = testId("email-error");
    public static final By CONFIRMATION = By.cssSelector(".alert-success");

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/forgot-password");
        waitVisible(SUBMIT);
    }
}
