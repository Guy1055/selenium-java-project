package com.toolshop.tests.quality;

import com.toolshop.core.BaseUiTest;
import com.toolshop.core.Env;
import com.toolshop.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Screenshots are per-OS, so baselines exist for the local machine only and CI excludes this group.
 * Only form regions are compared: the catalog and banners change on the shared demo.
 */
@Test(groups = "visual")
public class VisualTest extends BaseUiTest {
    private static final Path BASELINES = Path.of("src", "test", "resources", "baselines");
    private static final double MAX_DIFF_RATIO = 0.01;

    @DataProvider
    public Object[][] regions() {
        return new Object[][]{
                {"login-form", "/auth/login"},
                {"forgot-password-form", "/auth/forgot-password"},
        };
    }

    @Test(description = "VIS-01: stable forms match their baseline", dataProvider = "regions")
    public void matchesBaseline(String region, String path) throws IOException {
        open(path);
        By by = BasePage.testId(region);
        expect().visible(by);
        WebElement form = driver().findElement(by);
        byte[] shot = form.getScreenshotAs(OutputType.BYTES);
        Path baseline = BASELINES.resolve(region + "-" + System.getProperty("os.name").split(" ")[0].toLowerCase() + ".png");

        if (Env.UPDATE_SNAPSHOTS) {
            Files.createDirectories(BASELINES);
            Files.write(baseline, shot);
            return;
        }
        if (!Files.exists(baseline)) {
            throw new SkipException("No baseline yet. Run once with UPDATE_SNAPSHOTS=1");
        }

        double ratio = diffRatio(ImageIO.read(baseline.toFile()), ImageIO.read(new ByteArrayInputStream(shot)));
        Assert.assertTrue(ratio <= MAX_DIFF_RATIO, region + " differs by " + ratio);
    }

    private double diffRatio(BufferedImage expected, BufferedImage actual) {
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            return 1.0;
        }
        long different = 0;
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                if (expected.getRGB(x, y) != actual.getRGB(x, y)) {
                    different++;
                }
            }
        }
        return (double) different / (expected.getWidth() * expected.getHeight());
    }
}
