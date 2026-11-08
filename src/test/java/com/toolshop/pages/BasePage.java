package com.toolshop.pages;

import com.toolshop.core.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/** Locator helpers and auto-waiting actions shared by every page object. No sleeps. */
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

    public void selectValue(By by, String value) {
        wait.until(d -> {
            new Select(d.findElement(by)).selectByValue(value);
            return true;
        });
    }

    public void waitVisible(By by) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitAttached(By by) {
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public String text(By by) {
        return wait.until(d -> d.findElement(by).getText()).trim();
    }

    public String attribute(By by, String name) {
        return wait.until(d -> d.findElement(by).getDomAttribute(name));
    }

    public List<String> texts(By by) {
        return wait.until(d -> d.findElements(by).stream().map(e -> e.getText().trim()).toList());
    }

    public int count(By by) {
        return driver.findElements(by).size();
    }
}
