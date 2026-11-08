package com.toolshop.pages.admin;

import com.toolshop.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Add/edit screens for admin records. */
public class AdminFormPage extends BasePage {
    @FindBy(css = "[data-test='name']")
    public WebElement name;

    @FindBy(css = "[data-test='slug']")
    public WebElement slug;

    @FindBy(css = ".alert-success")
    public WebElement success;

    @FindBy(css = "[data-test='description']")
    private WebElement description;

    @FindBy(css = "[data-test='stock']")
    private WebElement stock;

    @FindBy(css = "[data-test='price']")
    private WebElement price;

    @FindBy(css = "[data-test='brand-id']")
    private WebElement brandId;

    @FindBy(css = "[data-test='category-id']")
    private WebElement categoryId;

    @FindBy(css = "[data-test='product-image-id']")
    private WebElement productImageId;

    @FindBy(css = "[data-test='first-name']")
    public WebElement firstName;

    @FindBy(css = "[data-test='last-name']")
    private WebElement lastName;

    @FindBy(css = "[data-test='dob']")
    private WebElement dob;

    @FindBy(css = "[data-test='country']")
    private WebElement country;

    @FindBy(css = "[data-test='postal_code']")
    private WebElement postalCode;

    @FindBy(css = "[data-test='street']")
    private WebElement street;

    @FindBy(css = "[data-test='city']")
    private WebElement city;

    @FindBy(css = "[data-test='state']")
    private WebElement state;

    @FindBy(css = "[data-test='phone']")
    private WebElement phone;

    @FindBy(css = "[data-test='email']")
    public WebElement email;

    @FindBy(css = "[data-test='password']")
    private WebElement password;

    @FindBy(css = "[data-test='failed_login_attempts']")
    private WebElement failedLoginAttempts;

    @FindBy(css = "[data-test='enabled']")
    private WebElement enabled;

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
        type(this.name, name);
        type(this.slug, slug);
    }

    public record ProductForm(String name, String description, String price, String stock,
                              String brandId, String categoryId, String imageId) {
    }

    public void fillProduct(ProductForm product) {
        // The dropdowns load after the page does.
        waitAttached(By.cssSelector("[data-test='brand-id'] option[value='" + product.brandId() + "']"));
        type(name, product.name());
        type(description, product.description());
        type(stock, product.stock());
        type(price, product.price());
        selectValue(brandId, product.brandId());
        selectValue(categoryId, product.categoryId());
        selectValue(productImageId, product.imageId());
    }

    public void fillUser(String firstName, String lastName, String email, String password) {
        type(this.firstName, firstName);
        type(this.lastName, lastName);
        setDate(dob, "1990-01-15");
        selectValue(country, "NL");
        type(postalCode, "1011AB");
        type(street, "Test Street");
        type(city, "Amsterdam");
        type(state, "Noord-Holland");
        type(phone, "0612345678");
        type(this.email, email);
        type(this.password, password);
        // Left empty these two make the API answer 500 (BUG-02).
        type(failedLoginAttempts, "0");
        if (!enabled.isSelected()) {
            enabled.click();
        }
    }
}
