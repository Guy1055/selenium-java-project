package com.toolshop.tests.ui;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.ComparisonPage;
import com.toolshop.pages.HomePage;
import com.toolshop.pages.ProductPage;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

public class ProductTest extends BaseUiTest {
    @Test(description = "PRD-01: product page shows name, price and description", groups = "sanity")
    public void showsDetails() {
        ProductPage product = new ProductPage(driver());
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        String name = home.openFirstInStockProduct();

        expect().text(product.name, name);
        expect().condition("price to be formatted",
                () -> product.unitPrice.getText().matches(".*\\d+\\.\\d{2}.*"));
        expect().visible(product.description);
    }

    @Test(description = "PRD-02: quantity buttons change the value and stop at 1")
    public void quantityButtons() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());
        expect().value(product.quantity, "1");

        product.click(product.increase);
        expect().value(product.quantity, "2");
        product.click(product.decrease);
        expect().value(product.quantity, "1");
        product.click(product.decrease);
        expect().value(product.quantity, "1");
    }

    @Test(description = "PRD-03: the chosen quantity is added to the cart")
    public void addsQuantity() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.type(product.quantity, "3");
        product.addToCart();

        expect().text(product.cartBadge, "3");
    }

    @Test(description = "PRD-04: related products are shown and open")
    public void relatedProducts() {
        ToolshopApi.Product sample = api.inStockProduct();
        ProductPage product = new ProductPage(driver());
        product.open(sample.id());
        expect().text(product.name, sample.name());
        expect().visible(product.related);

        product.click(product.related);

        expect().urlNot(sample.id());
        expect().condition("the product name to change",
                () -> !product.text(product.name).equals(sample.name()));
    }

    @Test(description = "PRD-05: specifications match the API")
    public void specsMatchApi() {
        String id = api.inStockProduct().id();
        List<Object> specs = api.get("/products/" + id + "/specs").jsonPath().getList("$");
        if (specs.isEmpty()) {
            throw new SkipException("This product has no specifications");
        }

        ProductPage product = new ProductPage(driver());
        product.open(id);

        expect().count(product.specRows, specs.size());
    }

    @Test(description = "PRD-06: a guest cannot add a favorite")
    public void guestFavorite() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.click(product.addToFavorites);

        expect().visible(BasePage.alertWith("Unauthorized"));
    }

    @Test(description = "PRD-07: a logged-in customer can add a favorite once")
    public void customerFavorite() {
        signedInCustomer();
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.click(product.addToFavorites);
        expect().visible(BasePage.alertWith("added to your favorites"));

        product.click(product.addToFavorites);
        expect().visible(BasePage.alertWith("already"));
    }

    private HomePage selectTwoProducts() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();
        home.compareProduct(0);
        home.compareProduct(1);
        expect().containsText(home.comparisonBar, "2 product(s) selected");
        return home;
    }

    @Test(description = "CMP-01: two selected products appear side by side")
    public void compareTwo() {
        ComparisonPage comparison = new ComparisonPage(driver());
        HomePage home = selectTwoProducts();

        home.click(home.compareLink);

        expect().url("/comparison$");
        expect().count(comparison.removeButtons, 2);
        expect().containsText(comparison.table, "Price");
    }

    @Test(description = "CMP-02: removing a product updates the comparison")
    public void compareRemove() {
        ComparisonPage comparison = new ComparisonPage(driver());
        HomePage home = selectTwoProducts();
        home.click(home.compareLink);
        expect().count(comparison.removeButtons, 2);

        home.click(comparison.removeButton);

        expect().count(comparison.removeButtons, 1);
    }

    @Test(description = "CMP-03: clear all removes the comparison bar")
    public void compareClear() {
        HomePage home = selectTwoProducts();

        home.click(home.clearComparison);

        expect().hidden(home.comparisonBar);
    }

    @Test(description = "CMP-04: the comparison page explains when it is empty")
    public void compareEmpty() {
        ComparisonPage comparison = new ComparisonPage(driver());
        comparison.open();

        expect().visible(comparison.empty);
    }
}
