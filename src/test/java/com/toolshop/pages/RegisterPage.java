package com.toolshop.pages;

import com.toolshop.data.TestData.NewUser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RegisterPage extends BasePage {
    @FindBy(css = "[data-test='register-submit']")
    public WebElement submit;

    @FindBy(css = "[data-test='register-error']")
    public WebElement error;

    @FindBy(css = "[data-test='first-name']")
    private WebElement firstName;

    @FindBy(css = "[data-test='last-name']")
    private WebElement lastName;

    @FindBy(css = "[data-test='dob']")
    private WebElement dob;

    @FindBy(css = "[data-test='country']")
    private WebElement country;

    @FindBy(css = "[data-test='postal_code']")
    private WebElement postalCode;

    @FindBy(css = "[data-test='house_number']")
    private WebElement houseNumber;

    @FindBy(css = "[data-test='street']")
    private WebElement street;

    @FindBy(css = "[data-test='city']")
    private WebElement city;

    @FindBy(css = "[data-test='state']")
    private WebElement state;

    @FindBy(css = "[data-test='phone']")
    private WebElement phone;

    @FindBy(css = "[data-test='email']")
    private WebElement email;

    @FindBy(css = "[data-test='password']")
    private WebElement password;

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/auth/register");
        waitVisible(submit);
    }

    public void register(NewUser user) {
        type(firstName, user.firstName());
        type(lastName, user.lastName());
        type(dob, "1990-01-15");
        selectValue(country, "NL");
        type(postalCode, "1011AB");
        type(houseNumber, "42");
        type(street, "Test Street");
        type(city, "Amsterdam");
        type(state, "Noord-Holland");
        type(phone, "0612345678");
        type(email, user.email());
        type(password, user.password());
        click(submit);
    }
}
