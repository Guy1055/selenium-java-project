package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Add/edit screens for admin records. Fields are addressed by their data-test value. */
public class AdminFormPage extends BasePage {
    public static final By NAME = testId("name");
    public static final By SLUG = testId("slug");
    public static final By SUCCESS = By.cssSelector(".alert-success");

    public AdminFormPage(WebDriver driver) {
        super(driver);
    }

    public void open(String path) {
        goTo(path);
    }

    public void submit(String entity) {
        click(testId(entity + "-submit"));
    }

    public void fillBrandOrCategory(String name, String slug) {
        type(NAME, name);
        type(SLUG, slug);
    }

    public record ProductForm(String name, String description, String price, String stock,
                              String brandId, String categoryId, String imageId) {
    }

    public void fillProduct(ProductForm product) {
        // The dropdowns load after the page does.
        waitAttached(By.cssSelector("[data-test='brand-id'] option[value='" + product.brandId() + "']"));
        type(NAME, product.name());
        type(testId("description"), product.description());
        type(testId("stock"), product.stock());
        type(testId("price"), product.price());
        selectValue(testId("brand-id"), product.brandId());
        selectValue(testId("category-id"), product.categoryId());
        selectValue(testId("product-image-id"), product.imageId());
    }

    public void fillUser(String firstName, String lastName, String email, String password) {
        type(testId("first-name"), firstName);
        type(testId("last-name"), lastName);
        setDate(testId("dob"), "1990-01-15");
        selectValue(testId("country"), "NL");
        type(testId("postal_code"), "1011AB");
        type(testId("street"), "Test Street");
        type(testId("city"), "Amsterdam");
        type(testId("state"), "Noord-Holland");
        type(testId("phone"), "0612345678");
        type(testId("email"), email);
        type(testId("password"), password);
        // Left empty these two make the API answer 500 (BUG-02).
        type(testId("failed_login_attempts"), "0");
        WebElement enabled = driver.findElement(testId("enabled"));
        if (!enabled.isSelected()) {
            enabled.click();
        }
    }
}
