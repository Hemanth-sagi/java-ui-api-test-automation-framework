package com.qa.tests.ui.playwright;

import java.math.BigDecimal;
import java.util.List;

import com.qa.framework.pages.playwright.PwCheckoutCompletePage;
import com.qa.framework.pages.playwright.PwCheckoutInformationPage;
import com.qa.framework.pages.playwright.PwCheckoutOverviewPage;
import com.qa.framework.pages.playwright.PwInventoryPage;
import com.qa.framework.utils.Money;
import com.qa.tests.base.BasePlaywrightTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.CheckoutScenario;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertTrue;

/** The same checkout journeys as {@code CheckoutTest}, driven by Playwright. */
@Epic("Swag Labs storefront")
@Feature("Checkout (Playwright)")
public class PwCheckoutTest extends BasePlaywrightTest {

    private static final String TAX_RATE_PERCENT = "8";

    @Test(groups = {"smoke", "ui", "playwright"},
            description = "An order completes and the summary arithmetic is correct")
    @Story("A shopper can place an order")
    @Severity(SeverityLevel.BLOCKER)
    public void orderSummaryAddsUpAndTheOrderCompletes() {
        PwInventoryPage inventory = loginAsStandardUser();
        inventory.addToCart("Sauce Labs Backpack").addToCart("Sauce Labs Bike Light");

        PwCheckoutOverviewPage overview = inventory.openCart()
                .checkout()
                .enterDetails("Ada", "Lovelace", "SW1A 1AA")
                .continueToOverview();

        BigDecimal subtotal = overview.subtotal();
        BigDecimal tax = overview.tax();
        BigDecimal total = overview.total();
        BigDecimal lineSum = overview.itemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(overview.itemNames(), List.of("Sauce Labs Backpack", "Sauce Labs Bike Light"),
                "The order summary should list exactly the products in the basket");
        softly.assertEquals(subtotal, Money.of("39.98"),
                "Subtotal does not match the expected basket value");
        softly.assertEquals(subtotal, lineSum,
                "Subtotal should equal the sum of the line prices (" + lineSum + ")");
        softly.assertEquals(tax, Money.percentageOf(subtotal, TAX_RATE_PERCENT),
                "Tax should be " + TAX_RATE_PERCENT + "% of the subtotal " + subtotal);
        softly.assertEquals(total, Money.round(subtotal.add(tax)),
                "Total should equal subtotal " + subtotal + " plus tax " + tax);
        softly.assertAll();

        PwCheckoutCompletePage complete = overview.finish();

        PlaywrightAssertions.assertThat(complete.confirmationBanner()).hasText("Thank you for your order!");
        assertTrue(complete.isCartEmpty(),
                "The cart should be empty once the order is placed, but the badge is still showing");
    }

    @Test(dataProvider = "invalidCheckoutCustomers", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "playwright"},
            description = "Incomplete buyer details are rejected with a field-specific message")
    @Story("Checkout validates buyer details")
    @Severity(SeverityLevel.CRITICAL)
    public void incompleteBuyerDetailsAreRejected(CheckoutScenario customer) {
        PwCheckoutInformationPage information = loginAsStandardUser()
                .addToCart("Sauce Labs Onesie")
                .openCart()
                .checkout()
                .enterDetails(customer.getFirstName(), customer.getLastName(), customer.getPostalCode())
                .submitExpectingValidation();

        PlaywrightAssertions.assertThat(information.errorBanner()).hasText(customer.getExpectedError());
        assertTrue(information.currentUrl().contains("/checkout-step-one.html"),
                "A rejected form must keep the shopper on the details page for correction");
    }
}
