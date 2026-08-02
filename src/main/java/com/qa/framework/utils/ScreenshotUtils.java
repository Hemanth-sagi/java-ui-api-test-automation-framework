package com.qa.framework.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.qa.framework.drivers.DriverManager;
import com.qa.framework.playwright.PlaywrightManager;

import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Captures screenshots from whichever UI stack the failing test was driving.
 *
 * <p>Bytes are returned rather than only written to disk, because the listener needs them twice:
 * once as a file (for the ExtentReports HTML) and once as an in-memory attachment (for Allure).
 * Every method is failure-tolerant — a screenshot that cannot be taken must never replace the real
 * assertion failure with an unrelated exception, which would hide the actual defect.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LogManager.getLogger(ScreenshotUtils.class);
    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private ScreenshotUtils() {
        // static utility
    }

    /** @return PNG bytes from the active Selenium or Playwright session, or an empty array */
    public static byte[] capture() {
        if (DriverManager.hasDriver()) {
            return captureSelenium();
        }
        if (PlaywrightManager.hasPage()) {
            return capturePlaywright();
        }
        LOG.debug("No active UI session — skipping screenshot");
        return new byte[0];
    }

    /** Writes PNG bytes under {@code target/screenshots}. @return the file, or {@code null} */
    public static Path save(byte[] png, String testName) {
        if (png == null || png.length == 0) {
            return null;
        }
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Path file = SCREENSHOT_DIR.resolve(
                    sanitise(testName) + "-" + LocalDateTime.now().format(TIMESTAMP) + ".png");
            Files.write(file, png);
            LOG.info("Screenshot saved to {}", file);
            return file;
        } catch (IOException e) {
            LOG.warn("Could not save screenshot for {}: {}", testName, e.getMessage());
            return null;
        }
    }

    private static byte[] captureSelenium() {
        try {
            return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (RuntimeException e) {
            LOG.warn("Selenium screenshot failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    private static byte[] capturePlaywright() {
        try {
            return PlaywrightManager.getPage().screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (RuntimeException e) {
            LOG.warn("Playwright screenshot failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    private static String sanitise(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
