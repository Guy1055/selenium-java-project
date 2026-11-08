package com.toolshop.tests.quality;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.toolshop.core.BaseUiTest;
import com.toolshop.core.KnownBug;
import com.toolshop.pages.BasePage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = "a11y")
public class AccessibilityTest extends BaseUiTest {
    @DataProvider
    public Object[][] pages() {
        return new Object[][]{
                {"catalog", "/", "product-name"},
                {"login", "/auth/login", "login-submit"},
                {"register", "/auth/register", "register-submit"},
                {"forgot password", "/auth/forgot-password", "forgot-password-submit"},
                {"contact", "/contact", "contact-submit"},
        };
    }

    @Test(description = "A11Y-01: public pages have no critical violations", dataProvider = "pages")
    @KnownBug(value = "BUG-07: the show/hide password button has no accessible name", onlyFor = {"login", "register"})
    public void noCriticalViolations(String name, String path, String ready) {
        open(path);
        expect().visible(BasePage.testId(ready));

        Results results = new AxeBuilder().analyze(driver());

        List<String> critical = results.getViolations().stream()
                .filter(v -> "critical".equals(v.getImpact()))
                .map(Rule::getId)
                .toList();
        Assert.assertEquals(critical, List.of(), name);
    }
}
