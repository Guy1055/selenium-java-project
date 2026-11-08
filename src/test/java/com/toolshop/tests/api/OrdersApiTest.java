package com.toolshop.tests.api;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseApiTest;
import com.toolshop.core.KnownBug;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.toolshop.api.ToolshopApi.expectStatus;

@Test(groups = "api")
public class OrdersApiTest extends BaseApiTest {
    @Test(description = "API-CRT-01: full lifecycle of a cart", groups = {"api", "sanity"})
    public void cartLifecycle() {
        String productId = api.inStockProduct().id();
        String cartId = api.createCart();

        expectStatus(api.post("/carts/" + cartId, Map.of("product_id", productId, "quantity", 1)), 200);
        Response cart = api.get("/carts/" + cartId);
        Assert.assertEquals(cart.jsonPath().getList("cart_items").size(), 1);
        Assert.assertEquals(cart.jsonPath().getInt("cart_items[0].quantity"), 1);

        expectStatus(api.put("/carts/" + cartId + "/product/quantity", Map.of("product_id", productId, "quantity", 3)), 200);
        Assert.assertEquals(api.get("/carts/" + cartId).jsonPath().getInt("cart_items[0].quantity"), 3);

        expectStatus(api.delete("/carts/" + cartId + "/product/" + productId), 204);
        Assert.assertTrue(api.get("/carts/" + cartId).jsonPath().getList("cart_items").isEmpty());

        expectStatus(api.delete("/carts/" + cartId), 204);
        expectStatus(api.get("/carts/" + cartId), 404);
    }

    @Test(description = "API-CRT-02: adding the same product twice raises the quantity")
    public void cartMergesQuantity() {
        String productId = api.inStockProduct().id();
        String cartId = api.createCartWith(productId, 1);
        afterTest(() -> api.delete("/carts/" + cartId));

        expectStatus(api.post("/carts/" + cartId, Map.of("product_id", productId, "quantity", 2)), 200);

        Response cart = api.get("/carts/" + cartId);
        Assert.assertEquals(cart.jsonPath().getList("cart_items").size(), 1);
        Assert.assertEquals(cart.jsonPath().getInt("cart_items[0].quantity"), 3);
    }

    @Test(description = "API-CRT-03: an unknown cart is a 404")
    public void unknownCart() {
        expectStatus(api.get("/carts/does-not-exist"), 404);
    }

    @DataProvider
    public Object[][] paymentMethods() {
        return new Object[][]{
                {"cash-on-delivery", Map.of()},
                {"gift-card", Map.of("gift_card_number", "ABCD1234EFGH5678", "validation_code", "AB12")},
                {"bank-transfer", Map.of("bank_name", "Test Bank", "account_name", "Test User", "account_number", "12345678")},
        };
    }

    @Test(description = "API-PAY-01: each payment method is accepted", dataProvider = "paymentMethods")
    public void paymentAccepted(String method, Map<String, String> details) {
        Response response = expectStatus(api.post("/payment/check", Map.of("payment_method", method, "payment_details", details)), 200);

        Assert.assertEquals(response.jsonPath().getString("message"), "Payment was successful");
    }

    @Test(description = "API-PAY-02: an unknown payment method is rejected")
    @KnownBug("BUG-04: /payment/check answers 200 for a method that does not exist")
    public void paymentUnknownMethod() {
        int status = api.post("/payment/check", Map.of("payment_method", "nope", "payment_details", Map.of())).statusCode();

        Assert.assertTrue(status >= 400, "status was " + status);
    }

    @Test(description = "API-INV-01: a customer can place an order", groups = {"api", "sanity"})
    public void placeCustomerOrder() {
        Customer customer = newCustomer();

        Response invoice = placeOrder(customer);

        Assert.assertTrue(invoice.jsonPath().getString("invoice_number").startsWith("INV-"));
        Assert.assertTrue(invoice.jsonPath().getDouble("total") > 0);
        Assert.assertEquals(invoice.jsonPath().getString("user_id"), customer.id());
    }

    @Test(description = "API-INV-02: the order shows in the customer list and details")
    public void orderListedAndDetails() {
        Customer customer = newCustomer();
        Response invoice = placeOrder(customer);
        String id = invoice.jsonPath().getString("id");

        Response list = customer.api().get("/invoices");
        Assert.assertEquals(list.jsonPath().getList("data").size(), 1);
        Assert.assertEquals(list.jsonPath().getString("data[0].id"), id);

        Response details = customer.api().get("/invoices/" + id);
        Assert.assertEquals(details.jsonPath().getString("invoice_number"), invoice.jsonPath().getString("invoice_number"));
        Assert.assertEquals(details.jsonPath().getString("status"), "AWAITING_FULFILLMENT");
    }

    @Test(description = "API-INV-03: a guest can place an order")
    public void guestOrder() {
        String cartId = api.createCartWith(api.inStockProduct().id(), 1);
        String email = "qa.guest." + TestData.uniqueId() + "@example.com";
        Map<String, Object> body = new LinkedHashMap<>(api.billingFor(cartId));
        body.put("email", email);
        body.put("guest_email", email);
        body.put("first_name", "Test");
        body.put("guest_first_name", "Test");
        body.put("last_name", "Guest");
        body.put("guest_last_name", "Guest");

        Response response = expectStatus(api.post("/invoices/guest", body), 201);

        Assert.assertNull(response.jsonPath().get("user_id"));
    }

    @Test(description = "API-INV-04: a city that does not match the postcode is rejected")
    public void cityMustMatchPostcode() {
        Customer customer = newCustomer();
        String cartId = api.createCartWith(api.inStockProduct().id(), 1);
        afterTest(() -> api.delete("/carts/" + cartId));
        Map<String, Object> billing = new LinkedHashMap<>(api.billingFor(cartId));
        billing.put("billing_city", "Definitely Not A Match");

        Response response = expectStatus(customer.api().post("/invoices", billing), 422);

        Assert.assertNotNull(response.jsonPath().get("billing_country"));
    }

    @Test(description = "API-INV-05: missing fields are rejected")
    public void invoiceRequired() {
        expectStatus(newCustomer().api().post("/invoices", Map.of()), 422);
    }

    @Test(description = "API-INV-06: invoices need a token")
    public void invoicesNeedToken() {
        expectStatus(api.get("/invoices"), 401);
        expectStatus(api.post("/invoices", Map.of()), 401);
    }

    @Test(description = "API-INV-07: a customer cannot read another customer's invoice")
    public void invoiceIsPrivate() {
        String id = placeOrder(newCustomer()).jsonPath().getString("id");
        Customer stranger = newCustomer();

        int status = stranger.api().get("/invoices/" + id).statusCode();

        Assert.assertTrue(status == 403 || status == 404, "status was " + status);
    }

    @Test(description = "API-INV-08: an admin can search and update status")
    public void invoiceSearchAndStatus() {
        Response invoice = placeOrder(newCustomer());
        String id = invoice.jsonPath().getString("id");

        List<String> found = adminApi().get("/invoices/search", Map.of("q", invoice.jsonPath().getString("invoice_number")))
                .jsonPath().getList("data.id");
        Assert.assertTrue(found.contains(id));

        expectStatus(adminApi().put("/invoices/" + id + "/status", Map.of("status", "SHIPPED", "status_message", "Shipped by QA")), 200);
        Assert.assertEquals(adminApi().get("/invoices/" + id).jsonPath().getString("status"), "SHIPPED");
    }

    @Test(description = "API-INV-09: a status message that is too short is rejected")
    public void statusMessageTooShort() {
        String id = placeOrder(newCustomer()).jsonPath().getString("id");

        expectStatus(adminApi().put("/invoices/" + id + "/status", Map.of("status", "SHIPPED", "status_message", "abc")), 422);
    }

    @Test(description = "API-INV-10: the PDF has not been generated right after the order")
    public void pdfNotReady() {
        Customer customer = newCustomer();
        String number = placeOrder(customer).jsonPath().getString("invoice_number");

        Response response = expectStatus(customer.api().get("/invoices/" + number + "/download-pdf-status"), 400);

        Assert.assertEquals(response.jsonPath().getString("status"), "NOT_INITIATED");
    }

    @Test(description = "API-INV-11: an admin can replace the billing details")
    @KnownBug("BUG-05: PUT /invoices/{id} answers 404 for an invoice that exists")
    public void invoicePut() {
        String id = placeOrder(newCustomer()).jsonPath().getString("id");
        Map<String, Object> billing = new LinkedHashMap<>(api.billingFor("unused"));
        billing.put("billing_street", "Replaced Street");

        expectStatus(adminApi().put("/invoices/" + id, billing), 200);
    }

    @Test(description = "API-INV-12: an admin can patch the billing street")
    @KnownBug("BUG-05: PATCH /invoices/{id} answers 404 for an invoice that exists")
    public void invoicePatch() {
        String id = placeOrder(newCustomer()).jsonPath().getString("id");

        expectStatus(adminApi().patch("/invoices/" + id, Map.of("billing_street", "Patched Street")), 200);
    }

    @Test(description = "API-INV-13: the PDF cannot be downloaded before it exists")
    public void pdfDownloadBeforeReady() {
        Customer customer = newCustomer();
        String number = placeOrder(customer).jsonPath().getString("invoice_number");

        Assert.assertTrue(customer.api().get("/invoices/" + number + "/download-pdf").statusCode() >= 400);
    }

    @Test(description = "API-FAV-01: full lifecycle of a favorite")
    public void favoriteLifecycle() {
        Customer customer = newCustomer();
        String productId = api.inStockProduct().id();

        String id = expectStatus(customer.api().post("/favorites", Map.of("product_id", productId)), 201).jsonPath().getString("id");

        List<String> ids = customer.api().get("/favorites").jsonPath().getList("id");
        Assert.assertTrue(ids.contains(id));
        expectStatus(customer.api().get("/favorites/" + id), 200);
        expectStatus(customer.api().delete("/favorites/" + id), 204);
        Assert.assertTrue(customer.api().get("/favorites").jsonPath().getList("$").isEmpty());
    }

    @Test(description = "API-FAV-02: favorites need a token")
    public void favoritesNeedToken() {
        expectStatus(api.get("/favorites"), 401);
    }

    @Test(description = "API-FAV-03: an unknown product is rejected")
    public void favoriteUnknownProduct() {
        expectStatus(newCustomer().api().post("/favorites", Map.of("product_id", "does-not-exist")), 422);
    }

    @Test(description = "API-MSG-01: a guest can send a message")
    public void guestMessage() {
        Response response = expectStatus(api.post("/messages", Map.of(
                "subject", "webmaster",
                "message", TestData.CONTACT_MESSAGE,
                "name", "Test User",
                "email", "qa.contact@example.com")), 200);

        Assert.assertEquals(response.jsonPath().getString("status"), "NEW");
    }

    @Test(description = "API-MSG-02: subject and message are required")
    public void messageRequired() {
        expectStatus(api.post("/messages", Map.of("name", "Test")), 422);
    }

    @Test(description = "API-MSG-03: a message over 250 characters is rejected")
    public void messageTooLong() {
        expectStatus(api.post("/messages", Map.of(
                "subject", "webmaster", "message", "x".repeat(251), "name", "T", "email", "qa@example.com")), 422);
    }

    private File tempText(String content) throws IOException {
        File file = Files.createTempFile("attachment", ".txt").toFile();
        Files.writeString(file.toPath(), content);
        afterTest(file::delete);
        return file;
    }

    @Test(description = "API-MSG-04: an empty text attachment is accepted")
    public void emptyAttachment() throws IOException {
        Customer customer = newCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");

        expectStatus(customer.api().attachFile(id, tempText("")), 200);
    }

    @Test(description = "API-MSG-09: a non-empty attachment is rejected")
    public void nonEmptyAttachment() throws IOException {
        Customer customer = newCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");

        Assert.assertTrue(customer.api().attachFile(id, tempText("content")).statusCode() >= 400);
    }

    @Test(description = "API-MSG-05: reply, then read it back")
    public void replyAndRead() {
        Customer customer = newCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");

        expectStatus(customer.api().post("/messages/" + id + "/reply", Map.of("message", "A reply from QA")), 201);

        Response message = customer.api().get("/messages/" + id);
        Assert.assertEquals(message.jsonPath().getList("replies").size(), 1);
        Assert.assertEquals(message.jsonPath().getString("replies[0].message"), "A reply from QA");
    }

    @Test(description = "API-MSG-06: an admin can change the status")
    public void messageStatus() {
        String id = sendMessage(newCustomer()).jsonPath().getString("id");

        expectStatus(adminApi().put("/messages/" + id + "/status", Map.of("status", "IN_PROGRESS")), 200);

        Assert.assertEquals(adminApi().get("/messages/" + id).jsonPath().getString("status"), "IN_PROGRESS");
    }

    @Test(description = "API-MSG-07: the message list needs a token")
    public void messagesNeedToken() {
        expectStatus(api.get("/messages"), 401);
    }

    @Test(description = "API-MSG-08: the list shows only the customer's own messages")
    public void onlyOwnMessages() {
        Customer customer = newCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");

        Assert.assertEquals(customer.api().get("/messages").jsonPath().getList("data.id"), List.of(id));
    }
}
