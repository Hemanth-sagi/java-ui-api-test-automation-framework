package com.qa.framework.pages;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.qa.framework.utils.Money;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Shopping cart — the review step before checkout. */
public class CartPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By CART_ITEM = By.cssSelector("[data-test='inventory-item']");
    private static final By ITEM_NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICE = By.cssSelector("[data-test='inventory-item-price']");
    private static final By ITEM_QUANTITY = By.cssSelector("[data-test='item-quantity']");
    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");
    private static final By CONTINUE_SHOPPING = By.cssSelector("[data-test='continue-shopping']");

    @Override
    public boolean isLoaded() {
        return currentUrl().contains("/cart.html") && isDisplayed(TITLE);
    }

    public String title() {
        return textOf(TITLE);
    }

    public int itemCount() {
        return countOf(CART_ITEM);
    }

    public List<String> itemNames() {
        return itemCount() == 0 ? List.of() : textsOf(ITEM_NAME);
    }

    public List<BigDecimal> itemPrices() {
        return itemCount() == 0 ? List.of() : textsOf(ITEM_PRICE).stream().map(Money::parse).collect(Collectors.toList());
    }

    /** @return the quantity shown against each line, in display order */
    public List<Integer> itemQuantities() {
        return itemCount() == 0 ? List.of()
                : textsOf(ITEM_QUANTITY).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public boolean contains(String productName) {
        return itemNames().contains(productName);
    }

    @Step("Remove '{productName}' from the cart")
    public CartPage remove(String productName) {
        click(By.id("remove-" + productName.toLowerCase(Locale.ROOT).replace(" ", "-")));
        return this;
    }

    @Step("Proceed to checkout")
    public CheckoutInformationPage checkout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutInformationPage();
    }

    @Step("Continue shopping")
    public InventoryPage continueShopping() {
        click(CONTINUE_SHOPPING);
        return new InventoryPage();
    }
}
