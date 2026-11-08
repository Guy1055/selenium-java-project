package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProfilePage extends BasePage {
    public static final By FIRST_NAME = testId("first-name");
    public static final By LAST_NAME = testId("last-name");
    public static final By EMAIL = testId("email");
    public static final By UPDATE_SUBMIT = testId("update-profile-submit");
    public static final By TOTP_SECRET = testId("totp-secret");
    public static final By TOTP_CODE = testId("totp-code");
    public static final By TOTP_VERIFY = testId("verify-totp");
    public static final By TOTP_ERROR = testId("totp-error");
    public static final By SUCCESS = By.cssSelector(".alert-success");
    public static final By FAILURE = By.cssSelector(".alert-danger");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/profile");
    }

    public void changePassword(String current, String next) {
        type(testId("current-password"), current);
        type(testId("new-password"), next);
        type(testId("new-password-confirm"), next);
        click(testId("change-password-submit"));
    }
}
