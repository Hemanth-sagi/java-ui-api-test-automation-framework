package com.qa.tests.ui.playwright;

import java.math.BigDecimal;

import com.qa.framework.pages.playwright.PwCartPage;
import com.qa.framework.pages.playwright.PwInventoryPage;
import com.qa.tests.base.BasePlaywrightTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.BasketScenario;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** The same basket journeys as {@code CartTest}, driven by Playwright. */
@Epic("Swag Labs storefront")
@Feature("Shopping cart (Playwright)")
public class PwCartTest extends BasePlaywrightTest {

    @Test(groups = {"smoke", "ui", "playwright"},
            description = "Adding one product updates the cart badge")
    @Story("Products can be added to the basket")
    @Severity(SeverityLevel.BLOCKER)
    public void addingAProductUpdatesTheCartBadge() {
        PwInventoryPage inventory = loginAsStandardUser();
        assertEquals(inventory.cartCount(), 0, "Precondition: the cart should start empty");

        inventory.addToCart("Sauce Labs Backpack");

        PlaywrightAssertions.assertThat(inventory.cartBadge()).hasText("1");
        assertTrue(inventory.openCart().contains("Sauce Labs Backpack"),
                "The backpack should be listed in the cart");
    }

    @Test(dataProvider = "baskets", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "playwright"},
            description = "The cart lists exactly what was added, once each")
    @Story("Products can be added to the basket")
    @Severity(SeverityLevel.CRITICAL)
    public void cartContentsMatchWhatWasAdded(BasketScenario basket) {
        PwInventoryPage inventory = loginAsStandardUser();
        basket.getProducts().forEach(inventory::addToCart);

        PwCartPage cart = inventory.openCart();

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(cart.itemCount(), basket.getProducts().size(),
                "Wrong number of lines in the cart");
        softly.assertEquals(cart.itemNames(), basket.getProducts(),
                "The cart should list exactly the products added, in the order they were added");
        softly.assertEquals(cart.itemQuantities(), basket.getProducts().stream().map(product -> 1).toList(),
                "Each distinct product should appear once, with quantity 1");

        BigDecimal linesTotal = cart.itemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        softly.assertEquals(linesTotal, basket.expectedSubtotalAmount(),
                "The cart's line prices should add up to the expected subtotal");
        softly.assertAll();
    }
}
