package com.qa.framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Order confirmation — the end of the happy path. */
public class CheckoutCompletePage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");
    private static final By COMPLETE_TEXT = By.cssSelector("[data-test='complete-text']");
    private static final By BACK_HOME = By.cssSelector("[data-test='back-to-products']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-complete.html") && isDisplayed(COMPLETE_HEADER);
    }

    public String title() {
        return textOf(TITLE);
    }

    public String confirmationHeader() {
        return textOf(COMPLETE_HEADER);
    }

    public String confirmationMessage() {
        return textOf(COMPLETE_TEXT);
    }

    /** A completed order must leave the cart empty — the badge disappears entirely. */
    public boolean isCartEmpty() {
        return !isPresentQuickly(CART_BADGE);
    }

    @Step("Return to the product list")
    public InventoryPage backToProducts() {
        click(BACK_HOME);
        return new InventoryPage();
    }
}
