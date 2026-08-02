package com.qa.framework.pages.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Order confirmation, driven by Playwright. */
public class PwCheckoutCompletePage extends PwBasePage {

    private final Locator completeHeader = testId("complete-header");
    private final Locator completeText = testId("complete-text");
    private final Locator backHome = testId("back-to-products");
    private final Locator cartBadge = testId("shopping-cart-badge");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-complete.html") && completeHeader.isVisible();
    }

    /** Exposed as a locator so tests can assert on it with Playwright's retrying assertions. */
    public Locator confirmationBanner() {
        return completeHeader;
    }

    public String confirmationHeader() {
        return completeHeader.textContent().trim();
    }

    public String confirmationMessage() {
        return completeText.textContent().trim();
    }

    public boolean isCartEmpty() {
        return !cartBadge.isVisible();
    }

    @Step("Return to the product list")
    public PwInventoryPage backToProducts() {
        backHome.click();
        return new PwInventoryPage();
    }
}
