package com.qa.framework.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A user, used both as a request body and as a response model.
 *
 * <p>{@code @JsonInclude(NON_NULL)} is what makes one class serve both directions: a builder that
 * sets only firstName and email produces exactly that JSON, with no nulls for the twenty fields a
 * create request has no business sending.
 *
 * <p>{@code ignoreUnknown} keeps the model focused on the fields the tests assert on, so the API
 * adding a field is not a compile-time event for this suite.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Integer id;
    private String firstName;
    private String lastName;
    private String maidenName;
    private Integer age;
    private String gender;
    private String email;
    private String phone;
    private String username;
    private String birthDate;
    private String image;
    private String bloodGroup;
    private Double height;
    private Double weight;
    private String eyeColor;
    private String role;

    /** Nested object, to exercise multi-level (de)serialisation rather than a flat DTO. */
    private Address address;

    /** Full name as the UI would render it — handy in assertion messages. */
    public String fullName() {
        return String.join(" ", firstName == null ? "" : firstName, lastName == null ? "" : lastName).trim();
    }
}
