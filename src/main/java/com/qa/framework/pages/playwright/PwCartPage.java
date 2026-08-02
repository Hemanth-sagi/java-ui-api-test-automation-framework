package com.qa.framework.pages.playwright;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Shopping cart, driven by Playwright. */
public class PwCartPage extends PwBasePage {

    private final Locator title = testId("title");
    private final Locator items = testId("inventory-item");
    private final Locator itemNames = testId("inventory-item-name");
    private final Locator itemPrices = testId("inventory-item-price");
    private final Locator quantities = testId("item-quantity");
    private final Locator checkoutButton = testId("checkout");
    private final Locator continueShopping = testId("continue-shopping");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/cart.html") && checkoutButton.isVisible();
    }

    public String title() {
        return title.textContent().trim();
    }

    public int itemCount() {
        return items.count();
    }

    public List<String> itemNames() {
        return itemNames.allTextContents().stream().map(String::trim).collect(Collectors.toList());
    }

    public List<BigDecimal> itemPrices() {
        return itemPrices.allTextContents().stream().map(Money::parse).collect(Collectors.toList());
    }

    public List<Integer> itemQuantities() {
        return quantities.allTextContents().stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    public boolean contains(String productName) {
        return itemNames().contains(productName);
    }

    @Step("Proceed to checkout")
    public PwCheckoutInformationPage checkout() {
        checkoutButton.click();
        return new PwCheckoutInformationPage();
    }

    @Step("Continue shopping")
    public PwInventoryPage continueShopping() {
        continueShopping.click();
        return new PwInventoryPage();
    }
}
