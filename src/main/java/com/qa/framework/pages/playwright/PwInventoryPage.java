package com.qa.framework.pages.playwright;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

/** Product listing, driven by Playwright. */
public class PwInventoryPage extends PwBasePage {

    private final Locator title = testId("title");
    private final Locator items = testId("inventory-item");
    private final Locator itemNames = testId("inventory-item-name");
    private final Locator itemPrices = testId("inventory-item-price");
    private final Locator cartLink = testId("shopping-cart-link");
    private final Locator cartBadge = testId("shopping-cart-badge");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/inventory.html") && items.first().isVisible();
    }

    public String title() {
        return title.textContent().trim();
    }

    public int productCount() {
        return items.count();
    }

    public List<String> productNames() {
        return itemNames.allTextContents().stream().map(String::trim).collect(Collectors.toList());
    }

    public List<BigDecimal> productPrices() {
        return itemPrices.allTextContents().stream().map(Money::parse).collect(Collectors.toList());
    }

    @Step("Add '{productName}' to the cart")
    public PwInventoryPage addToCart(String productName) {
        testId("add-to-cart-" + slug(productName)).click();
        log.info("Added '{}' to the cart", productName);
        return this;
    }

    @Step("Remove '{productName}' from the cart")
    public PwInventoryPage removeFromCart(String productName) {
        testId("remove-" + slug(productName)).click();
        return this;
    }

    public int cartCount() {
        return cartBadge.isVisible() ? Integer.parseInt(cartBadge.textContent().trim()) : 0;
    }

    /** Exposed for web-first assertions such as {@code assertThat(page.cartBadge()).hasText("2")}. */
    public Locator cartBadge() {
        return cartBadge;
    }

    @Step("Open the shopping cart")
    public PwCartPage openCart() {
        cartLink.click();
        return new PwCartPage();
    }

    private static String slug(String productName) {
        return productName.toLowerCase(Locale.ROOT).replace(" ", "-");
    }
}
