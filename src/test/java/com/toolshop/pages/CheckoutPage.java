package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.Map;

public class CheckoutPage extends BasePage {
    public static final By PAYMENT_METHOD = testId("payment-method");
    public static final By FINISH = testId("finish");
    public static final By PAYMENT_SUCCESS = testId("payment-success-message");
    public static final By ADDRESS_CONTINUE = testId("proceed-3");
    public static final By GUEST_SUBMIT = testId("guest-submit");
    private static final By GUEST_TAB = By.xpath("//a[@role='tab'][normalize-space(.)='Continue as Guest']");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public void proceedFromCart() {
        click(testId("proceed-1"));
    }

    /** Step 2 when already logged in. */
    public void proceedFromSignIn() {
        click(testId("proceed-2"));
    }

    public void fillAddressAndContinue() {
        type(testId("street"), "Test Street");
        type(testId("city"), "Amsterdam");
        type(testId("state"), "Noord-Holland");
        selectValue(testId("country"), "NL");
        type(testId("house_number"), "42");
        type(testId("postal_code"), "1011AB");
        click(ADDRESS_CONTINUE);
    }

    public void openGuestTab() {
        click(GUEST_TAB);
    }

    public void continueAsGuest(String email, String firstName, String lastName) {
        openGuestTab();
        type(testId("guest-email"), email);
        type(testId("guest-first-name"), firstName);
        type(testId("guest-last-name"), lastName);
        click(GUEST_SUBMIT);
        click(testId("proceed-2-guest"));
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
        selectValue(PAYMENT_METHOD, method);
        fillPaymentDetails(fields);
        click(FINISH);
    }
}
