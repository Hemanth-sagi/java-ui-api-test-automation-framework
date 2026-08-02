package com.qa.tests.ui.selenium;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.qa.framework.pages.InventoryPage;
import com.qa.tests.base.BaseWebTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.ProductFixture;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Product listing: what is on sale, at what price, and in what order. */
@Epic("Swag Labs storefront")
@Feature("Product listing")
public class ProductListingTest extends BaseWebTest {

    @Test(dataProvider = "catalogue", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "ui", "selenium"},
            description = "Each catalogued product is listed at its expected price")
    @Story("Products are advertised at the correct price")
    @Severity(SeverityLevel.CRITICAL)
    public void productIsListedAtTheExpectedPrice(ProductFixture expected) {
        InventoryPage inventory = loginAsStandardUser();

        List<String> names = inventory.productNames();
        int index = names.indexOf(expected.getName());
        assertTrue(index >= 0,
                "Product '" + expected.getName() + "' is missing from the listing. Listed: " + names);

        BigDecimal listedPrice = inventory.productPrices().get(index);
        assertEquals(listedPrice, expected.priceAmount(),
                "Wrong price for '" + expected.getName() + "'");
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "Sorting by price ascending reorders the grid")
    @Story("Shoppers can sort the catalogue")
    @Severity(SeverityLevel.NORMAL)
    public void sortingByPriceAscendingOrdersTheGrid() {
        InventoryPage inventory = loginAsStandardUser().sortBy(InventoryPage.SortOption.PRICE_LOW_TO_HIGH);

        List<BigDecimal> prices = inventory.productPrices();
        List<BigDecimal> expected = prices.stream().sorted().toList();

        assertEquals(prices, expected,
                "Prices should ascend after sorting low-to-high, but were " + prices);
    }

    @Test(groups = {"regression", "ui", "selenium"},
            description = "Sorting by name descending reorders the grid")
    @Story("Shoppers can sort the catalogue")
    @Severity(SeverityLevel.NORMAL)
    public void sortingByNameDescendingOrdersTheGrid() {
        InventoryPage inventory = loginAsStandardUser().sortBy(InventoryPage.SortOption.NAME_Z_TO_A);

        List<String> names = inventory.productNames();
        List<String> expected = names.stream().sorted(Comparator.reverseOrder()).toList();

        assertEquals(names, expected,
                "Names should descend after sorting Z-to-A, but were " + names);
    }
}
