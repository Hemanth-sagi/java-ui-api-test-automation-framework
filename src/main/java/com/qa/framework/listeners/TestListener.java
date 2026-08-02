package com.qa.framework.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.drivers.DriverManager;
import com.qa.framework.playwright.PlaywrightManager;
import com.qa.framework.reporting.ExtentManager;
import com.qa.framework.utils.ScreenshotUtils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * The single place where a test result turns into evidence.
 *
 * <p>Logging, screenshots, Allure attachments and the ExtentReports node all hang off these
 * callbacks rather than off the tests themselves. That is the whole point: a test method should
 * read as a description of behaviour, and nothing in it should be about reporting plumbing.
 *
 * <p>Registered once per suite in the TestNG XML. Allure's own listener is picked up separately
 * through the ServiceLoader, so it needs no entry there.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);
    private static final String SEPARATOR = "-".repeat(90);

    @Override
    public void onStart(ISuite suite) {
        LOG.info(SEPARATOR);
        LOG.info("SUITE START  : {} | env={} | browser={} | headless={}",
                suite.getName(),
                ConfigManager.get().env(),
                ConfigManager.get().browser(),
                ConfigManager.get().headless());
        LOG.info(SEPARATOR);
    }

    @Override
    public void onFinish(ISuite suite) {
        // Extent writes nothing to disk until it is flushed, and this is the last hook that runs.
        ExtentManager.flush();
        LOG.info("SUITE FINISH : {}", suite.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        LOG.info("TEST BLOCK START : {} (thread-count={})", context.getName(), context.getSuite().getXmlSuite().getThreadCount());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentManager.createTest(displayName(result), description(result));
        LOG.info(">>> START  {} {}", displayName(result), parameters(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log(Status.PASS, "Test passed in " + durationSeconds(result) + "s");
        LOG.info("<<< PASS   {} ({}s)", displayName(result), durationSeconds(result));
        ExtentManager.unload();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        LOG.error("<<< FAIL   {} ({}s) : {}", displayName(result), durationSeconds(result),
                error == null ? "no throwable" : error.getMessage());

        attachEvidence(result);

        ExtentTest test = ExtentManager.getTest();
        if (test != null && error != null) {
            test.fail(error);
        }
        ExtentManager.unload();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log(Status.SKIP, "Test skipped: " + (result.getThrowable() == null ? "" : result.getThrowable().getMessage()));
        LOG.warn("<<< SKIP   {}", displayName(result));
        ExtentManager.unload();
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info("TEST BLOCK FINISH: {} | passed={} failed={} skipped={}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    /**
     * Captures whatever the failing session can still give us.
     *
     * <p>Runs before the {@code @AfterMethod} teardown closes the browser, which is the only window
     * in which a screenshot is still possible. Every step is individually guarded — evidence
     * collection must never throw, or it would mask the assertion failure that caused it.
     */
    private void attachEvidence(ITestResult result) {
        if (!ConfigManager.get().screenshotOnFailure()) {
            return;
        }
        String testName = displayName(result);

        if (DriverManager.hasDriver() || PlaywrightManager.hasPage()) {
            byte[] screenshot = ScreenshotUtils.capture();
            if (screenshot.length > 0) {
                Allure.getLifecycle().addAttachment("Screenshot on failure", "image/png", "png", screenshot);

                Path saved = ScreenshotUtils.save(screenshot, testName);
                ExtentTest test = ExtentManager.getTest();
                if (test != null && saved != null) {
                    // Base64 keeps the Extent HTML a single portable file, with no sidecar images
                    // to lose when it is attached to a ticket.
                    test.fail("Screenshot at point of failure",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(
                                    Base64.getEncoder().encodeToString(screenshot)).build());
                }
            }
        }

        if (PlaywrightManager.hasPage()) {
            attachPlaywrightTrace(testName);
        }
    }

    private void attachPlaywrightTrace(String testName) {
        Path trace = PlaywrightManager.stopTracing(testName);
        if (trace == null) {
            return;
        }
        try {
            Allure.getLifecycle().addAttachment(
                    "Playwright trace (npx playwright show-trace)", "application/zip", "zip", Files.readAllBytes(trace));
        } catch (IOException e) {
            LOG.warn("Could not attach Playwright trace: {}", e.getMessage());
        }
    }

    private void log(Status status, String message) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(status, message);
        }
    }

    private static String displayName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getName();
    }

    private static String description(ITestResult result) {
        String description = result.getMethod().getDescription();
        return description == null || description.isBlank() ? "" : description;
    }

    private static String parameters(ITestResult result) {
        Object[] parameters = result.getParameters();
        return parameters == null || parameters.length == 0 ? "" : Arrays.toString(parameters);
    }

    private static String durationSeconds(ITestResult result) {
        return String.format("%.2f", (result.getEndMillis() - result.getStartMillis()) / 1000.0);
    }
}
