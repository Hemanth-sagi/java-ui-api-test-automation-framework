package com.qa.framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Checkout step one — the buyer's details. */
public class CheckoutInformationPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE = By.cssSelector("[data-test='continue']");
    private static final By CANCEL = By.cssSelector("[data-test='cancel']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-step-one.html") && isDisplayed(CONTINUE);
    }

    public String title() {
        return textOf(TITLE);
    }

    @Step("Enter buyer details: {firstName} {lastName}, {postalCode}")
    public CheckoutInformationPage enterDetails(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        return this;
    }

    /** Continues to the overview, failing loudly if validation kept us on this page. */
    @Step("Continue to the order overview")
    public CheckoutOverviewPage continueToOverview() {
        click(CONTINUE);
        CheckoutOverviewPage overview = new CheckoutOverviewPage();
        if (!overview.isLoaded()) {
            throw new IllegalStateException("Checkout did not advance to the overview. "
                    + (hasError() ? "Page reported: " + errorMessage() : "No error message was shown."));
        }
        return overview;
    }

    /** Submits without asserting the outcome — for validation cases that stay on this page. */
    @Step("Submit the checkout form")
    public CheckoutInformationPage submitExpectingValidation() {
        click(CONTINUE);
        return this;
    }

    @Step("Cancel checkout")
    public CartPage cancel() {
        click(CANCEL);
        return new CartPage();
    }

    public String errorMessage() {
        return textOf(ERROR_MESSAGE);
    }

    public boolean hasError() {
        return isPresentQuickly(ERROR_MESSAGE);
    }
}
