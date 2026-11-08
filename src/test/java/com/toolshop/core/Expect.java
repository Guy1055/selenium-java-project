package com.toolshop.core;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

/** Retrying assertions, the Selenium counterpart of Playwright's web-first assertions. */
public final class Expect {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebDriver driver;

    public Expect(WebDriver driver) {
        this.driver = driver;
    }

    private void that(String description, BooleanSupplier condition) {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.ignoring(StaleElementReferenceException.class).ignoring(NoSuchElementException.class);
        try {
            wait.until(d -> condition.getAsBoolean());
        } catch (TimeoutException e) {
            Assert.fail("Timed out waiting: " + description);
        }
    }

    private WebElement element(By by) {
        return driver.findElement(by);
    }

    public void visible(By by) {
        that(by + " to be visible", () -> element(by).isDisplayed());
    }

    public void hidden(By by) {
        that(by + " to be hidden", () -> driver.findElements(by).stream().noneMatch(WebElement::isDisplayed));
    }

    public void attached(By by) {
        that(by + " to be attached", () -> !driver.findElements(by).isEmpty());
    }

    public void text(By by, String expected) {
        that(by + " to have text '" + expected + "', was '" + safeText(by) + "'",
                () -> element(by).getText().trim().equals(expected));
    }

    public void containsText(By by, String expected) {
        that(by + " to contain '" + expected + "', was '" + safeText(by) + "'",
                () -> element(by).getText().contains(expected));
    }

    public void value(By by, String expected) {
        that(by + " to have value '" + expected + "'", () -> expected.equals(element(by).getDomProperty("value")));
    }

    public void count(By by, int expected) {
        that(by + " count to be " + expected + ", was " + driver.findElements(by).size(),
                () -> driver.findElements(by).size() == expected);
    }

    public void disabled(By by) {
        that(by + " to be disabled", () -> !element(by).isEnabled());
    }

    public void url(String regex) {
        Pattern pattern = Pattern.compile(regex);
        that("URL to match " + regex + ", was " + driver.getCurrentUrl(),
                () -> pattern.matcher(driver.getCurrentUrl()).find());
    }

    public void urlNot(String regex) {
        Pattern pattern = Pattern.compile(regex);
        that("URL not to match " + regex + ", was " + driver.getCurrentUrl(),
                () -> !pattern.matcher(driver.getCurrentUrl()).find());
    }

    public void condition(String description, BooleanSupplier condition) {
        that(description, condition);
    }

    public void texts(By by, List<String> expected) {
        that(by + " to list " + expected, () -> driver.findElements(by).stream()
                .map(e -> e.getText().trim()).toList().equals(expected));
    }

    private String safeText(By by) {
        try {
            List<WebElement> found = driver.findElements(by);
            return found.isEmpty() ? "<absent>" : found.get(0).getText();
        } catch (RuntimeException e) {
            return "<unreadable>";
        }
    }
}
