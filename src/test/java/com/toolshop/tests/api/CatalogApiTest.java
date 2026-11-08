package com.toolshop.tests.api;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseApiTest;
import com.toolshop.core.Env;
import com.toolshop.data.TestData;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.toolshop.api.ToolshopApi.expectStatus;

@Test(groups = "api")
public class CatalogApiTest extends BaseApiTest {
    @Test(description = "API-PRD-01: lists products with pagination metadata", groups = {"api", "sanity"})
    public void listProducts() {
        Response response = expectStatus(api.get("/products"), 200);

        Assert.assertEquals(response.jsonPath().getInt("current_page"), 1);
        Assert.assertFalse(response.jsonPath().getList("data").isEmpty());
        Assert.assertNotNull(response.jsonPath().getString("data[0].id"));
        Assert.assertNotNull(response.jsonPath().getString("data[0].name"));
        Assert.assertNotNull(response.jsonPath().get("data[0].category"));
        Assert.assertNotNull(response.jsonPath().get("data[0].brand"));
    }

    @Test(description = "API-PRD-02: page 2 returns other products")
    public void secondPage() {
        String first = api.get("/products", Map.of("page", 1)).jsonPath().getString("data[0].id");
        Response second = api.get("/products", Map.of("page", 2));

        Assert.assertEquals(second.jsonPath().getInt("current_page"), 2);
        Assert.assertNotEquals(second.jsonPath().getString("data[0].id"), first);
    }

    @DataProvider
    public Object[][] priceSorts() {
        return new Object[][]{{"price,asc", false}, {"price,desc", true}};
    }

    @Test(description = "API-PRD-03: sort orders the results", dataProvider = "priceSorts")
    public void sortByPrice(String sort, boolean descending) {
        List<Number> raw = api.get("/products", Map.of("sort", sort)).jsonPath().getList("data.price");
        List<Double> prices = raw.stream().map(Number::doubleValue).toList();
        List<Double> sorted = new ArrayList<>(prices);
        sorted.sort(descending ? Comparator.reverseOrder() : Comparator.naturalOrder());

        Assert.assertEquals(prices, sorted);
    }

    @Test(description = "API-PRD-04: between filters by price")
    public void betweenFilter() {
        List<Number> prices = api.get("/products", Map.of("between", "price,10,20")).jsonPath().getList("data.price");

        Assert.assertFalse(prices.isEmpty());
        prices.forEach(p -> Assert.assertTrue(p.doubleValue() >= 10 && p.doubleValue() <= 20, "out of range: " + p));
    }

    @Test(description = "API-PRD-05: by_category and by_brand narrow the list")
    public void categoryAndBrandFilters() {
        ToolshopApi.Product sample = api.inStockProduct();

        List<String> categories = api.get("/products", Map.of("by_category", sample.categoryId())).jsonPath().getList("data.category.id");
        List<String> brands = api.get("/products", Map.of("by_brand", sample.brandId())).jsonPath().getList("data.brand.id");

        Assert.assertEquals(Collections.frequency(categories, sample.categoryId()), categories.size());
        Assert.assertEquals(Collections.frequency(brands, sample.brandId()), brands.size());
    }

    @Test(description = "API-PRD-06: returns one product with specs")
    public void singleProduct() {
        ToolshopApi.Product sample = api.inStockProduct();

        Response response = expectStatus(api.get("/products/" + sample.id()), 200);

        Assert.assertEquals(response.jsonPath().getString("name"), sample.name());
        Assert.assertNotNull(response.jsonPath().getList("specs"));
    }

    @Test(description = "API-PRD-07: an unknown product is a 404")
    public void unknownProduct() {
        expectStatus(api.get("/products/does-not-exist"), 404);
    }

    @Test(description = "API-PRD-08: related products exclude the product itself")
    public void relatedProducts() {
        String id = api.inStockProduct().id();

        List<String> related = api.get("/products/" + id + "/related").jsonPath().getList("id");

        Assert.assertFalse(related.isEmpty());
        Assert.assertFalse(related.contains(id));
    }

    @Test(description = "API-PRD-09: search matches on the name")
    public void search() {
        List<String> names = api.get("/products/search", Map.of("q", "hammer")).jsonPath().getList("data.name");

        Assert.assertTrue(names.stream().anyMatch(n -> n.toLowerCase().contains("hammer")), names.toString());
    }

    @Test(description = "API-PRD-10: search with no match is empty")
    public void searchNoMatch() {
        Assert.assertTrue(api.get("/products/search", Map.of("q", "zzzznothing")).jsonPath().getList("data").isEmpty());
    }

    @Test(description = "API-PRD-11: creating a product needs the required fields")
    public void productRequiredFields() {
        Response response = expectStatus(api.post("/products", Map.of("name", "Autotest Product " + TestData.uniqueId())), 422);

        Assert.assertNotNull(response.jsonPath().get("price"));
        Assert.assertNotNull(response.jsonPath().get("category_id"));
    }

    @Test(description = "API-PRD-12: full lifecycle of a product and its specs")
    public void productLifecycle() {
        ToolshopApi.Product sample = api.inStockProduct();
        String name = "Autotest Product " + TestData.uniqueId();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("description", "Temporary product created by the QA suite.");
        payload.put("price", 9.99);
        payload.put("stock", 1);
        payload.put("category_id", sample.categoryId());
        payload.put("brand_id", sample.brandId());
        payload.put("product_image_id", sample.imageId());
        payload.put("is_location_offer", false);
        payload.put("is_rental", false);
        payload.put("co2_rating", "A");
        String id = expectStatus(api.post("/products", payload), 201).jsonPath().getString("id");
        afterTest(() -> adminApi().delete("/products/" + id));

        Map<String, Object> renamed = new LinkedHashMap<>(payload);
        renamed.put("name", name + " Two");
        expectStatus(api.put("/products/" + id, renamed), 200);
        Assert.assertEquals(api.get("/products/" + id).jsonPath().getString("name"), name + " Two");

        expectStatus(api.patch("/products/" + id, Map.of("price", 2.5)), 200);
        Assert.assertEquals(api.get("/products/" + id).jsonPath().getDouble("price"), 2.5, 0.001);

        Map<String, String> spec = Map.of("spec_name", "Weight", "spec_value", "1.5", "spec_unit", "kg");
        String specId = expectStatus(adminApi().post("/products/" + id + "/specs", spec), 201).jsonPath().getString("id");
        expectStatus(api.get("/products/" + id + "/specs/" + specId), 200);
        expectStatus(adminApi().put("/products/" + id + "/specs/" + specId,
                Map.of("spec_name", "Weight", "spec_value", "2", "spec_unit", "kg")), 200);
        Assert.assertEquals(api.get("/products/" + id + "/specs").jsonPath().getString("[0].spec_value"), "2");
        expectStatus(adminApi().delete("/products/" + id + "/specs/" + specId), 204);

        expectStatus(adminApi().delete("/products/" + id), 204);
        expectStatus(api.get("/products/" + id), 404);
    }

    @Test(description = "API-PRD-13: deleting a product needs an admin")
    public void deleteProductPermissions() {
        Customer customer = newCustomer();
        String id = api.inStockProduct().id();

        expectStatus(api.delete("/products/" + id), 401);
        expectStatus(customer.api().delete("/products/" + id), 403);
    }

    @Test(description = "API-PRD-14: specification names are listed")
    public void specNames() {
        Response response = api.get("/product-specs/names");

        Assert.assertFalse(response.jsonPath().getList("$").isEmpty());
        Assert.assertNotNull(response.jsonPath().getString("[0].name"));
        Assert.assertNotNull(response.jsonPath().getList("[0].values"));
    }

    @Test(description = "API-PRD-15: changing a spec needs a token")
    public void specNeedsToken() {
        String id = api.inStockProduct().id();

        expectStatus(api.post("/products/" + id + "/specs", Map.of("spec_name", "X", "spec_value", "1")), 401);
    }

    @Test(description = "API-BRD-01: lists and searches brands")
    public void brandsListAndSearch() {
        Response brands = api.get("/brands");
        Assert.assertFalse(brands.jsonPath().getList("$").isEmpty());
        String id = brands.jsonPath().getString("[0].id");
        String word = brands.jsonPath().getString("[0].name").split(" ")[0];

        List<String> found = api.get("/brands/search", Map.of("q", word)).jsonPath().getList("id");

        Assert.assertTrue(found.contains(id));
    }

    private Response createBrand() {
        String unique = TestData.uniqueId();
        Response brand = expectStatus(api.post("/brands", Map.of("name", "QA Brand " + unique, "slug", "qa-brand-" + unique)), 201);
        afterTest(() -> adminApi().delete("/brands/" + brand.jsonPath().getString("id")));
        return brand;
    }

    @Test(description = "API-BRD-02: full lifecycle of a brand")
    public void brandLifecycle() {
        Response brand = createBrand();
        String id = brand.jsonPath().getString("id");
        String name = brand.jsonPath().getString("name");
        String slug = brand.jsonPath().getString("slug");

        expectStatus(api.get("/brands/" + id), 200);
        expectStatus(api.put("/brands/" + id, Map.of("name", name + " Renamed", "slug", slug)), 200);
        expectStatus(api.patch("/brands/" + id, Map.of("name", name)), 200);
        Assert.assertEquals(api.get("/brands/" + id).jsonPath().getString("name"), name);

        expectStatus(adminApi().delete("/brands/" + id), 204);
        expectStatus(api.get("/brands/" + id), 404);
    }

    @Test(description = "API-BRD-03: a duplicate slug is a conflict")
    public void brandDuplicateSlug() {
        Response brand = createBrand();

        expectStatus(api.post("/brands", Map.of("name", "Duplicate", "slug", brand.jsonPath().getString("slug"))), 409);
    }

    @Test(description = "API-BRD-04: missing fields are rejected")
    public void brandRequired() {
        expectStatus(api.post("/brands", Map.of()), 422);
    }

    @Test(description = "API-BRD-05: deleting a brand needs an admin")
    public void brandDeletePermissions() {
        Customer customer = newCustomer();
        String id = api.get("/brands").jsonPath().getString("[0].id");

        expectStatus(api.delete("/brands/" + id), 401);
        expectStatus(customer.api().delete("/brands/" + id), 403);
    }

    @Test(description = "API-CAT-01: lists categories")
    public void categoriesList() {
        Assert.assertFalse(api.get("/categories").jsonPath().getList("$").isEmpty());
    }

    @Test(description = "API-CAT-02: the tree nests sub-categories")
    public void categoryTree() {
        List<List<Object>> children = api.get("/categories/tree").jsonPath().getList("sub_categories");

        Assert.assertTrue(children.stream().anyMatch(c -> !c.isEmpty()));
    }

    @Test(description = "API-CAT-03: search finds a category")
    public void categorySearch() {
        List<String> names = api.get("/categories/search", Map.of("q", "hammer")).jsonPath().getList("name");

        Assert.assertTrue(names.stream().anyMatch(n -> n.toLowerCase().contains("hammer")), names.toString());
    }

    @Test(description = "API-CAT-04: full lifecycle of a category")
    public void categoryLifecycle() {
        String unique = TestData.uniqueId();
        String name = "QA Category " + unique;
        String slug = "qa-category-" + unique;
        String id = expectStatus(api.post("/categories", Map.of("name", name, "slug", slug)), 201).jsonPath().getString("id");
        afterTest(() -> adminApi().delete("/categories/" + id));

        Response single = expectStatus(api.get("/categories/tree/" + id), 200);
        Assert.assertTrue(single.jsonPath().getList("sub_categories").isEmpty());
        expectStatus(api.put("/categories/" + id, Map.of("name", name + " Renamed", "slug", slug)), 200);
        expectStatus(api.patch("/categories/" + id, Map.of("name", name)), 200);

        expectStatus(adminApi().delete("/categories/" + id), 204);
    }

    @Test(description = "API-CAT-05: deleting a category needs an admin")
    public void categoryDeletePermissions() {
        Customer customer = newCustomer();
        String id = api.get("/categories").jsonPath().getString("[0].id");

        expectStatus(api.delete("/categories/" + id), 401);
        expectStatus(customer.api().delete("/categories/" + id), 403);
    }

    @Test(description = "API-IMG-01: lists product images")
    public void images() {
        Response response = api.get("/images");

        Assert.assertFalse(response.jsonPath().getList("$").isEmpty());
        Assert.assertNotNull(response.jsonPath().getString("[0].file_name"));
    }

    @Test(description = "API-STR-01: the sales stream is server-sent events")
    public void salesStream() throws IOException, InterruptedException {
        // Streamed body handler, because the stream never ends.
        HttpResponse<InputStream> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(Env.API_URL + "/sales-stream")).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertTrue(response.headers().firstValue("content-type").orElse("").contains("text/event-stream"));
        response.body().close();
    }

    @Test(description = "API-PST-01: looks up a Dutch postcode")
    public void postcodeLookup() {
        Response response = expectStatus(api.get("/postcode-lookup",
                Map.of("country", "NL", "postcode", "1011AB", "house_number", "42")), 200);

        Assert.assertNotNull(response.jsonPath().getString("city"));
        Assert.assertEquals(response.jsonPath().getString("country"), "NL");
        Assert.assertEquals(response.jsonPath().getString("postcode"), "1011AB");
    }
}
