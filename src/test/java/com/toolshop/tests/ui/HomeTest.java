package com.toolshop.tests.ui;

import com.toolshop.core.BaseUiTest;
import com.toolshop.pages.HomePage;
import com.toolshop.pages.NavBar;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomeTest extends BaseUiTest {
    @Test(description = "CAT-01: shows the product catalog", groups = "sanity")
    public void showsCatalog() {
        NavBar nav = new NavBar(driver());
        HomePage home = new HomePage(driver());
        home.open();

        expect().condition("title to mention Toolshop", () -> driver().getTitle().contains("Toolshop"));
        expect().visible(nav.signIn);
        expect().visible(home.productName);
    }

    @Test(description = "CAT-02: searching narrows the product list", groups = "sanity")
    public void searchNarrowsList() {
        HomePage home = new HomePage(driver());
        home.openAndWaitForProducts();

        home.search("hammer");

        expect().visible(home.productName);
        Assert.assertTrue(home.text(home.productName).toLowerCase().contains("hammer"));
    }
}
