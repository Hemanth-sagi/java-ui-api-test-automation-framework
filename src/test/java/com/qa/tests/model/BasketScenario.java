package com.qa.tests.model;

import java.math.BigDecimal;
import java.util.List;

import com.qa.framework.utils.Money;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of {@code testdata/basket-scenarios.json}: what to add, and what it should cost. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasketScenario {

    private String scenario;
    private List<String> products;
    private String expectedSubtotal;

    public BigDecimal expectedSubtotalAmount() {
        return Money.of(expectedSubtotal);
    }

    @Override
    public String toString() {
        return scenario;
    }
}
