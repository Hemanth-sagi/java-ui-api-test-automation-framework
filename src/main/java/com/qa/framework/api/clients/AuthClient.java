package com.qa.framework.api.clients;

import com.qa.framework.api.ApiEndpoints;
import com.qa.framework.api.BaseApiClient;
import com.qa.framework.api.models.LoginRequest;

import io.qameta.allure.Step;
import io.restassured.response.Response;

/** Authentication endpoints. */
public class AuthClient extends BaseApiClient {

    @Step("POST /auth/login as '{request.username}'")
    public Response login(LoginRequest request) {
        return request()
                .body(request)
                .post(ApiEndpoints.LOGIN);
    }

    /** Login with a raw body, so negative tests can send malformed or partial payloads. */
    @Step("POST /auth/login with a raw payload")
    public Response login(Object rawBody) {
        return request()
                .body(rawBody)
                .post(ApiEndpoints.LOGIN);
    }

    @Step("GET /auth/me with a valid token")
    public Response currentUser() {
        return authenticatedRequest().get(ApiEndpoints.CURRENT_USER);
    }

    /** Fetches the current user with a caller-supplied token — used to assert rejection paths. */
    @Step("GET /auth/me with a supplied token")
    public Response currentUser(String bearerToken) {
        return request()
                .header("Authorization", "Bearer " + bearerToken)
                .get(ApiEndpoints.CURRENT_USER);
    }

    @Step("GET /auth/me without a token")
    public Response currentUserWithoutToken() {
        return request().get(ApiEndpoints.CURRENT_USER);
    }
}
