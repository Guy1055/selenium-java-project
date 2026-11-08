package com.toolshop.tests.ui;

import com.toolshop.api.ToolshopApi;
import com.toolshop.core.BaseUiTest;
import com.toolshop.core.Env;
import com.toolshop.data.TestData;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.CartPage;
import com.toolshop.pages.NavBar;
import com.toolshop.pages.ProductPage;
import org.testng.annotations.Test;

public class CartTest extends BaseUiTest {
    private CartPage cartWith(String productId) {
        new ProductPage(driver()).addProductToCart(productId);
        CartPage cart = new CartPage(driver());
        cart.open();
        expect().count(CartPage.TITLES, 1);
        return cart;
    }

    private void expectPrice(org.openqa.selenium.By by, double expected, CartPage cart) {
        expect().condition(by + " to be " + expected,
                () -> Math.abs(TestData.parsePrice(cart.text(by)) - expected) < 0.01);
    }

    @Test(description = "CRT-01: adding a product shows a toast and sets the cart badge to 1", groups = "sanity")
    public void addToCart() {
        ProductPage product = new ProductPage(driver());
        product.open(api.inStockProduct().id());

        product.addToCart();

        expect().visible(BasePage.alertWith("Product added to shopping cart"));
        expect().text(ProductPage.CART_BADGE, "1");
    }

    @Test(description = "CRT-02: cart total equals unit price times quantity", groups = "sanity")
    public void cartTotal() {
        ToolshopApi.Product sample = api.inStockProduct();
        ProductPage product = new ProductPage(driver());
        product.open(sample.id());
        product.click(ProductPage.INCREASE);
        expect().value(ProductPage.QUANTITY, "2");
        product.addToCart();
        expect().text(ProductPage.CART_BADGE, "2");

        CartPage cart = new CartPage(driver());
        cart.open();

        expectPrice(CartPage.TOTAL, sample.price() * 2, cart);
    }

    @Test(description = "CRT-03: changing the quantity updates line price and total")
    public void changeQuantity() {
        ToolshopApi.Product sample = api.inStockProduct();
        CartPage cart = cartWith(sample.id());

        cart.setQuantity(3);

        expect().visible(BasePage.alertWith("Product quantity updated"));
        expectPrice(CartPage.LINE_PRICES, sample.price() * 3, cart);
        expectPrice(CartPage.TOTAL, sample.price() * 3, cart);
    }

    @Test(description = "CRT-04: removing the only item empties the cart")
    public void removeItem() {
        CartPage cart = cartWith(api.inStockProduct().id());

        cart.removeFirstItem();

        expect().visible(BasePage.alertWith("Product deleted"));
        expect().count(CartPage.TITLES, 0);
        expect().visible(CartPage.EMPTY_MESSAGE);
    }

    @Test(description = "CRT-05: continue shopping returns to the catalog and keeps the cart")
    public void continueShopping() {
        CartPage cart = cartWith(api.inStockProduct().id());

        cart.click(CartPage.CONTINUE_SHOPPING);

        expect().url(Env.HOME_URL);
        expect().text(ProductPage.CART_BADGE, "1");
    }

    @Test(description = "CRT-06: a visitor with no cart sees no cart link")
    public void noCartLink() {
        open("/");

        expect().visible(NavBar.HOME);
        expect().hidden(NavBar.CART);
    }
}
