package com.toolshop.api;

import com.toolshop.core.Env;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.ProxySpecification;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Thin client over the Toolshop REST API. Returns raw responses so tests can assert on status. */
public class ToolshopApi {
    private final String token;

    public ToolshopApi() {
        this(null);
    }

    private ToolshopApi(String token) {
        this.token = token;
    }

    public ToolshopApi withToken(String value) {
        return new ToolshopApi(value);
    }

    private RequestSpecification request() {
        RequestSpecification spec = RestAssured.given().baseUri(Env.API_URL).accept(ContentType.JSON);
        if (token != null) {
            spec.header("Authorization", "Bearer " + token);
        }
        if (Env.HTTPS_PROXY != null) {
            URI proxy = URI.create(Env.HTTPS_PROXY);
            spec.proxy(ProxySpecification.host(proxy.getHost()).withPort(proxy.getPort()));
        }
        return spec;
    }

    public Response get(String path) {
        return request().get(path);
    }

    public Response get(String path, Map<String, ?> params) {
        return request().queryParams(params).get(path);
    }

    public Response post(String path) {
        return request().post(path);
    }

    public Response post(String path, Object body) {
        return request().contentType(ContentType.JSON).body(body).post(path);
    }

    public Response put(String path, Object body) {
        return request().contentType(ContentType.JSON).body(body).put(path);
    }

    public Response patch(String path, Object body) {
        return request().contentType(ContentType.JSON).body(body).patch(path);
    }

    public Response delete(String path) {
        return request().delete(path);
    }

    public Response attachFile(String messageId, File file) {
        return request().multiPart("file", file, "text/plain").post("/messages/" + messageId + "/attach-file");
    }

    /** Asserts the status and shows the response body when it fails. */
    public static Response expectStatus(Response response, int status) {
        Assert.assertEquals(response.statusCode(), status, response.asString());
        return response;
    }

    public String register(NewUser user) {
        return expectStatus(post("/users/register", user.payload()), 201).jsonPath().getString("id");
    }

    public String login(String email, String password) {
        Response response = post("/users/login", Map.of("email", email, "password", password));
        return expectStatus(response, 200).jsonPath().getString("access_token");
    }

    public record Product(String id, String name, double price, String brandId, String brandName,
                          String categoryId, String imageId) {
    }

    private static Product toProduct(Map<String, Object> raw) {
        return new Product((String) raw.get("id"), (String) raw.get("name"), ((Number) raw.get("price")).doubleValue(),
                nested(raw, "brand", "id"), nested(raw, "brand", "name"),
                nested(raw, "category", "id"), nested(raw, "product_image", "id"));
    }

    @SuppressWarnings("unchecked")
    private static String nested(Map<String, Object> raw, String parent, String key) {
        return (String) ((Map<String, Object>) raw.get(parent)).get(key);
    }

    /** First purchasable product on page 1 (stock varies on the demo). */
    public Product inStockProduct() {
        List<Map<String, Object>> data = get("/products", Map.of("page", 1)).jsonPath().getList("data");
        return data.stream()
                .filter(p -> Boolean.TRUE.equals(p.get("in_stock")) && !Boolean.TRUE.equals(p.get("is_rental")))
                .findFirst().map(ToolshopApi::toProduct)
                .orElseThrow(() -> new IllegalStateException("No in-stock product on page 1"));
    }

    /** Any out-of-stock product, or null when the demo currently has none. */
    public Product outOfStockProduct() {
        for (int page = 1; page <= 10; page++) {
            Response response = get("/products", Map.of("page", page));
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            for (Map<String, Object> product : data) {
                if (!Boolean.TRUE.equals(product.get("in_stock"))) {
                    return toProduct(product);
                }
            }
            if (page >= response.jsonPath().getInt("last_page")) {
                break;
            }
        }
        return null;
    }

    public String createCart() {
        return expectStatus(post("/carts"), 201).jsonPath().getString("id");
    }

    public String createCartWith(String productId, int quantity) {
        String cartId = createCart();
        expectStatus(post("/carts/" + cartId, Map.of("product_id", productId, "quantity", quantity)), 200);
        return cartId;
    }

    /** Valid billing details: the API rejects a city that does not match the postcode lookup. */
    public Map<String, Object> billingFor(String cartId) {
        Response lookup = expectStatus(get("/postcode-lookup", Map.of(
                "country", TestData.COUNTRY, "postcode", TestData.POSTCODE, "house_number", TestData.HOUSE_NUMBER)), 200);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("billing_street", "Test Street");
        body.put("billing_house_number", TestData.HOUSE_NUMBER);
        body.put("billing_city", lookup.jsonPath().getString("city"));
        body.put("billing_state", lookup.jsonPath().getString("state"));
        body.put("billing_country", TestData.COUNTRY);
        body.put("billing_postal_code", TestData.POSTCODE);
        body.put("payment_method", "cash-on-delivery");
        body.put("payment_details", Map.of());
        body.put("cart_id", cartId);
        return body;
    }
}
