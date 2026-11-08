package com.toolshop.core;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/** One driver per test method. Selenium Manager resolves the matching chromedriver. */
public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver start() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900", "--disable-search-engine-choice-screen");
        if (Env.HEADLESS) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        if (Env.HTTPS_PROXY != null) {
            // Corporate network: Chrome ignores HTTPS_PROXY unless it is passed explicitly.
            String hostAndPort = Env.HTTPS_PROXY.replaceFirst("^https?://", "");
            options.setProxy(new Proxy().setHttpProxy(hostAndPort).setSslProxy(hostAndPort));
        }
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver current() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No driver started on this thread");
        }
        return driver;
    }

    public static boolean isRunning() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
