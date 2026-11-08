package com.toolshop.tests.ui.admin;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.core.KnownBug;
import com.toolshop.data.TestData;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.admin.AdminFormPage;
import com.toolshop.pages.admin.AdminFormPage.ProductForm;
import com.toolshop.pages.admin.AdminListPage;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminCatalogTest extends BaseUiTest {
    private static final By BRAND_SAVED = BasePage.testId("brand-saved");
    private static final By BRAND_ERROR = BasePage.testId("brand-error");

    /** Removes a record the test created, found by its unique name. Seed data is never touched. */
    private void deleteByName(String collection, String name) {
        List<Map<String, String>> found = adminApi().get("/" + collection + "/search", Map.of("q", name)).jsonPath().getList("$");
        found.stream().filter(r -> name.equals(r.get("name")))
                .forEach(r -> adminApi().delete("/" + collection + "/" + r.get("id")));
    }

    private Response createBrand() {
        String id = TestData.uniqueId();
        Response brand = ToolshopApi.expectStatus(api.post("/brands", Map.of("name", "QA Brand " + id, "slug", "qa-brand-" + id)), 201);
        afterTest(() -> adminApi().delete("/brands/" + brand.jsonPath().getString("id")));
        return brand;
    }

    @Test(description = "ADM-10: an admin can add a brand")
    public void addBrand() {
        signInAsAdmin();
        String id = TestData.uniqueId();
        String name = "QA Brand " + id;
        afterTest(() -> deleteByName("brands", name));
        AdminListPage list = new AdminListPage(driver(), "brand", "/admin/brands");
        AdminFormPage form = new AdminFormPage(driver());
        list.open();

        list.click(list.addButton());
        form.fillBrandOrCategory(name, "qa-brand-" + id);
        form.submit("brand");

        expect().visible(BRAND_SAVED);
    }

    @Test(description = "ADM-11: a duplicate brand slug shows an error")
    @KnownBug("BUG-01: the API answers 409 but the form shows no error message")
    public void duplicateSlug() {
        signInAsAdmin();
        Response brand = createBrand();
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/brands/add");

        form.fillBrandOrCategory("QA Other " + TestData.uniqueId(), brand.jsonPath().getString("slug"));
        form.submit("brand");

        expect().containsText(BRAND_ERROR, "already exists");
    }

    @Test(description = "ADM-12: an admin can rename a brand")
    public void renameBrand() {
        signInAsAdmin();
        Response brand = createBrand();
        String id = brand.jsonPath().getString("id");
        String name = brand.jsonPath().getString("name");
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/brands/edit/" + id);
        expect().value(form.name, name);

        form.type(form.name, name + " Renamed");
        form.submit("brand");

        expect().visible(BRAND_SAVED);
        Assert.assertEquals(api.get("/brands/" + id).jsonPath().getString("name"), name + " Renamed");
    }

    @Test(description = "ADM-13: an admin can find and delete a brand")
    public void deleteBrand() {
        signInAsAdmin();
        Response brand = createBrand();
        String id = brand.jsonPath().getString("id");
        AdminListPage list = new AdminListPage(driver(), "brand", "/admin/brands");
        list.open();

        list.search(brand.jsonPath().getString("name"));
        list.click(list.deleteButton(id));

        expect().visible(BasePage.alertWith("Brand deleted"));
        ToolshopApi.expectStatus(api.get("/brands/" + id), 404);
    }

    @Test(description = "ADM-14: name and slug are required")
    public void brandRequired() {
        signInAsAdmin();
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/brands/add");

        form.submit("brand");

        expect().visible(BasePage.testId("name-error"));
        expect().visible(BasePage.testId("slug-error"));
    }

    private Response createCategory() {
        String id = TestData.uniqueId();
        Response category = ToolshopApi.expectStatus(api.post("/categories", Map.of("name", "QA Category " + id, "slug", "qa-category-" + id)), 201);
        afterTest(() -> adminApi().delete("/categories/" + category.jsonPath().getString("id")));
        return category;
    }

    @Test(description = "ADM-20: an admin can add a category")
    public void addCategory() {
        signInAsAdmin();
        String id = TestData.uniqueId();
        String name = "QA Category " + id;
        afterTest(() -> deleteByName("categories", name));
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/categories/add");

        form.fillBrandOrCategory(name, "qa-category-" + id);
        form.submit("category");

        expect().visible(form.success);
    }

    @Test(description = "ADM-21: an admin can rename a category")
    public void renameCategory() {
        signInAsAdmin();
        Response category = createCategory();
        String name = category.jsonPath().getString("name");
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/categories/edit/" + category.jsonPath().getString("id"));
        expect().value(form.name, name);

        form.type(form.name, name + " Renamed");
        form.submit("category");

        expect().visible(form.success);
    }

    @Test(description = "ADM-22: an admin can find and delete a category")
    public void deleteCategory() {
        signInAsAdmin();
        Response category = createCategory();
        AdminListPage list = new AdminListPage(driver(), "category", "/admin/categories");
        list.open();

        list.search(category.jsonPath().getString("name"));
        list.click(list.deleteButton(category.jsonPath().getString("id")));

        expect().visible(BasePage.alertWith("deleted"));
    }

    private Response createProduct() {
        ToolshopApi.Product sample = api.inStockProduct();
        Map<String, Object> body = new LinkedHashMap<>();
        // Full-text search ignores two-letter words, hence not "QA Product".
        body.put("name", "Autotest Product " + TestData.uniqueId());
        body.put("description", "Temporary product created by the QA suite.");
        body.put("price", 9.99);
        body.put("stock", 1);
        body.put("category_id", sample.categoryId());
        body.put("brand_id", sample.brandId());
        body.put("product_image_id", sample.imageId());
        body.put("is_location_offer", false);
        body.put("is_rental", false);
        body.put("co2_rating", "A");
        Response product = ToolshopApi.expectStatus(api.post("/products", body), 201);
        afterTest(() -> adminApi().delete("/products/" + product.jsonPath().getString("id")));
        return product;
    }

    private ProductForm formFor(String name, String price) {
        ToolshopApi.Product sample = api.inStockProduct();
        return new ProductForm(name, "Temporary product created by the QA suite.", price, "1",
                sample.brandId(), sample.categoryId(), sample.imageId());
    }

    @Test(description = "ADM-30: an admin can add a product")
    public void addProduct() {
        signInAsAdmin();
        String name = "Autotest Product " + TestData.uniqueId();
        afterTest(() -> {
            List<String> ids = api.get("/products/search", Map.of("q", name)).jsonPath().getList("data.id");
            ids.forEach(id -> adminApi().delete("/products/" + id));
        });
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/products/add");

        form.fillProduct(formFor(name, "9.99"));
        form.submit("product");

        expect().visible(form.success);
    }

    @Test(description = "ADM-31: an admin can change a product price")
    public void changePrice() {
        signInAsAdmin();
        Response product = createProduct();
        String id = product.jsonPath().getString("id");
        String name = product.jsonPath().getString("name");
        AdminFormPage form = new AdminFormPage(driver());
        form.open("/admin/products/edit/" + id);
        expect().value(form.name, name);

        // The edit form does not prefill stock, brand, category or image (BUG-03), so set them again.
        form.fillProduct(formFor(name, "19.5"));
        form.submit("product");

        expect().visible(form.success);
        Assert.assertEquals(api.get("/products/" + id).jsonPath().getDouble("price"), 19.5, 0.001);
    }

    @Test(description = "ADM-32: an admin can find and delete a product")
    public void deleteProduct() {
        signInAsAdmin();
        Response product = createProduct();
        String id = product.jsonPath().getString("id");
        AdminListPage list = new AdminListPage(driver(), "product", "/admin/products");
        list.open();

        list.search(product.jsonPath().getString("name"));
        list.click(list.deleteButton(id));

        expect().visible(BasePage.alertWith("deleted"));
        ToolshopApi.expectStatus(api.get("/products/" + id), 404);
    }

    @Test(description = "ADM-33: search reset restores the product list")
    public void searchReset() {
        signInAsAdmin();
        AdminListPage list = new AdminListPage(driver(), "product", "/admin/products");
        list.open();
        int fullCount = list.count(list.rows);

        list.search("zzzz-no-such-product");
        expect().count(list.rows, 0);
        list.click(list.resetButton());

        expect().count(list.rows, fullCount);
    }
}
