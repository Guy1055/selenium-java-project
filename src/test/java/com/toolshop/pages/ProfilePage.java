package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProfilePage extends BasePage {
    @FindBy(css = "[data-test='first-name']")
    public WebElement firstName;

    @FindBy(css = "[data-test='last-name']")
    public WebElement lastName;

    @FindBy(css = "[data-test='email']")
    public WebElement email;

    @FindBy(css = "[data-test='update-profile-submit']")
    public WebElement updateSubmit;

    @FindBy(css = "[data-test='totp-secret']")
    public WebElement totpSecret;

    @FindBy(css = "[data-test='totp-code']")
    public WebElement totpCode;

    @FindBy(css = "[data-test='verify-totp']")
    public WebElement totpVerify;

    @FindBy(css = "[data-test='totp-error']")
    public WebElement totpError;

    @FindBy(css = ".alert-success")
    public WebElement success;

    @FindBy(css = ".alert-danger")
    public WebElement failure;

    @FindBy(css = "[data-test='current-password']")
    private WebElement currentPassword;

    @FindBy(css = "[data-test='new-password']")
    private WebElement newPassword;

    @FindBy(css = "[data-test='new-password-confirm']")
    private WebElement newPasswordConfirm;

    @FindBy(css = "[data-test='change-password-submit']")
    private WebElement changePasswordSubmit;

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/account/profile");
    }

    public void changePassword(String current, String next) {
        type(currentPassword, current);
        type(newPassword, next);
        type(newPasswordConfirm, next);
        click(changePasswordSubmit);
    }
}
