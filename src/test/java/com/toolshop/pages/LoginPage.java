package com.toolshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LoginPage extends BasePage {
    @FindBy(css = "[data-test='email']")
    public WebElement email;

    @FindBy(css = "[data-test='password']")
    public WebElement password;

    @FindBy(css = "[data-test='login-submit']")
    public WebElement submit;

    @FindBy(css = "[data-test='login-error']")
    public WebElement error;

    @FindBy(css = "[data-test='login-error']")
    public List<WebElement> errors;

    @FindBy(css = "[data-test='email-error']")
    public WebElement emailError;

    @FindBy(css = "[data-test='password-error']")
    public WebElement passwordError;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/login");
        waitVisible(submit);
    }

    public void login(String email, String password) {
        type(this.email, email);
        type(this.password, password);
        click(submit);
    }
}
