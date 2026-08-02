package com.qa.tests.ui.selenium;

import java.math.BigDecimal;

import com.qa.framework.pages.CheckoutCompletePage;
import com.qa.framework.pages.CheckoutInformationPage;
import com.qa.framework.pages.CheckoutOverviewPage;
import com.qa.framework.pages.InventoryPage;
import com.qa.framework.utils.Money;
import com.qa.tests.base.BaseWebTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.BasketScenario;
import com.qa.tests.model.CheckoutScenario;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Checkout — the money path.
 *
 * <p>These are the assertions that justify automating the UI at all. Anything cheaper can be tested
 * below the browser; whether the customer is charged the right amount cannot.
 */
@Epic("Swag Labs storefront")
@Feature("Checkout")
public class CheckoutTest extends BaseWebTest {

    /** Swag Labs charges 8% sales tax on the item subtotal. */
    private static final String TAX_RATE_PERCENT = "8";

    @Test(dataProvider = "baskets", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "An order completes and the summary arithmetic is correct")
    @Story("A shopper can place an order")
    @Severity(SeverityLevel.BLOCKER)
    public void orderSummaryAddsUpAndTheOrderCompletes(BasketScenario basket) {
        InventoryPage inventory = loginAsStandardUser();
        basket.getProducts().forEach(inventory::addToCart);

        CheckoutOverviewPage overview = inventory.openCart()
                .checkout()
                .enterDetails("Ada", "Lovelace", "SW1A 1AA")
                .continueToOverview();

        BigDecimal subtotal = overview.subtotal();
        BigDecimal tax = overview.tax();
        BigDecimal total = overview.total();
        BigDecimal lineSum = overview.itemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(overview.itemNames(), basket.getProducts(),
                "The order summary should list exactly the products in the basket");
        softly.assertEquals(subtotal, basket.expectedSubtotalAmount(),
                "Subtotal does not match the expected basket value");
        softly.assertEquals(subtotal, lineSum,
                "Subtotal should equal the sum of the line prices (" + lineSum + ")");
        softly.assertEquals(tax, Money.percentageOf(subtotal, TAX_RATE_PERCENT),
                "Tax should be " + TAX_RATE_PERCENT + "% of the subtotal " + subtotal);
        softly.assertEquals(total, Money.round(subtotal.add(tax)),
                "Total should equal subtotal " + subtotal + " plus tax " + tax);
        softly.assertAll();

        CheckoutCompletePage complete = overview.finish();

        assertEquals(complete.confirmationHeader(), "Thank you for your order!",
                "The confirmation header is the shopper's only signal the order went through");
        assertTrue(complete.isCartEmpty(),
                "The cart should be empty once the order is placed, but the badge is still showing");
    }

    @Test(dataProvider = "validCheckoutCustomers", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "Valid buyer details are accepted, including non-ASCII names")
    @Story("A shopper can place an order")
    @Severity(SeverityLevel.CRITICAL)
    public void validBuyerDetailsAreAccepted(CheckoutScenario customer) {
        CheckoutCompletePage complete = loginAsStandardUser()
                .addToCart("Sauce Labs Onesie")
                .openCart()
                .checkout()
                .enterDetails(customer.getFirstName(), customer.getLastName(), customer.getPostalCode())
                .continueToOverview()
                .finish();

        assertTrue(complete.isLoaded(),
                "Scenario '" + customer.getScenario() + "' should have reached the confirmation page");
        assertEquals(complete.confirmationHeader(), "Thank you for your order!",
                "Wrong confirmation for scenario '" + customer.getScenario() + "'");
    }

    @Test(dataProvider = "invalidCheckoutCustomers", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "Incomplete buyer details are rejected with a field-specific message")
    @Story("Checkout validates buyer details")
    @Severity(SeverityLevel.CRITICAL)
    public void incompleteBuyerDetailsAreRejected(CheckoutScenario customer) {
        CheckoutInformationPage information = loginAsStandardUser()
                .addToCart("Sauce Labs Onesie")
                .openCart()
                .checkout()
                .enterDetails(customer.getFirstName(), customer.getLastName(), customer.getPostalCode())
                .submitExpectingValidation();

        assertTrue(information.hasError(),
                "Scenario '" + customer.getScenario() + "' should have been rejected but no error was shown");
        assertEquals(information.errorMessage(), customer.getExpectedError(),
                "Wrong validation message for scenario '" + customer.getScenario() + "'");
        assertTrue(information.currentUrl().contains("/checkout-step-one.html"),
                "A rejected form must keep the shopper on the details page for correction");
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "Cancelling checkout returns to the cart with the basket intact")
    @Story("Checkout can be abandoned safely")
    @Severity(SeverityLevel.NORMAL)
    public void cancellingCheckoutKeepsTheBasket() {
        boolean stillHasProduct = loginAsStandardUser()
                .addToCart("Sauce Labs Backpack")
                .openCart()
                .checkout()
                .cancel()
                .contains("Sauce Labs Backpack");

        assertTrue(stillHasProduct,
                "Abandoning checkout must not empty the basket — the shopper may want to come back to it");
    }
}
