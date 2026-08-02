package com.qa.framework.pages.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Checkout step one, driven by Playwright. */
public class PwCheckoutInformationPage extends PwBasePage {

    private final Locator firstName = testId("firstName");
    private final Locator lastName = testId("lastName");
    private final Locator postalCode = testId("postalCode");
    private final Locator continueButton = testId("continue");
    private final Locator cancelButton = testId("cancel");
    private final Locator errorMessage = testId("error");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-step-one.html") && continueButton.isVisible();
    }

    @Step("Enter buyer details: {first} {last}, {postcode}")
    public PwCheckoutInformationPage enterDetails(String first, String last, String postcode) {
        firstName.fill(first);
        lastName.fill(last);
        postalCode.fill(postcode);
        return this;
    }

    @Step("Continue to the order overview")
    public PwCheckoutOverviewPage continueToOverview() {
        continueButton.click();
        PwCheckoutOverviewPage overview = new PwCheckoutOverviewPage();
        if (!overview.isLoaded()) {
            throw new IllegalStateException("Checkout did not advance to the overview. "
                    + (hasError() ? "Page reported: " + errorMessage() : "No error message was shown."));
        }
        return overview;
    }

    @Step("Submit the checkout form")
    public PwCheckoutInformationPage submitExpectingValidation() {
        continueButton.click();
        return this;
    }

    @Step("Cancel checkout")
    public PwCartPage cancel() {
        cancelButton.click();
        return new PwCartPage();
    }

    public Locator errorBanner() {
        return errorMessage;
    }

    public String errorMessage() {
        return errorMessage.textContent().trim();
    }

    public boolean hasError() {
        return errorMessage.isVisible();
    }
}
