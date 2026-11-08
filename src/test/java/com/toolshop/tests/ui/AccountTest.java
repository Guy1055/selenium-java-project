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
        expect().text(account.title, "My account");

        account.click(AccountPage.link(section));

        expect().url(path + "$");
        expect().containsText(account.title, title);
    }

    @Test(description = "ACC-02: the profile shows the registered details")
    public void profileDetails() {
        Customer customer = signedInCustomer();

        ProfilePage profile = new ProfilePage(driver());
        profile.open();

        expect().value(profile.firstName, customer.user().firstName());
        expect().value(profile.lastName, customer.user().lastName());
        expect().value(profile.email, customer.user().email());
    }

    private ProfilePage openLoadedProfile(Customer customer) {
        ProfilePage profile = new ProfilePage(driver());
        profile.open();
        expect().value(profile.firstName, customer.user().firstName());
        return profile;
    }

    @Test(description = "ACC-03: updating the profile saves the new value")
    public void updateProfile() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);

        profile.type(profile.firstName, "Renamed");
        profile.click(profile.updateSubmit);

        expect().containsText(profile.success, "successfully updated");
        Assert.assertEquals(customer.api().get("/users/me").jsonPath().getString("first_name"), "Renamed");
    }

    @Test(description = "ACC-04: changing the password lets the user log in with the new one")
    public void changePassword() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);
        String next = customer.user().password() + "N1";

        profile.changePassword(customer.user().password(), next);

        expect().containsText(profile.success, "successfully updated");
        ToolshopApi.expectStatus(api.post("/users/login", Map.of("email", customer.user().email(), "password", next)), 200);
    }

    @Test(description = "ACC-05: a wrong current password is rejected")
    public void wrongCurrentPassword() {
        Customer customer = signedInCustomer();
        ProfilePage profile = openLoadedProfile(customer);

        profile.changePassword("Not!TheCurrent1", customer.user().password() + "N1");

        expect().containsText(profile.failure, "current password");
    }

    @Test(description = "ACC-06: a wrong two-factor code is rejected")
    public void wrongTotp() {
        signedInCustomer();
        ProfilePage profile = new ProfilePage(driver());
        profile.open();
        expect().visible(profile.totpSecret);

        profile.type(profile.totpCode, "000000");
        profile.click(profile.totpVerify);

        expect().visible(profile.totpError);
    }

    @Test(description = "ACC-07: a new customer has no favorites")
    public void noFavorites() {
        signedInCustomer();

        FavoritesPage favorites = new FavoritesPage(driver());
        favorites.open();

        expect().containsText(favorites.title, "Favorites");
        expect().count(favorites.names, 0);
    }

    @Test(description = "ACC-08: a favorite can be listed and removed")
    public void listAndRemoveFavorite() {
        Customer customer = signedInCustomer();
        ToolshopApi.Product product = api.inStockProduct();
        ToolshopApi.expectStatus(customer.api().post("/favorites", Map.of("product_id", product.id())), 201);
        FavoritesPage favorites = new FavoritesPage(driver());
        favorites.open();
        expect().text(favorites.name, product.name());

        favorites.click(favorites.delete);

        expect().count(favorites.names, 0);
    }

    @Test(description = "ACC-09: a new customer has an empty invoice list")
    public void noInvoices() {
        signedInCustomer();

        InvoicesPage invoices = new InvoicesPage(driver());
        invoices.open();

        expect().containsText(invoices.title, "Invoices");
        expect().count(invoices.rows, 0);
    }

    @Test(description = "ACC-10: an order appears in the list and opens its details")
    public void invoiceDetails() {
        Customer customer = signedInCustomer();
        Response invoice = placeOrder(customer);
        String number = invoice.jsonPath().getString("invoice_number");
        InvoicesPage invoices = new InvoicesPage(driver());

        invoices.open();
        expect().count(invoices.rows, 1);
        expect().containsText(invoices.row, number);

        invoices.openDetails(invoice.jsonPath().getString("id"));

        expect().value(invoices.invoiceNumber, number);
        expect().visible(invoices.download);
    }

    @Test(description = "ACC-11: a sent message appears in the list")
    public void messageListed() {
        Customer customer = signedInCustomer();
        sendMessage(customer);

        MessagesPage messages = new MessagesPage(driver());
        messages.open();

        expect().count(messages.rows, 1);
        expect().containsText(messages.row, "webmaster");
    }

    @Test(description = "ACC-12: a customer can reply on the message page")
    public void reply() {
        Customer customer = signedInCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");
        MessagesPage messages = new MessagesPage(driver());
        messages.openDetails(id, TestData.CONTACT_MESSAGE);
        String text = "A follow-up from the automated QA suite, please ignore this reply.";

        messages.type(messages.replyBox, text);
        messages.click(messages.replySubmit);

        expect().visible(MessagesPage.textOnPage(text));
    }
}
