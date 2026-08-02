package com.qa.tests.ui.selenium;

import com.qa.framework.pages.InventoryPage;
import com.qa.framework.pages.LoginPage;
import com.qa.tests.base.BaseWebTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.LoginScenario;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Authentication journeys, driven by Selenium. */
@Epic("Swag Labs storefront")
@Feature("Authentication")
public class LoginTest extends BaseWebTest {

    @Test(groups = {"smoke", "ui", "selenium"},
            description = "A registered shopper signs in and lands on the product list")
    @Story("A registered shopper can sign in")
    @Severity(SeverityLevel.BLOCKER)
    public void standardUserCanSignIn() {
        InventoryPage inventory = openLoginPage().loginAs("standard_user", "secret_sauce");

        assertTrue(inventory.isLoaded(),
                "Expected the product list after signing in, but the browser is at " + inventory.currentUrl());
        assertEquals(inventory.title(), "Products",
                "The product list header should read 'Products'");
        assertEquals(inventory.productCount(), 6,
                "The catalogue should list all 6 products for a standard user");
        assertEquals(inventory.cartCount(), 0,
                "A newly signed-in shopper should start with an empty cart");
    }

    @Test(dataProvider = "invalidLogins", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "Invalid sign-in attempts are refused with the correct message")
    @Story("Invalid credentials are refused")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidLoginIsRefused(LoginScenario scenario) {
        LoginPage login = openLoginPage().attemptLogin(scenario.getUsername(), scenario.getPassword());

        assertTrue(login.hasError(),
                "Expected an error banner for scenario '" + scenario.getScenario() + "' but none was shown");

        // Asserting the exact message, not merely that some error appeared: the wording is the
        // requirement here — it is what stops the form leaking which of the two fields was wrong.
        assertEquals(login.errorMessage(), scenario.getExpectedError(),
                "Wrong error message for scenario '" + scenario.getScenario() + "'");

        assertFalse(login.currentUrl().contains("/inventory.html"),
                "A refused sign-in must not reach the product list, but the browser is at " + login.currentUrl());
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "The locked-out account is refused even with the correct password")
    @Story("Invalid credentials are refused")
    @Severity(SeverityLevel.CRITICAL)
    public void lockedOutUserCannotSignInWithValidPassword() {
        LoginPage login = openLoginPage().attemptLogin("locked_out_user", "secret_sauce");

        assertEquals(login.errorMessage(), "Epic sadface: Sorry, this user has been locked out.",
                "A locked-out account should be told it is locked, not that the credentials are wrong");
        assertFalse(login.currentUrl().contains("/inventory.html"),
                "A locked-out account must never reach the product list");
    }
}
