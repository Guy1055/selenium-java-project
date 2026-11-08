package com.toolshop.core;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Starts a browser per test and saves a screenshot when one fails. */
public abstract class BaseUiTest extends BaseApiTest {
    protected WebDriver driver() {
        return DriverFactory.current();
    }

    protected Expect expect() {
        return new Expect(driver());
    }

    protected void open(String path) {
        driver().get(Env.BASE_URL + path);
    }

    /** Signs the browser in by seeding the token the Angular app reads on start. */
    protected void signIn(String token) {
        open("/privacy");
        ((JavascriptExecutor) driver()).executeScript("window.localStorage.setItem('auth-token', arguments[0]);", token);
    }

    protected Customer signedInCustomer() {
        Customer customer = newCustomer();
        signIn(customer.token());
        return customer;
    }

    protected void signInAsAdmin() {
        signIn(adminToken());
    }

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverFactory.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser(ITestResult result) {
        if (!DriverFactory.isRunning()) {
            return;
        }
        if (result.getStatus() == ITestResult.FAILURE) {
            saveScreenshot(result);
        }
        DriverFactory.quit();
    }

    private void saveScreenshot(ITestResult result) {
        try {
            Path target = Path.of("target", "screenshots", result.getMethod().getMethodName() + "-" + System.nanoTime() + ".png");
            Files.createDirectories(target.getParent());
            Files.write(target, ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES));
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
