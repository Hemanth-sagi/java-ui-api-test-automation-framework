package com.qa.framework.pages;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Product listing shown immediately after a successful sign-in. */
public class InventoryPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By INVENTORY_ITEM = By.cssSelector("[data-test='inventory-item']");
    private static final By ITEM_NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICE = By.cssSelector("[data-test='inventory-item-price']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By SORT_DROPDOWN = By.cssSelector("[data-test='product-sort-container']");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/inventory.html") && isDisplayed(INVENTORY_ITEM);
    }

    public String title() {
        return textOf(TITLE);
    }

    public int productCount() {
        return countOf(INVENTORY_ITEM);
    }

    public List<String> productNames() {
        return textsOf(ITEM_NAME);
    }

    public List<BigDecimal> productPrices() {
        return textsOf(ITEM_PRICE).stream().map(Money::parse).collect(Collectors.toList());
    }

    /**
     * Adds a product by its display name.
     *
     * <p>Swag Labs derives its button ids from the product name ("Sauce Labs Backpack" becomes
     * {@code add-to-cart-sauce-labs-backpack}), so the test data can stay human-readable while the
     * locator remains a stable id rather than a brittle positional XPath.
     */
    @Step("Add '{productName}' to the cart")
    public InventoryPage addToCart(String productName) {
        click(By.id("add-to-cart-" + slug(productName)));
        log.info("Added '{}' to the cart", productName);
        return this;
    }

    @Step("Remove '{productName}' from the cart")
    public InventoryPage removeFromCart(String productName) {
        click(By.id("remove-" + slug(productName)));
        return this;
    }

    /** @return the number on the cart badge, or 0 when the badge is absent (an empty cart) */
    public int cartCount() {
        return isPresentQuickly(CART_BADGE) ? Integer.parseInt(textOf(CART_BADGE)) : 0;
    }

    @Step("Sort products by '{option}'")
    public InventoryPage sortBy(SortOption option) {
        selectByValue(SORT_DROPDOWN, option.value());
        return this;
    }

    @Step("Open the shopping cart")
    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage();
    }

    private static String slug(String productName) {
        return productName.toLowerCase(Locale.ROOT).replace(" ", "-");
    }

    /** The sort orders offered by the product dropdown. */
    public enum SortOption {
        NAME_A_TO_Z("az"),
        NAME_Z_TO_A("za"),
        PRICE_LOW_TO_HIGH("lohi"),
        PRICE_HIGH_TO_LOW("hilo");

        private final String value;

        SortOption(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
