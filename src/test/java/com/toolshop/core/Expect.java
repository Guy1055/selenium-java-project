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

    public void visible(By by) {
        that(by + " to be visible", () -> driver.findElement(by).isDisplayed());
    }

    public void visible(WebElement element) {
        that(element + " to be visible", element::isDisplayed);
    }

    public void hidden(WebElement element) {
        that(element + " to be hidden", () -> {
            try {
                return !element.isDisplayed();
            } catch (NoSuchElementException e) {
                return true;
            }
        });
    }

    public void text(By by, String expected) {
        that(by + " to have text '" + expected + "'", () -> driver.findElement(by).getText().trim().equals(expected));
    }

    public void text(WebElement element, String expected) {
        that(element + " to have text '" + expected + "', was '" + safeText(element) + "'",
                () -> element.getText().trim().equals(expected));
    }

    public void containsText(By by, String expected) {
        that(by + " to contain '" + expected + "'", () -> driver.findElement(by).getText().contains(expected));
    }

    public void containsText(WebElement element, String expected) {
        that(element + " to contain '" + expected + "', was '" + safeText(element) + "'",
                () -> element.getText().contains(expected));
    }

    public void value(WebElement element, String expected) {
        that(element + " to have value '" + expected + "'", () -> expected.equals(element.getDomProperty("value")));
    }

    public void count(List<WebElement> elements, int expected) {
        that("element count to be " + expected + ", was " + elements.size(), () -> elements.size() == expected);
    }

    public void disabled(WebElement element) {
        that(element + " to be disabled", () -> !element.isEnabled());
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

    public void texts(List<WebElement> elements, List<String> expected) {
        that("elements to list " + expected, () -> elements.stream().map(e -> e.getText().trim()).toList().equals(expected));
    }

    private String safeText(WebElement element) {
        try {
            return element.getText();
        } catch (RuntimeException e) {
            return "<absent>";
        }
    }
}
