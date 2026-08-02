package com.qa.tests.model;

import com.qa.framework.api.models.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of {@code testdata/api-new-users.json}.
 *
 * <p>Carries a label alongside the payload and converts to the production {@link User} model on
 * demand, so the request body sent by the test is the same type the framework uses everywhere else.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewUserScenario {

    private String scenario;
    private String firstName;
    private String lastName;
    private Integer age;
    private String gender;
    private String email;
    private String username;

    public User toUser() {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .gender(gender)
                .email(email)
                .username(username)
                .build();
    }

    @Override
    public String toString() {
        return scenario;
    }
}
