package com.toolshop.pages;

import com.toolshop.core.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Auto-waiting actions shared by every page object. No sleeps.
 * Elements are declared with @FindBy; each use looks the element up again, so Angular re-renders do not leave it stale.
 * By is kept for locators built at run time.
 */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(ElementNotInteractableException.class)
                .ignoring(NoSuchElementException.class);
        PageFactory.initElements(driver, this);
    }

    /** This project's test id attribute is data-test. */
    public static By testId(String id) {
        return By.cssSelector("[data-test='" + id + "']");
    }

    /** A toast or alert containing the given text. */
    public static By alertWith(String text) {
        return By.xpath("//*[@role='alert'][contains(normalize-space(.), \"" + text + "\")]");
    }

    protected void goTo(String path) {
        driver.get(Env.BASE_URL + path);
    }

    public void click(By by) {
        wait.until(d -> {
            WebElement element = d.findElement(by);
            element.click();
            return true;
        });
    }

    public void type(By by, String text) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
        element.clear();
        element.sendKeys(text);
    }

    public void click(WebElement element) {
        wait.until(d -> {
            element.click();
            return true;
        });
    }

    public void type(WebElement element, String text) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.clear();
        element.sendKeys(text);
    }

    /** sendKeys on a native date input depends on the browser locale, so set the ISO value directly. */
    public void setDate(WebElement element, String isoDate) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                element, isoDate);
    }

    public void selectValue(WebElement element, String value) {
        wait.until(d -> {
            new Select(element).selectByValue(value);
            return true;
        });
    }

    public void waitVisible(By by) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitAttached(By by) {
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public String text(WebElement element) {
        return wait.until(d -> element.getText()).trim();
    }

    public String attribute(WebElement element, String name) {
        return wait.until(d -> element.getDomAttribute(name));
    }

    public List<String> texts(List<WebElement> elements) {
        return wait.until(d -> elements.stream().map(e -> e.getText().trim()).toList());
    }

    public int count(List<WebElement> elements) {
        return elements.size();
    }
}
