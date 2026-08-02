package com.qa.framework.pages;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Checkout step two — the order summary.
 *
 * <p>The money on this page is the reason the flow is worth automating at all, so the page exposes
 * subtotal, tax and total as {@link BigDecimal} rather than as display strings. Tests can then
 * assert the arithmetic the business actually cares about instead of matching text.
 */
public class CheckoutOverviewPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEM_NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICE = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SUBTOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL = By.cssSelector("[data-test='total-label']");
    private static final By FINISH = By.cssSelector("[data-test='finish']");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-step-two.html") && isDisplayed(FINISH);
    }

    public String title() {
        return textOf(TITLE);
    }

    public List<String> itemNames() {
        return textsOf(ITEM_NAME);
    }

    public List<BigDecimal> itemPrices() {
        return textsOf(ITEM_PRICE).stream().map(Money::parse).collect(Collectors.toList());
    }

    /** @return the "Item total" line, i.e. the sum of line prices before tax */
    public BigDecimal subtotal() {
        return Money.parse(textOf(SUBTOTAL));
    }

    public BigDecimal tax() {
        return Money.parse(textOf(TAX));
    }

    public BigDecimal total() {
        return Money.parse(textOf(TOTAL));
    }

    @Step("Place the order")
    public CheckoutCompletePage finish() {
        click(FINISH);
        return new CheckoutCompletePage();
    }
}
