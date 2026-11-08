package com.toolshop.tests.db;

import com.toolshop.core.BaseApiTest;
import com.toolshop.core.Env;
import com.toolshop.data.TestData;
import com.toolshop.db.DbClient;
import com.toolshop.db.Sql;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.toolshop.api.ToolshopApi.expectStatus;

/** Placeholder tier: skipped unless DB_URL points at a self-hosted Toolshop. */
@Test(groups = "db")
public class DatabaseTest extends BaseApiTest {
    @BeforeMethod(alwaysRun = true)
    public void requireDatabase() {
        if (Env.DB_URL == null) {
            throw new SkipException("DB_URL is not set: the public demo exposes no database");
        }
    }

    private DbClient db() throws SQLException {
        return new DbClient(Env.DB_URL);
    }

    @Test(description = "DB-01: a registered user is stored with a hashed password")
    public void userStored() throws SQLException {
        Customer customer = newCustomer();
        try (DbClient db = db()) {
            Map<String, Object> row = db.query(Sql.USER_BY_EMAIL, customer.user().email()).get(0);

            Assert.assertEquals(row.get("email"), customer.user().email());
            Assert.assertEquals(row.get("role"), "user");
            Assert.assertNotEquals(row.get("password"), customer.user().password());
        }
    }

    @Test(description = "DB-02: an order total equals the sum of its lines")
    public void orderTotal() throws SQLException {
        Customer customer = newCustomer();
        String cartId = api.createCartWith(api.inStockProduct().id(), 2);
        String id = customer.api().post("/invoices", api.billingFor(cartId)).jsonPath().getString("id");
        try (DbClient db = db()) {
            Map<String, Object> invoice = db.query(Sql.INVOICE_BY_ID, id).get(0);
            double sum = db.query(Sql.INVOICE_ITEMS, id).stream()
                    .mapToDouble(i -> ((Number) i.get("unit_price")).doubleValue() * ((Number) i.get("quantity")).intValue())
                    .sum();

            Assert.assertEquals(((Number) invoice.get("total")).doubleValue(), sum, 0.01);
            Assert.assertEquals(invoice.get("user_id"), customer.id());
        }
    }

    @Test(description = "DB-03: cart items are stored with their quantity")
    public void cartItems() throws SQLException {
        String productId = api.inStockProduct().id();
        String cartId = api.createCartWith(productId, 3);
        afterTest(() -> api.delete("/carts/" + cartId));
        try (DbClient db = db()) {
            List<Map<String, Object>> rows = db.query(Sql.CART_ITEMS, cartId);

            Assert.assertEquals(rows.size(), 1);
            Assert.assertEquals(rows.get(0).get("product_id"), productId);
            Assert.assertEquals(((Number) rows.get(0).get("quantity")).intValue(), 3);
        }
    }

    @Test(description = "DB-04: a favorite is stored and then removed")
    public void favorite() throws SQLException {
        Customer customer = newCustomer();
        String productId = api.inStockProduct().id();
        String id = customer.api().post("/favorites", Map.of("product_id", productId)).jsonPath().getString("id");
        try (DbClient db = db()) {
            Assert.assertEquals(db.query(Sql.FAVORITE, customer.id(), productId).size(), 1);

            expectStatus(customer.api().delete("/favorites/" + id), 204);

            Assert.assertTrue(db.query(Sql.FAVORITE, customer.id(), productId).isEmpty());
        }
    }

    @Test(description = "DB-05: a contact message is stored as NEW")
    public void contactRequest() throws SQLException {
        Customer customer = newCustomer();
        String id = sendMessage(customer).jsonPath().getString("id");
        try (DbClient db = db()) {
            Map<String, Object> row = db.query(Sql.CONTACT_REQUEST, id).get(0);

            Assert.assertEquals(row.get("status"), "NEW");
            Assert.assertEquals(row.get("user_id"), customer.id());
        }
    }

    @Test(description = "DB-06: a deleted brand leaves no row")
    public void deletedBrand() throws SQLException {
        String unique = TestData.uniqueId();
        String id = api.post("/brands", Map.of("name", "QA Brand " + unique, "slug", "qa-brand-" + unique)).jsonPath().getString("id");
        try (DbClient db = db()) {
            Assert.assertEquals(db.query(Sql.BRAND_BY_ID, id).size(), 1);

            expectStatus(adminApi().delete("/brands/" + id), 204);

            Assert.assertTrue(db.query(Sql.BRAND_BY_ID, id).isEmpty());
        }
    }

    @DataProvider
    public Object[][] integrityChecks() {
        return new Object[][]{
                {"DB-10 every product has a brand, category and image", Sql.PRODUCTS_WITHOUT_RELATIONS},
                {"DB-11 no invoice line is orphaned", Sql.ORPHAN_INVOICE_ITEMS},
                {"DB-12 no cart item is orphaned", Sql.ORPHAN_CART_ITEMS},
                {"DB-13 emails are unique", Sql.DUPLICATE_EMAILS},
                {"DB-14 brand slugs are unique", Sql.DUPLICATE_BRAND_SLUGS},
                {"DB-15 category slugs are unique", Sql.DUPLICATE_CATEGORY_SLUGS},
                {"DB-16 a user cannot favorite a product twice", Sql.DUPLICATE_FAVORITES},
                {"DB-17 prices are never negative", Sql.NEGATIVE_PRICES},
                {"DB-18 stock is never negative", Sql.NEGATIVE_STOCK},
                {"DB-19 roles are limited to user and admin", Sql.UNKNOWN_ROLES},
                {"DB-20 category parents exist", Sql.CATEGORIES_WITH_MISSING_PARENT},
        };
    }

    @Test(description = "Database integrity rules", dataProvider = "integrityChecks")
    public void integrity(String rule, String sql) throws SQLException {
        try (DbClient db = db()) {
            Assert.assertEquals(db.query(sql), List.of(), rule);
        }
    }
}
