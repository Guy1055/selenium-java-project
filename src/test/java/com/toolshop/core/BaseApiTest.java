package com.toolshop.core;

import com.toolshop.api.ToolshopApi;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/** Shared API helpers: a throwaway customer per test, one admin token, and per-test cleanup. */
public abstract class BaseApiTest {
    public record Customer(String id, String token, NewUser user, ToolshopApi api) {
    }

    protected final ToolshopApi api = new ToolshopApi();
    private static volatile String adminToken;
    private final ThreadLocal<Deque<Runnable>> cleanups = ThreadLocal.withInitial(ArrayDeque::new);

    protected ToolshopApi adminApi() {
        if (adminToken == null) {
            synchronized (BaseApiTest.class) {
                if (adminToken == null) {
                    adminToken = api.login(Env.ADMIN_EMAIL, Env.ADMIN_PASSWORD);
                }
            }
        }
        return api.withToken(adminToken);
    }

    protected String adminToken() {
        adminApi();
        return adminToken;
    }

    /** Runs after the test, even when it fails. Last registered runs first. */
    protected void afterTest(Runnable cleanup) {
        cleanups.get().push(cleanup);
    }

    /** A customer registered for this test only. A user with orders cannot be deleted, which is fine. */
    protected Customer newCustomer() {
        NewUser user = TestData.newUser();
        String id = api.register(user);
        afterTest(() -> adminApi().delete("/users/" + id));
        String token = api.login(user.email(), user.password());
        return new Customer(id, token, user, api.withToken(token));
    }

    /** Places an order for the customer through the API and returns the response. */
    protected Response placeOrder(Customer customer) {
        String cartId = api.createCartWith(api.inStockProduct().id(), 1);
        return ToolshopApi.expectStatus(customer.api().post("/invoices", api.billingFor(cartId)), 201);
    }

    protected Response sendMessage(Customer customer) {
        Response response = customer.api().post("/messages", Map.of(
                "subject", "webmaster",
                "message", TestData.CONTACT_MESSAGE,
                "name", "Test User",
                "email", customer.user().email()));
        return ToolshopApi.expectStatus(response, 200);
    }

    @BeforeMethod(alwaysRun = true)
    public void resetCleanups() {
        cleanups.get().clear();
    }

    @AfterMethod(alwaysRun = true)
    public void runCleanups() {
        Deque<Runnable> queue = cleanups.get();
        while (!queue.isEmpty()) {
            try {
                queue.pop().run();
            } catch (RuntimeException e) {
                System.err.println("Cleanup failed: " + e.getMessage());
            }
        }
    }
}
