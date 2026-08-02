package com.qa.tests.dataproviders;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.qa.framework.utils.CsvDataReader;
import com.qa.framework.utils.JsonDataReader;
import com.qa.tests.model.BasketScenario;
import com.qa.tests.model.CheckoutScenario;
import com.qa.tests.model.LoginScenario;
import com.qa.tests.model.NewUserScenario;
import com.qa.tests.model.ProductFixture;

import org.testng.annotations.DataProvider;

/**
 * Every data provider in the suite, sourced from files rather than from code.
 *
 * <p>Test data lives in {@code src/test/resources/testdata} so that adding a case is an edit to a
 * JSON or CSV file, not a code change and a rebuild. Providers hand back typed objects, so a test
 * signature says what it receives and a typo in a column name fails at load time with the file name
 * in the message.
 *
 * <p>Providers are {@code parallel = false} deliberately. Parallelism here is per method, set in the
 * suite XML; running a provider's rows in parallel as well would multiply browser instances in a way
 * the thread count no longer bounds.
 */
public final class TestDataProviders {

    private static final String LOGIN_SCENARIOS = "testdata/login-scenarios.json";
    private static final String BASKET_SCENARIOS = "testdata/basket-scenarios.json";
    private static final String PRODUCT_CATALOGUE = "testdata/product-catalogue.json";
    private static final String NEW_USERS = "testdata/api-new-users.json";
    private static final String CHECKOUT_CUSTOMERS = "testdata/checkout-customers.csv";

    private TestDataProviders() {
        // static providers
    }

    /** Invalid sign-in attempts and the exact message each must produce. */
    @DataProvider(name = "invalidLogins")
    public static Object[][] invalidLogins() {
        return toRows(JsonDataReader.readList(LOGIN_SCENARIOS, LoginScenario.class));
    }

    /** Baskets to build, with the subtotal each should produce. */
    @DataProvider(name = "baskets")
    public static Object[][] baskets() {
        return toRows(JsonDataReader.readList(BASKET_SCENARIOS, BasketScenario.class));
    }

    /** The advertised price of every product in the catalogue. */
    @DataProvider(name = "catalogue")
    public static Object[][] catalogue() {
        return toRows(JsonDataReader.readList(PRODUCT_CATALOGUE, ProductFixture.class));
    }

    /** Payloads for user creation, including non-ASCII and boundary values. */
    @DataProvider(name = "newUsers")
    public static Object[][] newUsers() {
        return toRows(JsonDataReader.readList(NEW_USERS, NewUserScenario.class));
    }

    /** CSV rows that should pass validation — used by the end-to-end order tests. */
    @DataProvider(name = "validCheckoutCustomers")
    public static Object[][] validCheckoutCustomers() {
        return checkoutRows(CheckoutScenario::expectsSuccess);
    }

    /** CSV rows that should be rejected, with the message each must produce. */
    @DataProvider(name = "invalidCheckoutCustomers")
    public static Object[][] invalidCheckoutCustomers() {
        return checkoutRows(scenario -> !scenario.expectsSuccess());
    }

    private static Object[][] checkoutRows(Predicate<CheckoutScenario> filter) {
        List<Map<String, String>> rows = CsvDataReader.readRows(CHECKOUT_CUSTOMERS);
        return toRows(rows.stream()
                .map(CheckoutScenario::from)
                .filter(filter)
                .collect(Collectors.toList()));
    }

    private static <T> Object[][] toRows(List<T> items) {
        Object[][] rows = new Object[items.size()][1];
        for (int i = 0; i < items.size(); i++) {
            rows[i][0] = items.get(i);
        }
        return rows;
    }
}
