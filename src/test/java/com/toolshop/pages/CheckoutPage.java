package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.Map;

public class CheckoutPage extends BasePage {
    @FindBy(css = "[data-test='payment-method']")
    public WebElement paymentMethod;

    @FindBy(css = "[data-test='finish']")
    public WebElement finish;

    @FindBy(css = "[data-test='payment-success-message']")
    public WebElement paymentSuccess;

    @FindBy(css = "[data-test='proceed-3']")
    public WebElement addressContinue;

    @FindBy(css = "[data-test='guest-submit']")
    public WebElement guestSubmit;

    @FindBy(xpath = "//a[@role='tab'][normalize-space(.)='Continue as Guest']")
    private WebElement guestTab;

    @FindBy(css = "[data-test='proceed-1']")
    private WebElement proceedFromCart;

    @FindBy(css = "[data-test='proceed-2']")
    private WebElement proceedFromSignIn;

    @FindBy(css = "[data-test='proceed-2-guest']")
    private WebElement proceedAsGuest;

    @FindBy(css = "[data-test='street']")
    private WebElement street;

    @FindBy(css = "[data-test='city']")
    private WebElement city;

    @FindBy(css = "[data-test='state']")
    private WebElement state;

    @FindBy(css = "[data-test='country']")
    private WebElement country;

    @FindBy(css = "[data-test='house_number']")
    private WebElement houseNumber;

    @FindBy(css = "[data-test='postal_code']")
    private WebElement postalCode;

    @FindBy(css = "[data-test='guest-email']")
    private WebElement guestEmail;

    @FindBy(css = "[data-test='guest-first-name']")
    private WebElement guestFirstName;

    @FindBy(css = "[data-test='guest-last-name']")
    private WebElement guestLastName;

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public void proceedFromCart() {
        click(proceedFromCart);
    }

    /** Step 2 when already logged in. */
    public void proceedFromSignIn() {
        click(proceedFromSignIn);
    }

    public void fillAddressAndContinue() {
        type(street, "Test Street");
        type(city, "Amsterdam");
        type(state, "Noord-Holland");
        selectValue(country, "NL");
        type(houseNumber, "42");
        type(postalCode, "1011AB");
        click(addressContinue);
    }

    public void openGuestTab() {
        click(guestTab);
    }

    public void continueAsGuest(String email, String firstName, String lastName) {
        openGuestTab();
        type(guestEmail, email);
        type(guestFirstName, firstName);
        type(guestLastName, lastName);
        click(guestSubmit);
        click(proceedAsGuest);
    }

    public static By guestError(String field) {
        return testId("guest-" + field + "-error");
    }

    /** Fills the fields of the selected payment method; keys are the form's data-test values. */
    public void fillPaymentDetails(Map<String, String> fields) {
        fields.forEach((id, value) -> {
            By by = testId(id);
            waitVisible(by);
            WebElement field = driver.findElement(by);
            if ("select".equals(field.getTagName())) {
                new Select(field).selectByValue(value);
            } else {
                type(by, value);
            }
        });
    }

    public void pay(String method, Map<String, String> fields) {
        selectValue(paymentMethod, method);
        fillPaymentDetails(fields);
        click(finish);
    }
}
