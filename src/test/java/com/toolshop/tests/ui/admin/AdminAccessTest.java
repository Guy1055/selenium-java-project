package com.toolshop.tests.ui.admin;

import com.toolshop.core.BaseUiTest;
import com.toolshop.pages.BasePage;
import com.toolshop.pages.NavBar;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AdminAccessTest extends BaseUiTest {
    private static final By TITLE = BasePage.testId("page-title");
    private static final By CHART = By.tagName("canvas");

    @DataProvider
    public Object[][] menu() {
        return new Object[][]{
                {"nav-admin-dashboard", "/admin/dashboard"},
                {"nav-admin-brands", "/admin/brands"},
                {"nav-admin-categories", "/admin/categories"},
                {"nav-admin-products", "/admin/products"},
                {"nav-admin-orders", "/admin/orders"},
                {"nav-admin-users", "/admin/users"},
                {"nav-admin-messages", "/admin/messages"},
                {"nav-admin-settings", "/admin/settings"},
        };
    }

    @Test(description = "ADM-01: the admin menu opens each page", dataProvider = "menu")
    public void adminMenu(String id, String path) {
        signInAsAdmin();
        NavBar nav = new NavBar(driver());
        open("/admin/dashboard");

        nav.openUserMenuItem(id);

        expect().url(path + "$");
        expect().visible(TITLE);
    }

    @DataProvider
    public Object[][] reports() {
        return new Object[][]{
                {"nav-admin-statistics", "/admin/reports/statistics"},
                {"nav-average-month-sales", "/admin/reports/average-sales-per-month"},
                {"nav-average-week-sales", "/admin/reports/average-sales-per-week"},
        };
    }

    @Test(description = "ADM-01b: the reports menu opens each report", dataProvider = "reports")
    public void reportsMenu(String id, String path) {
        signInAsAdmin();
        NavBar nav = new NavBar(driver());
        open("/admin/dashboard");

        nav.openReport(id);

        expect().url(path + "$");
        expect().visible(TITLE);
    }

    @DataProvider
    public Object[][] guarded() {
        return new Object[][]{{"/admin/dashboard"}, {"/admin/brands"}, {"/admin/products"}, {"/admin/users"}, {"/admin/orders"}};
    }

    @Test(description = "ADM-02: a guest opening an admin page is sent away", dataProvider = "guarded")
    public void guestBlocked(String path) {
        open(path);

        expect().urlNot(path + "$");
    }

    @Test(description = "ADM-03: the dashboard shows the sales chart and latest orders")
    public void dashboard() {
        signInAsAdmin();

        open("/admin/dashboard");

        expect().text(TITLE, "Sales over the years");
        expect().visible(CHART);
        expect().visible(By.xpath("//h2[normalize-space(.)='Latest orders']"));
    }

    @Test(description = "ADM-04: the statistics report renders its tables")
    public void statistics() {
        signInAsAdmin();

        open("/admin/reports/statistics");

        expect().text(TITLE, "Statistics");
        expect().visible(By.xpath("//h4[normalize-space(.)='Top 10 Best Selling Categories']"));
        expect().visible(By.tagName("table"));
    }

    @DataProvider
    public Object[][] yearReports() {
        return new Object[][]{{"average-sales-per-month"}, {"average-sales-per-week"}};
    }

    @Test(description = "ADM-05: yearly reports have a year selector", dataProvider = "yearReports")
    public void yearSelector(String report) {
        signInAsAdmin();

        open("/admin/reports/" + report);

        expect().visible(BasePage.testId("year"));
        expect().visible(CHART);
    }

    @Test(description = "ADM-06: the settings page opens")
    public void settings() {
        signInAsAdmin();

        open("/admin/settings");

        expect().visible(TITLE);
        expect().visible(BasePage.testId("settings-submit"));
    }
}
