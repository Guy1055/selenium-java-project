package com.toolshop.tests.ui;

import com.toolshop.core.BaseUiTest;
import com.toolshop.pages.CartPage;
import com.toolshop.pages.CheckoutPage;
import com.toolshop.pages.ProductPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class CheckoutTest extends BaseUiTest {
    private CheckoutPage reachSignInStep() {
        new ProductPage(driver()).addProductToCart(api.inStockProduct().id());
        new CartPage(driver()).open();
        CheckoutPage checkout = new CheckoutPage(driver());
        checkout.proceedFromCart();
        return checkout;
    }

    private CheckoutPage reachPaymentStepAsCustomer() {
        signedInCustomer();
        CheckoutPage checkout = reachSignInStep();
        checkout.proceedFromSignIn();
        checkout.fillAddressAndContinue();
        return checkout;
    }

    private static Map<String, String> fields(String... keysAndValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    @Test(description = "CHK-01: a logged-in user can complete an order with cash on delivery", groups = "sanity")
    public void loggedInOrder() {
        CheckoutPage checkout = reachPaymentStepAsCustomer();

        checkout.pay("cash-on-delivery", Map.of());

        expect().visible(checkout.paymentSuccess);
    }

    @Test(description = "CHK-02: a guest can complete an order")
    public void guestOrder() {
        CheckoutPage checkout = reachSignInStep();

        checkout.continueAsGuest("qa.guest@example.com", "Test", "Guest");
        checkout.fillAddressAndContinue();
        checkout.pay("cash-on-delivery", Map.of());

        expect().visible(checkout.paymentSuccess);
    }

    @Test(description = "CHK-03: the address step cannot continue while required fields are empty")
    public void addressRequired() {
        CheckoutPage checkout = reachSignInStep();

        checkout.continueAsGuest("qa.guest@example.com", "Test", "Guest");

        expect().disabled(checkout.addressContinue);
    }

    @Test(description = "CHK-06: guest details are validated")
    public void guestValidation() {
        CheckoutPage checkout = reachSignInStep();
        checkout.openGuestTab();

        checkout.click(checkout.guestSubmit);

        expect().visible(CheckoutPage.guestError("email"));
        expect().visible(CheckoutPage.guestError("first-name"));
        expect().visible(CheckoutPage.guestError("last-name"));
    }

    @DataProvider
    public Object[][] paymentMethods() {
        return new Object[][]{
                {"bank-transfer", fields("bank_name", "Test Bank", "account_name", "Test User", "account_number", "12345678")},
                {"cash-on-delivery", fields()},
                {"credit-card", fields("credit_card_number", "1111-1111-1111-1111", "expiration_date", "12/2035",
                        "cvv", "123", "card_holder_name", "Test User")},
                {"buy-now-pay-later", fields("monthly_installments", "6")},
                {"gift-card", fields("gift_card_number", "ABCD1234EFGH5678", "validation_code", "AB12")},
        };
    }

    @Test(description = "CHK-04: a customer can pay with each method", dataProvider = "paymentMethods")
    public void payWith(String method, Map<String, String> details) {
        CheckoutPage checkout = reachPaymentStepAsCustomer();

        checkout.pay(method, details);

        expect().visible(checkout.paymentSuccess);
    }

    @DataProvider
    public Object[][] invalidPayments() {
        return new Object[][]{
                {"credit-card", fields("credit_card_number", "1234", "expiration_date", "12/2035", "cvv", "123", "card_holder_name", "Test User")},
                {"credit-card", fields("credit_card_number", "1111-1111-1111-1111", "expiration_date", "01/2020", "cvv", "123", "card_holder_name", "Test User")},
                {"gift-card", fields("gift_card_number", "ABC", "validation_code", "AB12")},
                {"bank-transfer", fields("bank_name", "Test Bank", "account_name", "Test User", "account_number", "abc")},
        };
    }

    @Test(description = "CHK-05: invalid payment details block payment", dataProvider = "invalidPayments")
    public void invalidPayment(String method, Map<String, String> details) {
        CheckoutPage checkout = reachPaymentStepAsCustomer();

        checkout.selectValue(checkout.paymentMethod, method);
        checkout.fillPaymentDetails(details);

        expect().disabled(checkout.finish);
    }
}
