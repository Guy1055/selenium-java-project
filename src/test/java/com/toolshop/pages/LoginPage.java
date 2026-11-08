package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    public static final By EMAIL = testId("email");
    public static final By PASSWORD = testId("password");
    public static final By SUBMIT = testId("login-submit");
    public static final By ERROR = testId("login-error");
    public static final By EMAIL_ERROR = testId("email-error");
    public static final By PASSWORD_ERROR = testId("password-error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/login");
        waitVisible(SUBMIT);
    }

    public void login(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(SUBMIT);
    }
}
