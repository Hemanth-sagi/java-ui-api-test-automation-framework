package com.qa.framework.pages.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Swag Labs sign-in page, driven by Playwright. */
public class PwLoginPage extends PwBasePage {

    private final Locator username = testId("username");
    private final Locator password = testId("password");
    private final Locator loginButton = testId("login-button");
    private final Locator errorMessage = testId("error");

    @Override
    public boolean isLoaded() {
        return isVisible(loginButton);
    }

    @Step("Open the login page")
    public PwLoginPage open() {
        open("/");
        return this;
    }

    @Step("Log in as '{user}'")
    public PwInventoryPage loginAs(String user, String pass) {
        submitCredentials(user, pass);
        PwInventoryPage inventoryPage = new PwInventoryPage();
        if (!inventoryPage.isLoaded()) {
            throw new IllegalStateException(
                    "Login as '" + user + "' did not reach the products page. "
                            + (hasError() ? "Page reported: " + errorMessage() : "No error message was shown."));
        }
        return inventoryPage;
    }

    @Step("Attempt login as '{user}'")
    public PwLoginPage attemptLogin(String user, String pass) {
        submitCredentials(user, pass);
        return this;
    }

    private void submitCredentials(String user, String pass) {
        log.info("Signing in as '{}'", user);
        username.fill(user);
        password.fill(pass);
        loginButton.click();
    }

    /** Exposed as a locator so tests can use Playwright's auto-retrying web-first assertions. */
    public Locator errorBanner() {
        return errorMessage;
    }

    public String errorMessage() {
        return errorMessage.textContent().trim();
    }

    public boolean hasError() {
        return isVisible(errorMessage);
    }
}
