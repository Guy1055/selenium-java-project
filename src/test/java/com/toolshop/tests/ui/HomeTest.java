package com.toolshop.tests.ui;

import com.toolshop.core.BaseUiTest;
import com.toolshop.pages.HomePage;
import com.toolshop.pages.NavBar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomeTest extends BaseUiTest {
    @Test(description = "CAT-01: shows the product catalog", groups = "sanity")
    public void showsCatalog() {
        new HomePage(driver()).open();

        expect().condition("title to mention Toolshop", () -> driver().getTitle().contains("Toolshop"));
        expect().visible(NavBar.SIGN_IN);
        expect().visible(HomePage.PRODUCT_NAMES);
    }

    @Test(description = "CAT-02: searching narrows the product list", groups = "sanity")
    public void searchNarrowsList() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.search("hammer");

        expect().visible(HomePage.PRODUCT_NAMES);
        Assert.assertTrue(home.text(HomePage.PRODUCT_NAMES).toLowerCase().contains("hammer"));
    }
}
