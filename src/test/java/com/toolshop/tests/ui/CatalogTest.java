package com.toolshop.tests.ui;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.core.Env;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.HomePage;
import com.toolshop.pages.NavBar;
import com.toolshop.pages.ProductPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class CatalogTest extends BaseUiTest {
    @Test(description = "CAT-03: a search with no matches shows the empty state")
    public void noResults() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.search("zzzz-no-such-product");

        expect().text(home.noResults, "There are no products found.");
        expect().count(home.productNames, 0);
        expect().containsText(home.searchCaption, "zzzz-no-such-product");
    }

    @DataProvider
    public Object[][] searchTerms() {
        return new Object[][]{{"pliers"}, {"saw"}, {"hammer"}};
    }

    @Test(description = "CAT-08: search lists only matching products", dataProvider = "searchTerms")
    public void searchMatches(String term) {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.search(term);

        expect().visible(home.productName);
        expect().condition("every name to contain " + term,
                () -> home.listedNames().stream().allMatch(n -> n.toLowerCase().contains(term)));
    }

    @Test(description = "CAT-09: resetting the search restores the catalog")
    public void resetRestores() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();
        List<String> original = home.listedNames();
        home.search("hammer");
        expect().visible(home.searchCaption);

        home.click(home.searchReset);

        expect().hidden(home.searchCaption);
        expect().value(home.searchInput, "");
        expect().texts(home.productNames, original);
    }

    @DataProvider
    public Object[][] sorts() {
        return new Object[][]{{"price,asc"}, {"price,desc"}, {"name,asc"}, {"name,desc"}};
    }

    @Test(description = "CAT-04: sorting matches the API order", dataProvider = "sorts")
    public void sortMatchesApi(String sort) {
        // The grid applies its default 1-100 price range, so the API call must too.
        List<String> expected = api.get("/products", Map.of("sort", sort, "page", 1, "between", "price,1,100"))
                .jsonPath().getList("data.name");
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.sortBy(sort);

        expect().texts(home.productNames, expected);
    }

    @Test(description = "CAT-05: filtering by category narrows the list to that category")
    public void filterByCategory() {
        ToolshopApi.Product product = api.inStockProduct();
        List<String> expected = api.get("/products",
                        Map.of("by_category", product.categoryId(), "page", 1, "between", "price,1,100"))
                .jsonPath().getList("data.name");
        String category = api.get("/products/" + product.id()).jsonPath().getString("category.name");
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.filterBy(category);
        home.waitFor("filter_completed");

        expect().texts(home.productNames, expected);
    }

    @Test(description = "CAT-05b: filtering by brand lists only that brand")
    public void filterByBrand() {
        ToolshopApi.Product product = api.inStockProduct();
        List<String> expected = api.get("/products",
                        Map.of("by_brand", product.brandId(), "page", 1, "between", "price,1,100"))
                .jsonPath().getList("data.name");
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.filterBy(product.brandName());
        home.waitFor("filter_completed");

        expect().texts(home.productNames, expected);
    }

    @Test(description = "CAT-10: the eco filter shows only eco-friendly products")
    public void ecoFilter() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.click(home.ecoFilter);

        expect().condition("every listed product to have an eco badge", () -> {
            int names = home.count(home.productNames);
            return names > 0 && home.count(home.ecoBadges) == names;
        });
    }

    @Test(description = "CAT-11: lowering the maximum price removes more expensive products")
    public void priceSlider() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.lowerMaxPrice();

        int max = home.maxPrice();
        Assert.assertTrue(max < 100, "slider should have moved below 100, was " + max);
        expect().condition("every price to be within " + max,
                () -> home.prices().stream().allMatch(price -> price <= max));
    }

    @Test(description = "CAT-06: the next page shows different products")
    public void pagination() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();
        List<String> firstPage = home.listedNames();

        home.click(home.nextPage);

        expect().condition("the listing to change", () -> !home.listedNames().equals(firstPage));
        expect().count(home.productNames, 9);
    }

    @Test(description = "CAT-07: an out-of-stock product cannot be added to the cart")
    public void outOfStock() {
        ToolshopApi.Product product = api.outOfStockProduct();
        if (product == null) {
            throw new SkipException("The shared demo currently has no out-of-stock product");
        }

        ProductPage productPage = new ProductPage(driver());
        productPage.open(product.id());

        expect().visible(productPage.outOfStock);
        expect().disabled(productPage.addToCartButton);
    }

    @Test(description = "RNT-01: the rentals page lists rental products")
    public void rentals() {
        open("/rentals");

        expect().text(BasePage.testId("page-title"), "Rentals");
        expect().visible(By.cssSelector("[data-test^='product-']"));
    }

    @Test(description = "PRV-01: the privacy policy page opens")
    public void privacy() {
        open("/privacy");

        expect().containsText(By.tagName("h1"), "Privacy");
    }

    @DataProvider
    public Object[][] categories() {
        return new Object[][]{
                {"nav-hand-tools", "/category/hand-tools", "Hand Tools"},
                {"nav-power-tools", "/category/power-tools", "Power Tools"},
                {"nav-other", "/category/other", "Other"},
                {"nav-special-tools", "/category/special-tools", "Special Tools"},
        };
    }

    @Test(description = "NAV-01: the menu opens a category", dataProvider = "categories")
    public void categoryMenu(String id, String path, String title) {
        NavBar nav = new NavBar(driver());
        open("/");

        nav.openCategory(id);

        expect().url(path + "$");
        expect().containsText(BasePage.testId("page-title"), title);
    }

    @Test(description = "NAV-02: the top links open their pages")
    public void topLinks() {
        NavBar nav = new NavBar(driver());
        open("/");

        nav.click(nav.contact);
        expect().url("/contact$");
        nav.openCategory("nav-rentals");
        expect().url("/rentals$");
        nav.click(nav.signIn);
        expect().url("/auth/login$");
        nav.click(nav.home);
        expect().url(Env.HOME_URL);
    }
}
