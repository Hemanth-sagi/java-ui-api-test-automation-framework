package com.qa.tests.base;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;
import com.qa.framework.drivers.DriverManager;
import com.qa.framework.pages.InventoryPage;
import com.qa.framework.pages.LoginPage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Lifecycle for Selenium tests: a fresh browser per test method.
 *
 * <p>Per-method rather than per-class is the trade that makes the suite trustworthy in parallel.
 * A shared browser leaks cookies and cart state between tests, so a failure in one silently changes
 * the meaning of the next; a fresh session costs a few seconds and buys genuine independence.
 *
 * <p>Teardown is in an {@code alwaysRun} hook, so a browser is closed even when setup or the test
 * itself throws — otherwise a failing suite would leave orphaned processes behind on the agent.
 */
public abstract class BaseWebTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final FrameworkConfig config = ConfigManager.get();

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverManager.startDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        DriverManager.quitDriver();
    }

    /** Opens the site and signs in as the standard user — the precondition of most journeys. */
    protected InventoryPage loginAsStandardUser() {
        return new LoginPage().open().loginAs("standard_user", "secret_sauce");
    }

    protected LoginPage openLoginPage() {
        return new LoginPage().open();
    }
}
