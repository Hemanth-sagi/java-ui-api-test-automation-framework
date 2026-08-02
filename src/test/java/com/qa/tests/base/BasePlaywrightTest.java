package com.qa.tests.base;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;
import com.qa.framework.pages.playwright.PwInventoryPage;
import com.qa.framework.pages.playwright.PwLoginPage;
import com.qa.framework.playwright.PlaywrightManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Lifecycle for Playwright tests — the same contract as {@link BaseWebTest}, so the two UI suites
 * are directly comparable rather than being two different styles of test.
 */
public abstract class BasePlaywrightTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final FrameworkConfig config = ConfigManager.get();

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        PlaywrightManager.startPage();
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        PlaywrightManager.closePage();
    }

    protected PwInventoryPage loginAsStandardUser() {
        return new PwLoginPage().open().loginAs("standard_user", "secret_sauce");
    }

    protected PwLoginPage openLoginPage() {
        return new PwLoginPage().open();
    }
}
