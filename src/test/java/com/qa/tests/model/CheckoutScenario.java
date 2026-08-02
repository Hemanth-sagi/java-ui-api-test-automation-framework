package com.qa.tests.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of {@code testdata/checkout-customers.csv}, mapped out of the raw column map. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutScenario {

    private String scenario;
    private String firstName;
    private String lastName;
    private String postalCode;

    /** Empty for rows that are expected to pass validation. */
    private String expectedError;

    public static CheckoutScenario from(Map<String, String> row) {
        return new CheckoutScenario(
                row.get("scenario"),
                row.get("firstName"),
                row.get("lastName"),
                row.get("postalCode"),
                row.getOrDefault("expectedError", ""));
    }

    public boolean expectsSuccess() {
        return expectedError == null || expectedError.isBlank();
    }

    @Override
    public String toString() {
        return scenario;
    }
}
