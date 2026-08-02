package com.qa.tests.ui.selenium;

import java.math.BigDecimal;
import java.util.List;

import com.qa.framework.pages.CartPage;
import com.qa.framework.pages.InventoryPage;
import com.qa.tests.base.BaseWebTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.BasketScenario;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Basket behaviour: adding, removing and persistence. */
@Epic("Swag Labs storefront")
@Feature("Shopping cart")
public class CartTest extends BaseWebTest {

    @Test(groups = {"smoke", "ui", "selenium"},
            description = "Adding one product updates the cart badge")
    @Story("Products can be added to the basket")
    @Severity(SeverityLevel.BLOCKER)
    public void addingAProductUpdatesTheCartBadge() {
        InventoryPage inventory = loginAsStandardUser();
        assertEquals(inventory.cartCount(), 0, "Precondition: the cart should start empty");

        inventory.addToCart("Sauce Labs Backpack");

        assertEquals(inventory.cartCount(), 1,
                "The cart badge should show 1 item after adding the backpack");
        assertTrue(inventory.openCart().contains("Sauce Labs Backpack"),
                "The backpack should be listed in the cart");
    }

    @Test(dataProvider = "baskets", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "The cart lists exactly what was added, once each")
    @Story("Products can be added to the basket")
    @Severity(SeverityLevel.CRITICAL)
    public void cartContentsMatchWhatWasAdded(BasketScenario basket) {
        InventoryPage inventory = loginAsStandardUser();
        basket.getProducts().forEach(inventory::addToCart);

        assertEquals(inventory.cartCount(), basket.getProducts().size(),
                "The cart badge should count every product added for '" + basket.getScenario() + "'");

        CartPage cart = inventory.openCart();

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

        // One report entry listing every mismatch, rather than stopping at the first.
        softly.assertAll();
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "Removing one product leaves the rest of the basket untouched")
    @Story("Products can be removed from the basket")
    @Severity(SeverityLevel.CRITICAL)
    public void removingAProductLeavesTheRestOfTheBasket() {
        InventoryPage inventory = loginAsStandardUser();
        inventory.addToCart("Sauce Labs Backpack")
                .addToCart("Sauce Labs Bike Light")
                .addToCart("Sauce Labs Onesie");

        CartPage cart = inventory.openCart().remove("Sauce Labs Bike Light");

        List<String> remaining = cart.itemNames();
        assertEquals(remaining, List.of("Sauce Labs Backpack", "Sauce Labs Onesie"),
                "Only the bike light should have been removed");
        assertFalse(cart.contains("Sauce Labs Bike Light"),
                "The removed product should no longer be in the cart");
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "The basket survives a trip back to the product list")
    @Story("The basket persists across navigation")
    @Severity(SeverityLevel.NORMAL)
    public void basketSurvivesContinueShopping() {
        InventoryPage inventory = loginAsStandardUser().addToCart("Sauce Labs Fleece Jacket");

        InventoryPage backOnListing = inventory.openCart().continueShopping();

        assertEquals(backOnListing.cartCount(), 1,
                "The cart badge should still show the jacket after returning to the listing");
        assertTrue(backOnListing.openCart().contains("Sauce Labs Fleece Jacket"),
                "The jacket should still be in the cart");
    }
}
