package com.qa.tests.ui.playwright;

import com.qa.framework.pages.playwright.PwInventoryPage;
import com.qa.framework.pages.playwright.PwLoginPage;
import com.qa.tests.base.BasePlaywrightTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.LoginScenario;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * The same authentication journeys as {@code LoginTest}, driven by Playwright.
 *
 * <p>Identical coverage from the identical data file — the point is the comparison. Note the use of
 * {@link PlaywrightAssertions}: unlike a TestNG assert, which evaluates once, a web-first assertion
 * retries until it passes or times out. That removes a whole class of race between "the click
 * happened" and "the DOM caught up".
 */
@Epic("Swag Labs storefront")
@Feature("Authentication (Playwright)")
public class PwLoginTest extends BasePlaywrightTest {

    @Test(groups = {"smoke", "ui", "playwright"},
            description = "A registered shopper signs in and lands on the product list")
    @Story("A registered shopper can sign in")
    @Severity(SeverityLevel.BLOCKER)
    public void standardUserCanSignIn() {
        PwInventoryPage inventory = openLoginPage().loginAs("standard_user", "secret_sauce");

        assertTrue(inventory.isLoaded(),
                "Expected the product list after signing in, but the browser is at " + inventory.currentUrl());
        assertEquals(inventory.title(), "Products",
                "The product list header should read 'Products'");
        assertEquals(inventory.productCount(), 6,
                "The catalogue should list all 6 products for a standard user");
    }

    @Test(dataProvider = "invalidLogins", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "playwright"},
            description = "Invalid sign-in attempts are refused with the correct message")
    @Story("Invalid credentials are refused")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidLoginIsRefused(LoginScenario scenario) {
        PwLoginPage login = openLoginPage().attemptLogin(scenario.getUsername(), scenario.getPassword());

        // Retries until the banner renders — no explicit wait, and no flake if React is slow.
        PlaywrightAssertions.assertThat(login.errorBanner()).hasText(scenario.getExpectedError());

        assertFalse(login.currentUrl().contains("/inventory.html"),
                "A refused sign-in must not reach the product list, but the browser is at " + login.currentUrl());
    }
}
