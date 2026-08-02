package com.qa.tests.api;

import java.util.Map;

import com.qa.framework.api.models.LoginRequest;
import com.qa.framework.api.models.LoginResponse;
import com.qa.tests.base.BaseApiTest;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/** Authentication contract: issuing tokens, and refusing to serve without one. */
@Epic("DummyJSON API")
@Feature("Authentication")
public class AuthApiTest extends BaseApiTest {

    @Test(groups = {"smoke", "api"},
            description = "A valid login returns a JWT and the caller's profile")
    @Story("Valid credentials are exchanged for a token")
    @Severity(SeverityLevel.BLOCKER)
    public void validLoginReturnsTokenAndProfile() {
        LoginRequest credentials = LoginRequest.builder()
                .username(config.apiUsername())
                .password(config.apiPassword())
                .expiresInMins(30)
                .build();

        Response response = authClient.login(credentials);

        assertEquals(response.statusCode(), 200,
                "Login should succeed for the configured credentials. Body: " + response.asString());

        // The schema asserts the whole shape at once, including that both tokens are structurally
        // JWTs — a check that no amount of field-by-field assertion would cover as cheaply.
        response.then().body(matchesSchema("login-response-schema.json"));

        LoginResponse body = response.as(LoginResponse.class);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(body.getUsername(), config.apiUsername(),
                "The profile returned should belong to the user who signed in");
        softly.assertEquals(body.getAccessToken().split("\\.").length, 3,
                "The access token should be a three-part JWT");
        softly.assertNotNull(body.getId(), "A signed-in user must have an id");
        softly.assertTrue(body.getEmail() != null && body.getEmail().contains("@"),
                "The profile should carry a usable email address, but was: " + body.getEmail());
        softly.assertAll();
    }

    @Test(groups = {"smoke", "api"},
            description = "A valid token identifies the caller at /auth/me")
    @Story("A token grants access to the caller's own profile")
    @Severity(SeverityLevel.CRITICAL)
    public void validTokenReturnsTheCurrentUser() {
        Response response = authClient.currentUser();

        assertEquals(response.statusCode(), 200,
                "A valid bearer token should be accepted. Body: " + response.asString());
        assertEquals(response.jsonPath().getString("username"), config.apiUsername(),
                "/auth/me should describe the user the token was issued for");
    }

    @Test(groups = {"regression", "api"},
            description = "Requests without a token are rejected")
    @Story("Protected endpoints require a token")
    @Severity(SeverityLevel.CRITICAL)
    public void missingTokenIsUnauthorised() {
        Response response = authClient.currentUserWithoutToken();

        assertEquals(response.statusCode(), 401,
                "An unauthenticated request must be refused with 401, not served. Body: " + response.asString());
        response.then().body(matchesSchema("error-schema.json"));
    }

    @Test(groups = {"regression", "api"},
            description = "Requests with a malformed token are rejected")
    @Story("Protected endpoints require a token")
    @Severity(SeverityLevel.CRITICAL)
    public void malformedTokenIsUnauthorised() {
        Response response = authClient.currentUser("not.a.valid.token");

        assertEquals(response.statusCode(), 401,
                "A malformed token must be refused with 401. Body: " + response.asString());
        assertEquals(response.jsonPath().getString("message"), "Invalid/Expired Token!",
                "The rejection should say the token is invalid or expired");
    }

    @Test(groups = {"regression", "api"},
            description = "A wrong password is refused, and the response leaks nothing")
    @Story("Invalid credentials are refused")
    @Severity(SeverityLevel.CRITICAL)
    public void wrongPasswordIsRefused() {
        Response response = authClient.login(LoginRequest.builder()
                .username(config.apiUsername())
                .password("definitely-not-the-password")
                .build());

        assertEquals(response.statusCode(), 400,
                "A wrong password should be rejected. Body: " + response.asString());
        assertEquals(response.jsonPath().getString("message"), "Invalid credentials",
                "The rejection message should not reveal whether the username exists");
        assertTrue(response.jsonPath().get("accessToken") == null,
                "A failed login must not return a token");
    }

    @Test(groups = {"regression", "api"},
            description = "A login missing its password field is rejected, not accepted as blank")
    @Story("Invalid credentials are refused")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithoutPasswordFieldIsRefused() {
        Response response = authClient.login(Map.of("username", config.apiUsername()));

        assertNotEquals(response.statusCode(), 200,
                "A payload with no password must never authenticate. Body: " + response.asString());
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                "A malformed login is a client error, so expect 4xx but got " + response.statusCode());
    }
}
