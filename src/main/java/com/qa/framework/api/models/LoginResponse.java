package com.qa.framework.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Identity and tokens returned by a successful login. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @ToString.Exclude
    private String accessToken;

    @ToString.Exclude
    private String refreshToken;

    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String image;
}
