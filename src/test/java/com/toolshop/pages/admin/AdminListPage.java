package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Admin list screens share one layout: title, search form, table, row actions. */
public class AdminListPage extends BasePage {
    public static final By ROWS = By.cssSelector("tbody tr");
    private final String entity;
    private final String path;

    public AdminListPage(WebDriver driver, String entity, String path) {
        super(driver);
        this.entity = entity;
        this.path = path;
    }

    public void open() {
        goTo(path);
        waitVisible(ROWS);
    }

    public By addButton() {
        return testId(entity + "-add");
    }

    public By resetButton() {
        return testId(entity + "-search-reset");
    }

    public void search(String term) {
        type(testId(entity + "-search-query"), term);
        click(testId(entity + "-search-submit"));
    }

    /** Row action buttons embed the record id; the brand ones use a different word order. */
    public By editButton(String id) {
        return testId("brand".equals(entity) ? "brand-" + id + "-edit" : entity + "-edit-" + id);
    }

    public By deleteButton(String id) {
        return testId("brand".equals(entity) ? "brand-" + id + "-delete" : entity + "-delete-" + id);
    }
}
