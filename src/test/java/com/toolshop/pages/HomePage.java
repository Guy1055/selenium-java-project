package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HomePage extends BasePage {
    /** Searched inside one card, which @FindBy cannot express. */
    private static final By CARD_NAME = testId("product-name");
    private static final By CARD_OUT_OF_STOCK = testId("out-of-stock");

    @FindBy(css = "[data-test='search-query']")
    public WebElement searchInput;

    @FindBy(css = "[data-test='search-submit']")
    public WebElement searchSubmit;

    @FindBy(css = "[data-test='search-reset']")
    public WebElement searchReset;

    @FindBy(css = "[data-test='search-caption']")
    public WebElement searchCaption;

    @FindBy(css = "[data-test='no-results']")
    public WebElement noResults;

    @FindBy(css = "[data-test='product-name']")
    public WebElement productName;

    @FindBy(css = "[data-test='product-name']")
    public List<WebElement> productNames;

    @FindBy(css = "[data-test='product-price']")
    public List<WebElement> productPrices;

    @FindBy(css = "[data-test='sort']")
    public WebElement sort;

    @FindBy(css = "[data-test='pagination-next']")
    public WebElement nextPage;

    @FindBy(css = "[data-test='eco-friendly-filter']")
    public WebElement ecoFilter;

    @FindBy(css = "[data-test='eco-badge']")
    public List<WebElement> ecoBadges;

    @FindBy(css = "[data-test='compare-btn']")
    public List<WebElement> compareButtons;

    @FindBy(css = "[data-test='comparison-bar']")
    public WebElement comparisonBar;

    @FindBy(css = "[data-test='compare-link']")
    public WebElement compareLink;

    @FindBy(css = "[data-test='clear-comparison']")
    public WebElement clearComparison;

    @FindBy(css = "[role='slider'][aria-label='ngx-slider-max']")
    public WebElement priceMaxSlider;

    /** Last resort: product cards have no shared test id. */
    @FindBy(css = "a.card")
    private List<WebElement> cards;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/");
    }

    public void openAndWaitForProducts() {
        open();
        waitVisible(productName);
    }

    public void search(String term) {
        type(searchInput, term);
        click(searchSubmit);
        waitFor("search_completed");
    }

    /** The grid exposes its loading state as a data-test value. */
    public void waitFor(String state) {
        waitAttached(testId(state));
    }

    public void sortBy(String value) {
        selectValue(sort, value);
        waitFor("sorting_completed");
    }

    public List<String> listedNames() {
        return texts(productNames);
    }

    public List<Double> prices() {
        return texts(productPrices).stream().map(t -> Double.parseDouble(t.replaceAll("[^0-9.]", ""))).toList();
    }

    /** Sidebar checkbox for a category or brand, found by its visible label. */
    public By filterCheckbox(String label) {
        return By.xpath("//label[normalize-space(.)=\"" + label + "\"]//input[@type='checkbox']");
    }

    public void filterBy(String label) {
        click(filterCheckbox(label));
    }

    /** Opens the first in-stock card and returns its name. */
    public String openFirstInStockProduct() {
        WebElement card = wait.until(d -> cards.stream()
                .filter(c -> c.findElements(CARD_OUT_OF_STOCK).isEmpty())
                .findFirst().orElse(null));
        String name = card.findElement(CARD_NAME).getText().trim();
        card.click();
        return name;
    }

    public void lowerMaxPrice() {
        wait.until(d -> {
            priceMaxSlider.sendKeys(Keys.PAGE_DOWN);
            return true;
        });
    }

    public int maxPrice() {
        return Integer.parseInt(attribute(priceMaxSlider, "aria-valuenow"));
    }

    public void compareProduct(int index) {
        wait.until(d -> {
            compareButtons.get(index).click();
            return true;
        });
    }
}
