package com.qa.tests.model;

import java.math.BigDecimal;

import com.qa.framework.utils.Money;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of {@code testdata/product-catalogue.json}: the price a product is expected to advertise. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductFixture {

    private String name;
    private String price;

    public BigDecimal priceAmount() {
        return Money.of(price);
    }

    @Override
    public String toString() {
        return name + " @ $" + price;
    }
}
