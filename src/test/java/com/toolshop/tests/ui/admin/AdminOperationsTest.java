package com.toolshop.tests.ui.admin;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.MessagesPage;
import com.toolshop.pages.admin.AdminFormPage;
import com.toolshop.pages.admin.AdminListPage;
import com.toolshop.pages.admin.AdminMessagePage;
import com.toolshop.pages.admin.AdminOrderPage;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class AdminOperationsTest extends BaseUiTest {
    @Test(description = "ADM-40: an admin can search orders by invoice number")
    public void searchOrders() {
        Customer customer = newCustomer();
        String number = placeOrder(customer).jsonPath().getString("invoice_number");
        signInAsAdmin();
        AdminListPage list = new AdminListPage(driver(), "order", "/admin/orders");
        list.open();

        list.search(number);

        expect().count(list.rows, 1);
        expect().containsText(list.row, number);
    }

    @Test(description = "ADM-41: an admin can update the status of an order")
    public void updateOrderStatus() {
        Customer customer = newCustomer();
        Response invoice = placeOrder(customer);
        String id = invoice.jsonPath().getString("id");
        signInAsAdmin();
        AdminOrderPage order = new AdminOrderPage(driver());
        order.open(id);
        expect().value(order.invoiceNumber, invoice.jsonPath().getString("invoice_number"));

        order.selectValue(order.status, "SHIPPED");
        order.click(order.updateStatus);

        expect().visible(order.success);
        Assert.assertEquals(adminApi().get("/invoices/" + id).jsonPath().getString("status"), "SHIPPED");
    }

    @Test(description = "ADM-50: an admin can add a user")
    public void addUser() {
        signInAsAdmin();
        String email = "qa." + TestData.uniqueId() + "@example.com";
        afterTest(() -> {
            List<Map<String, String>> found = adminApi().get("/users/search", Map.of("q", email)).jsonPath().getList("data");
            found.stream().filter(u -> email.equals(u.get("email"))).forEach(u -> adminApi().delete("/users/" + u.get("id")));
        });
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/users/add");

        form.fillUser("Test", "Admin Added", email, "Qa!" + TestData.uniqueId() + "xZ");
        form.submit("user");

        expect().visible(form.success);
    }

    @Test(description = "ADM-51: an admin can edit a user")
    public void editUser() {
        Customer customer = newCustomer();
        signInAsAdmin();
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/users/edit/" + customer.id());
        expect().value(form.email, customer.user().email());

        form.type(form.firstName, "Edited");
        form.submit("user");

        expect().visible(form.success);
        Assert.assertEquals(adminApi().get("/users/" + customer.id()).jsonPath().getString("first_name"), "Edited");
    }

    @Test(description = "ADM-52: an admin can find and delete a user")
    public void deleteUser() {
        NewUser user = TestData.newUser();
        String id = api.register(user);
        signInAsAdmin();
        AdminListPage list = new AdminListPage(driver(), "user", "/admin/users");
        list.open();

        list.search(user.email());
        expect().count(list.rows, 1);
        list.click(list.deleteButton(id));

        expect().visible(BasePage.alertWith("deleted"));
        ToolshopApi.expectStatus(adminApi().get("/users/" + id), 404);
    }

    @Test(description = "ADM-60: an admin can reply to a message")
    public void replyToMessage() {
        String id = sendMessage(newCustomer()).jsonPath().getString("id");
        signInAsAdmin();
        AdminMessagePage message = new AdminMessagePage(driver());
        message.open(id, TestData.CONTACT_MESSAGE);
        String reply = "An admin answer from the automated QA suite, please ignore.";

        message.type(message.replyBox, reply);
        message.click(message.replySubmit);

        expect().visible(MessagesPage.textOnPage(reply));
    }

    @Test(description = "ADM-61: an admin can change the message status")
    public void changeMessageStatus() {
        String id = sendMessage(newCustomer()).jsonPath().getString("id");
        signInAsAdmin();
        AdminMessagePage message = new AdminMessagePage(driver());
        message.open(id, TestData.CONTACT_MESSAGE);

        message.selectValue(message.status, "RESOLVED");

        expect().condition("the status to reach the API",
                () -> "RESOLVED".equals(adminApi().get("/messages/" + id).jsonPath().getString("status")));
    }

    @Test(description = "ADM-62: the message list opens")
    public void messageList() {
        signInAsAdmin();

        open("/admin/messages");

        expect().visible(BasePage.testId("page-title"));
        expect().visible(By.cssSelector("[data-test^='message-details-']"));
    }
}
