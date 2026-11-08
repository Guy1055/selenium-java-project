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
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        String name = home.openFirstInStockProduct();

        expect().text(ProductPage.NAME, name);
        expect().condition("price to be formatted",
                () -> driver().findElement(ProductPage.UNIT_PRICE).getText().matches(".*\\d+\\.\\d{2}.*"));
        expect().visible(ProductPage.DESCRIPTION);
    }

    @Test(description = "PRD-02: quantity buttons change the value and stop at 1")
    public void quantityButtons() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());
        expect().value(ProductPage.QUANTITY, "1");

        product.click(ProductPage.INCREASE);
        expect().value(ProductPage.QUANTITY, "2");
        product.click(ProductPage.DECREASE);
        expect().value(ProductPage.QUANTITY, "1");
        product.click(ProductPage.DECREASE);
        expect().value(ProductPage.QUANTITY, "1");
    }

    @Test(description = "PRD-03: the chosen quantity is added to the cart")
    public void addsQuantity() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.type(ProductPage.QUANTITY, "3");
        product.addToCart();

        expect().text(ProductPage.CART_BADGE, "3");
    }

    @Test(description = "PRD-04: related products are shown and open")
    public void relatedProducts() {
        ToolshopApi.Product sample = api.inStockProduct();
        ProductPage product = new ProductPage(driver());
        product.open(sample.id());
        expect().text(ProductPage.NAME, sample.name());
        expect().visible(ProductPage.RELATED);

        product.click(ProductPage.RELATED);

        expect().urlNot(sample.id());
        expect().condition("the product name to change",
                () -> !product.text(ProductPage.NAME).equals(sample.name()));
    }

    @Test(description = "PRD-05: specifications match the API")
    public void specsMatchApi() {
        String id = api.inStockProduct().id();
        List<Object> specs = api.get("/products/" + id + "/specs").jsonPath().getList("$");
        if (specs.isEmpty()) {
            throw new SkipException("This product has no specifications");
        }

        new ProductPage(driver()).open(id);

        expect().count(ProductPage.SPEC_ROWS, specs.size());
    }

    @Test(description = "PRD-06: a guest cannot add a favorite")
    public void guestFavorite() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.click(ProductPage.ADD_TO_FAVORITES);

        expect().visible(BasePage.alertWith("Unauthorized"));
    }

    @Test(description = "PRD-07: a logged-in customer can add a favorite once")
    public void customerFavorite() {
        signedInCustomer();
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.click(ProductPage.ADD_TO_FAVORITES);
        expect().visible(BasePage.alertWith("added to your favorites"));

        product.click(ProductPage.ADD_TO_FAVORITES);
        expect().visible(BasePage.alertWith("already"));
    }

    private HomePage selectTwoProducts() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();
        home.compareProduct(0);
        home.compareProduct(1);
        expect().containsText(HomePage.COMPARISON_BAR, "2 product(s) selected");
        return home;
    }

    @Test(description = "CMP-01: two selected products appear side by side")
    public void compareTwo() {
        HomePage home = selectTwoProducts();

        home.click(HomePage.COMPARE_LINK);

        expect().url("/comparison$");
        expect().count(ComparisonPage.REMOVE_BUTTONS, 2);
        expect().containsText(ComparisonPage.TABLE, "Price");
    }

    @Test(description = "CMP-02: removing a product updates the comparison")
    public void compareRemove() {
        HomePage home = selectTwoProducts();
        home.click(HomePage.COMPARE_LINK);
        expect().count(ComparisonPage.REMOVE_BUTTONS, 2);

        home.click(ComparisonPage.REMOVE_BUTTONS);

        expect().count(ComparisonPage.REMOVE_BUTTONS, 1);
    }

    @Test(description = "CMP-03: clear all removes the comparison bar")
    public void compareClear() {
        HomePage home = selectTwoProducts();

        home.click(HomePage.CLEAR_COMPARISON);

        expect().hidden(HomePage.COMPARISON_BAR);
    }

    @Test(description = "CMP-04: the comparison page explains when it is empty")
    public void compareEmpty() {
        new ComparisonPage(driver()).open();

        expect().visible(ComparisonPage.EMPTY);
    }
}
