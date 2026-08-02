package com.qa.framework.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Credentials posted to the login endpoint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {

    private String username;

    /** Excluded from {@code toString()} so a logged request body can never leak a password. */
    @ToString.Exclude
    private String password;

    /** Token lifetime in minutes; omitted from the payload when null. */
    private Integer expiresInMins;
}
