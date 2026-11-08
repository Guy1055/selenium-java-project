package com.toolshop.tests.ui;

import com.toolshop.core.BaseUiTest;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.ForgotPasswordPage;
import com.toolshop.pages.LoginPage;
import com.toolshop.pages.NavBar;
import com.toolshop.pages.RegisterPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AuthTest extends BaseUiTest {
    private static final String WRONG_PASSWORD = "Wrong!Password1";

    @Test(description = "AUTH-01: a new user can register and is sent to the login page", groups = "sanity")
    public void register() {
        RegisterPage register = new RegisterPage(driver());
        register.open();

        register.register(TestData.newUser());

        expect().url("/auth/login");
    }

    @Test(description = "AUTH-02: a registered user can log in and sees the account page", groups = "sanity")
    public void login() {
        NavBar nav = new NavBar(driver());
        Customer customer = newCustomer();
        LoginPage login = new LoginPage(driver());
        login.open();

        login.login(customer.user().email(), customer.user().password());

        expect().url("/account");
        expect().containsText(nav.userMenu, customer.user().firstName());
    }

    @Test(description = "AUTH-03: a wrong password shows an error")
    public void wrongPassword() {
        Customer customer = newCustomer();
        LoginPage login = new LoginPage(driver());
        login.open();

        login.login(customer.user().email(), WRONG_PASSWORD);

        expect().containsText(login.error, "Invalid email or password");
        expect().url("/auth/login");
    }

    @Test(description = "AUTH-06: empty fields show required messages")
    public void loginRequired() {
        LoginPage login = new LoginPage(driver());
        login.open();

        login.click(login.submit);

        expect().containsText(login.emailError, "Email is required");
        expect().containsText(login.passwordError, "Password is required");
    }

    @Test(description = "AUTH-10: repeated failures lock the account")
    public void lockout() {
        // Throwaway user only; never run this against a shared demo account.
        Customer customer = newCustomer();
        WebDriver driver = driver();
        LoginPage login = new LoginPage(driver);
        login.open();

        expect().condition("the account to lock", () -> {
            login.login(customer.user().email(), WRONG_PASSWORD);
            return login.errors.stream().anyMatch(e -> e.getText().contains("locked"));
        });
    }

    @Test(description = "AUTH-07: signing out returns to the login page")
    public void logout() {
        signedInCustomer();
        NavBar nav = new NavBar(driver());
        open("/account");
        expect().containsText(nav.userMenu, "Test");

        nav.logOut();

        expect().url("/auth/login");
        expect().visible(nav.signIn);
    }

    @DataProvider
    public Object[][] accountPaths() {
        return new Object[][]{{"/account"}, {"/account/profile"}, {"/account/favorites"}, {"/account/invoices"}, {"/account/messages"}};
    }

    @Test(description = "AUTH-08: a guest opening an account page is sent to login", dataProvider = "accountPaths")
    public void guestRedirect(String path) {
        open(path);

        expect().url("/auth/login");
    }

    @Test(description = "AUTH-11: a customer cannot open the admin area")
    public void customerNotAdmin() {
        signedInCustomer();

        open("/admin/dashboard");

        expect().urlNot("/admin");
    }

    @DataProvider
    public Object[][] invalidRegistrations() {
        NewUser base = TestData.newUser();
        return new Object[][]{
                {"invalid email", base.withEmail("not-an-email"), "email-error"},
                {"short password", base.withPassword("Aa1!"), "password-error"},
                {"no symbols", base.withPassword("Abcdefgh12"), "password-error"},
                {"no digits", base.withPassword("Abcdefgh!!"), "password-error"},
                {"no capitals", base.withPassword("abcdefg1!"), "password-error"},
        };
    }

    @Test(description = "AUTH-04: invalid registration data is rejected", dataProvider = "invalidRegistrations")
    public void invalidRegistration(String label, NewUser user, String errorId) {
        RegisterPage register = new RegisterPage(driver());
        register.open();

        register.register(user);

        expect().visible(BasePage.testId(errorId));
        expect().url("/auth/register");
    }

    @Test(description = "AUTH-05: an email that is already registered is rejected")
    public void duplicateEmail() {
        Customer customer = newCustomer();
        RegisterPage register = new RegisterPage(driver());
        register.open();

        register.register(TestData.newUser().withEmail(customer.user().email()));

        expect().containsText(register.error, "already exists");
    }

    @Test(description = "AUTH-12: an empty form shows every required message")
    public void registerRequired() {
        RegisterPage register = new RegisterPage(driver());
        register.open();

        register.click(register.submit);

        for (String field : new String[]{"first-name", "last-name", "dob", "country", "postal_code", "house_number",
                "street", "city", "state", "phone", "email", "password"}) {
            expect().visible(BasePage.testId(field + "-error"));
        }
    }

    @Test(description = "AUTH-09: an empty email shows a required message")
    public void forgotRequired() {
        ForgotPasswordPage forgot = new ForgotPasswordPage(driver());
        forgot.open();

        forgot.click(forgot.submit);

        expect().containsText(forgot.emailError, "Email is required");
    }

    @Test(description = "AUTH-13: a registered email gets a confirmation")
    public void forgotConfirmation() {
        // Resets the password, so only ever use a user created by this test.
        Customer customer = newCustomer();
        ForgotPasswordPage forgot = new ForgotPasswordPage(driver());
        forgot.open();

        forgot.type(forgot.email, customer.user().email());
        forgot.click(forgot.submit);

        expect().visible(forgot.confirmation);
    }
}
