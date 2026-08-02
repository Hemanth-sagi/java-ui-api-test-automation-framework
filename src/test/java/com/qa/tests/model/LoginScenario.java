package com.qa.tests.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of {@code testdata/login-scenarios.json}.
 *
 * <p>{@code toString()} is what TestNG prints beside the invocation in the report, so it is written
 * to read as the case name rather than as a field dump.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginScenario {

    private String scenario;
    private String username;
    private String password;
    private String expectedError;

    @Override
    public String toString() {
        return scenario;
    }
}
