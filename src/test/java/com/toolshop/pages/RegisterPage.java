package com.toolshop.pages;

import com.toolshop.data.TestData.NewUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {
    public static final By SUBMIT = testId("register-submit");
    public static final By ERROR = testId("register-error");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/register");
        waitVisible(SUBMIT);
    }

    public void register(NewUser user) {
        type(testId("first-name"), user.firstName());
        type(testId("last-name"), user.lastName());
        type(testId("dob"), "1990-01-15");
        selectValue(testId("country"), "NL");
        type(testId("postal_code"), "1011AB");
        type(testId("house_number"), "42");
        type(testId("street"), "Test Street");
        type(testId("city"), "Amsterdam");
        type(testId("state"), "Noord-Holland");
        type(testId("phone"), "0612345678");
        type(testId("email"), user.email());
        type(testId("password"), user.password());
        click(SUBMIT);
    }
}
