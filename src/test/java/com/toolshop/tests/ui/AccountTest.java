package com.toolshop.tests.ui;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.data.TestData;
import com.toolshop.pages.AccountPage;
import com.toolshop.pages.FavoritesPage;
import com.toolshop.pages.InvoicesPage;
import com.toolshop.pages.MessagesPage;
import com.toolshop.pages.ProfilePage;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class AccountTest extends BaseUiTest {
    @DataProvider
    public Object[][] sections() {
        return new Object[][]{
                {"favorites", "/account/favorites", "Favorites"},
                {"profile", "/account/profile", "Profile"},
                {"invoices", "/account/invoices", "Invoices"},
                {"messages", "/account/messages", "Messages"},
        };
    }

    @Test(description = "ACC-01: the overview links to each section", dataProvider = "sections")
    public void overviewLinks(String section, String path, String title) {
        signedInCustomer();
        AccountPage account = new AccountPage(driver());
        account.open();
        expect().text(AccountPage.TITLE, "My account");

        account.click(AccountPage.link(section));

        expect().url(path + "$");
        expect().containsText(AccountPage.TITLE, title);
    }

    @Test(description = "ACC-02: the profile shows the registered details")
    public void profileDetails() {
        Customer customer = signedInCustomer();

        new ProfilePage(driver()).open();

        expect().value(ProfilePage.FIRST_NAME, customer.user().firstName());
        expect().value(ProfilePage.LAST_NAME, customer.user().lastName());
        expect().value(ProfilePage.EMAIL, customer.user().email());
    }

    private ProfilePage openLoadedProfile(Customer customer) {
        ProfilePage profile = new ProfilePage(driver());
        profile.open();
        expect().value(ProfilePage.FIRST_NAME, customer.user().firstName());
        return profile;
    }

    @Test(description = "ACC-03: updating the profile saves the new value")
    public void updateProfile() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);

        profile.type(ProfilePage.FIRST_NAME, "Renamed");
        profile.click(ProfilePage.UPDATE_SUBMIT);

        expect().containsText(ProfilePage.SUCCESS, "successfully updated");
        Assert.assertEquals(customer.api().get("/users/me").jsonPath().getString("first_name"), "Renamed");
    }

    @Test(description = "ACC-04: changing the password lets the user log in with the new one")
    public void changePassword() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);
        String next = customer.user().password() + "N1";

        profile.changePassword(customer.user().password(), next);

        expect().containsText(ProfilePage.SUCCESS, "successfully updated");
        ToolshopApi.expectStatus(api.post("/users/login", Map.of("email", customer.user().email(), "password", next)), 200);
    }

    @Test(description = "ACC-05: a wrong current password is rejected")
    public void wrongCurrentPassword() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);

        profile.changePassword("Not!TheCurrent1", customer.user().password() + "N1");

        expect().containsText(ProfilePage.FAILURE, "current password");
    }

    @Test(description = "ACC-06: a wrong two-factor code is rejected")
    public void wrongTotp() {
        signedInCustomer();
        ProfilePage profile = new ProfilePage(driver());
        profile.open();
        expect().visible(ProfilePage.TOTP_SECRET);

        profile.type(ProfilePage.TOTP_CODE, "000000");
        profile.click(ProfilePage.TOTP_VERIFY);

        expect().visible(ProfilePage.TOTP_ERROR);
    }

    @Test(description = "ACC-07: a new customer has no favorites")
    public void noFavorites() {
        signedInCustomer();

        new FavoritesPage(driver()).open();

        expect().containsText(FavoritesPage.TITLE, "Favorites");
        expect().count(FavoritesPage.NAMES, 0);
    }

    @Test(description = "ACC-08: a favorite can be listed and removed")
    public void listAndRemoveFavorite() {
        Customer customer = signedInCustomer();
        ToolshopApi.Product product = api.inStockProduct();
        ToolshopApi.expectStatus(customer.api().post("/favorites", Map.of("product_id", product.id())), 201);
        FavoritesPage favorites = new FavoritesPage(driver());
        favorites.open();
        expect().text(FavoritesPage.NAMES, product.name());

        favorites.click(FavoritesPage.DELETE);

        expect().count(FavoritesPage.NAMES, 0);
    }

    @Test(description = "ACC-09: a new customer has an empty invoice list")
    public void noInvoices() {
        signedInCustomer();

        new InvoicesPage(driver()).open();

        expect().containsText(InvoicesPage.TITLE, "Invoices");
        expect().count(InvoicesPage.ROWS, 0);
    }

    @Test(description = "ACC-10: an order appears in the list and opens its details")
    public void invoiceDetails() {
        Customer customer = signedInCustomer();
        Response invoice = placeOrder(customer);
        String number = invoice.jsonPath().getString("invoice_number");
        InvoicesPage invoices = new InvoicesPage(driver());

        invoices.open();
        expect().count(InvoicesPage.ROWS, 1);
        expect().containsText(InvoicesPage.ROWS, number);

        invoices.openDetails(invoice.jsonPath().getString("id"));

        expect().value(InvoicesPage.INVOICE_NUMBER, number);
        expect().visible(InvoicesPage.DOWNLOAD);
    }

    @Test(description = "ACC-11: a sent message appears in the list")
    public void messageListed() {
        Customer customer = signedInCustomer();
        sendMessage(customer);

        new MessagesPage(driver()).open();

        expect().count(MessagesPage.ROWS, 1);
        expect().containsText(MessagesPage.ROWS, "webmaster");
    }

    @Test(description = "ACC-12: a customer can reply on the message page")
    public void reply() {
        Customer customer = signedInCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");
        MessagesPage messages = new MessagesPage(driver());
        messages.openDetails(id, TestData.CONTACT_MESSAGE);
        String text = "A follow-up from the automated QA suite, please ignore this reply.";

        messages.type(MessagesPage.REPLY_BOX, text);
        messages.click(MessagesPage.REPLY_SUBMIT);

        expect().visible(MessagesPage.textOnPage(text));
    }
}
