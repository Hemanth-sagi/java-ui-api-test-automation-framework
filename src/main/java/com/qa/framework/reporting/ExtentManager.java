package com.qa.framework.reporting;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Owns the single {@link ExtentReports} instance and the per-thread {@link ExtentTest} node.
 *
 * <p>Allure and ExtentReports are both wired up on purpose, because they answer different
 * questions. Allure is the CI-facing report: history, trends, severities, published to Pages.
 * ExtentReports is the self-contained HTML file you can attach to a ticket or email to someone who
 * will not clone the repo. Both are fed from the one listener, so neither can drift from reality.
 *
 * <p>{@code ExtentReports} is itself thread-safe; the per-test node is not, hence the
 * {@link ThreadLocal}.
 */
public final class ExtentManager {

    private static final Logger LOG = LogManager.getLogger(ExtentManager.class);
    private static final String REPORT_PATH = "target/extent-report/index.html";

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private static ExtentReports extent;

    private ExtentManager() {
        // static holder
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            extent = build();
        }
        return extent;
    }

    private static ExtentReports build() {
        FrameworkConfig config = ConfigManager.get();

        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("UI + API Automation Report");
        spark.config().setReportName("java-ui-api-test-automation-framework");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);

        // Recorded in the report header so a result set is always traceable to the run that produced it.
        reports.setSystemInfo("Environment", config.env());
        reports.setSystemInfo("Browser", config.browser());
        reports.setSystemInfo("Headless", String.valueOf(config.headless()));
        reports.setSystemInfo("Web Base URL", config.webBaseUrl());
        reports.setSystemInfo("API Base URI", config.apiBaseUri());
        reports.setSystemInfo("Java", System.getProperty("java.version"));
        reports.setSystemInfo("OS", System.getProperty("os.name"));

        LOG.info("ExtentReports initialised -> {}", REPORT_PATH);
        return reports;
    }

    public static ExtentTest createTest(String name, String description) {
        ExtentTest test = getInstance().createTest(name, description);
        CURRENT_TEST.set(test);
        return test;
    }

    /** @return the node for the calling thread, or {@code null} if no test is running on it */
    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static void unload() {
        CURRENT_TEST.remove();
    }

    /** Writes the HTML report. Nothing is on disk until this runs, so it must happen once per suite. */
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
            LOG.info("ExtentReports written -> {}", REPORT_PATH);
        }
    }
}
