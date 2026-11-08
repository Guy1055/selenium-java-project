package com.toolshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class HomePage extends BasePage {
    public static final By SEARCH_INPUT = testId("search-query");
    public static final By SEARCH_SUBMIT = testId("search-submit");
    public static final By SEARCH_RESET = testId("search-reset");
    public static final By SEARCH_CAPTION = testId("search-caption");
    public static final By NO_RESULTS = testId("no-results");
    public static final By PRODUCT_NAMES = testId("product-name");
    public static final By PRODUCT_PRICES = testId("product-price");
    public static final By SORT = testId("sort");
    public static final By NEXT_PAGE = testId("pagination-next");
    public static final By ECO_FILTER = testId("eco-friendly-filter");
    public static final By ECO_BADGES = testId("eco-badge");
    public static final By COMPARE_BUTTONS = testId("compare-btn");
    public static final By COMPARISON_BAR = testId("comparison-bar");
    public static final By COMPARE_LINK = testId("compare-link");
    public static final By CLEAR_COMPARISON = testId("clear-comparison");
    public static final By PRICE_MAX_SLIDER = By.cssSelector("[role='slider'][aria-label='ngx-slider-max']");
    /** Last resort: product cards have no shared test id. */
    private static final By CARDS = By.cssSelector("a.card");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        goTo("/");
    }

    public void openAndWaitForProducts() {
        open();
        waitVisible(PRODUCT_NAMES);
    }

    public void search(String term) {
        type(SEARCH_INPUT, term);
        click(SEARCH_SUBMIT);
        waitFor("search_completed");
    }

    /** The grid exposes its loading state as a data-test value. */
    public void waitFor(String state) {
        waitAttached(testId(state));
    }

    public void sortBy(String value) {
        selectValue(SORT, value);
        waitFor("sorting_completed");
    }

    public List<String> listedNames() {
        return texts(PRODUCT_NAMES);
    }

    public List<Double> prices() {
        return texts(PRODUCT_PRICES).stream().map(t -> Double.parseDouble(t.replaceAll("[^0-9.]", ""))).toList();
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
        WebElement card = wait.until(d -> d.findElements(CARDS).stream()
                .filter(c -> c.findElements(testId("out-of-stock")).isEmpty())
                .findFirst().orElse(null));
        String name = card.findElement(PRODUCT_NAMES).getText().trim();
        card.click();
        return name;
    }

    public void lowerMaxPrice() {
        wait.until(d -> {
            d.findElement(PRICE_MAX_SLIDER).sendKeys(Keys.PAGE_DOWN);
            return true;
        });
    }

    public int maxPrice() {
        return Integer.parseInt(attribute(PRICE_MAX_SLIDER, "aria-valuenow"));
    }

    public void compareProduct(int index) {
        wait.until(d -> {
            d.findElements(COMPARE_BUTTONS).get(index).click();
            return true;
        });
    }
}
