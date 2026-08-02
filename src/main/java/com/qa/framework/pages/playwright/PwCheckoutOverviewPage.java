package com.qa.framework.pages.playwright;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Checkout step two, driven by Playwright. */
public class PwCheckoutOverviewPage extends PwBasePage {

    private final Locator title = testId("title");
    private final Locator itemNames = testId("inventory-item-name");
    private final Locator itemPrices = testId("inventory-item-price");
    private final Locator subtotalLabel = testId("subtotal-label");
    private final Locator taxLabel = testId("tax-label");
    private final Locator totalLabel = testId("total-label");
    private final Locator finishButton = testId("finish");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/checkout-step-two.html") && finishButton.isVisible();
    }

    public String title() {
        return title.textContent().trim();
    }

    public List<String> itemNames() {
        return itemNames.allTextContents().stream().map(String::trim).collect(Collectors.toList());
    }

    public List<BigDecimal> itemPrices() {
        return itemPrices.allTextContents().stream().map(Money::parse).collect(Collectors.toList());
    }

    public BigDecimal subtotal() {
        return Money.parse(subtotalLabel.textContent());
    }

    public BigDecimal tax() {
        return Money.parse(taxLabel.textContent());
    }

    public BigDecimal total() {
        return Money.parse(totalLabel.textContent());
    }

    @Step("Place the order")
    public PwCheckoutCompletePage finish() {
        finishButton.click();
        return new PwCheckoutCompletePage();
    }
}
