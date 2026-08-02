package com.qa.framework.pages.playwright;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.playwright.PlaywrightManager;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shared behaviour for the Playwright page objects.
 *
 * <p>Deliberately thinner than its Selenium counterpart, and that contrast is the point of having
 * both stacks here. Playwright's {@link Locator} is lazy and auto-waiting: it resolves at the moment
 * of the action and retries until the element is actionable, so there is no wait helper to write.
 * The Selenium layer needs {@code WaitUtils} to reach the same reliability; this one gets it from
 * the engine.
 */
public abstract class PwBasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final Page page;

    protected PwBasePage() {
        this.page = PlaywrightManager.getPage();
    }

    /** @see com.qa.framework.pages.BasePage#isLoaded() */
    public abstract boolean isLoaded();

    @Step("Open {path}")
    protected void open(String path) {
        String url = ConfigManager.get().webBaseUrl() + path;
        log.info("Navigating to {}", url);
        page.navigate(url);
    }

    public String currentUrl() {
        return page.url();
    }

    public String pageTitle() {
        return page.title();
    }

    protected Locator testId(String id) {
        return page.getByTestId(id);
    }

    /**
     * Visibility check that does not throw.
     *
     * <p>{@code Locator.isVisible()} is the one Playwright call that is <em>not</em> auto-waiting —
     * it answers about the current instant. That is what makes it right for asserting an element is
     * absent, and wrong for waiting on one to appear.
     */
    protected boolean isVisible(Locator locator) {
        return locator.isVisible();
    }
}
