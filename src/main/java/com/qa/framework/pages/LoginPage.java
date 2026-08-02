package com.qa.framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Swag Labs sign-in page.
 *
 * <p>Locators prefer {@code data-test} attributes over CSS classes or XPath: they are contract
 * hooks the application team maintains for automation, so a restyle does not break the suite.
 */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.cssSelector("[data-test='username']");
    private static final By PASSWORD = By.cssSelector("[data-test='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("[data-test='login-button']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

    @Override
    public boolean isLoaded() {
        return isDisplayed(LOGIN_BUTTON);
    }

    @Step("Open the login page")
    public LoginPage open() {
        open("/");
        return this;
    }

    /**
     * Submits credentials and lands on the products page.
     *
     * <p>Returning the next page object is what makes a test read as a journey. It also means a
     * failed login surfaces here, at the transition, rather than as a puzzling missing element in
     * whatever the test tried to do next.
     */
    @Step("Log in as '{username}'")
    public InventoryPage loginAs(String username, String password) {
        submitCredentials(username, password);
        InventoryPage inventoryPage = new InventoryPage();
        if (!inventoryPage.isLoaded()) {
            throw new IllegalStateException(
                    "Login as '" + username + "' did not reach the products page. "
                            + (hasError() ? "Page reported: " + errorMessage() : "No error message was shown."));
        }
        return inventoryPage;
    }

    /** Submits credentials without asserting the outcome — for negative cases that stay on this page. */
    @Step("Attempt login as '{username}'")
    public LoginPage attemptLogin(String username, String password) {
        submitCredentials(username, password);
        return this;
    }

    private void submitCredentials(String username, String password) {
        log.info("Signing in as '{}'", username);
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    @Step("Read the login error message")
    public String errorMessage() {
        return textOf(ERROR_MESSAGE);
    }

    public boolean hasError() {
        return isPresentQuickly(ERROR_MESSAGE);
    }
}
