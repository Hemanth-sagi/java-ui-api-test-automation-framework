package com.qa.framework.api.clients;

import com.qa.framework.api.ApiEndpoints;
import com.qa.framework.api.BaseApiClient;
import com.qa.framework.api.models.User;

import io.qameta.allure.Step;
import io.restassured.response.Response;

/**
 * User CRUD endpoints.
 *
 * <p>Every method returns the raw {@link Response} rather than a deserialised model. That is
 * intentional: a client that returned {@code User} could only ever be used by happy-path tests,
 * because a 404 has no user to return. Handing back the response lets the same client serve status
 * code, header, schema and negative assertions, and callers that want the model just call
 * {@code response.as(User.class)}.
 */
public class UserClient extends BaseApiClient {

    @Step("GET /users/{id}")
    public Response getUser(int id) {
        return request()
                .pathParam("id", id)
                .get(ApiEndpoints.USER_BY_ID);
    }

    @Step("GET /users?limit={limit}&skip={skip}")
    public Response getUsers(int limit, int skip) {
        return request()
                .queryParam("limit", limit)
                .queryParam("skip", skip)
                .get(ApiEndpoints.USERS);
    }

    @Step("GET /users/search?q={query}")
    public Response searchUsers(String query) {
        return request()
                .queryParam("q", query)
                .get(ApiEndpoints.SEARCH_USERS);
    }

    @Step("POST /users/add")
    public Response createUser(User user) {
        return request()
                .body(user)
                .post(ApiEndpoints.ADD_USER);
    }

    /** Creates with a raw body, so negative tests can post malformed JSON. */
    @Step("POST /users/add with a raw payload")
    public Response createUser(Object rawBody) {
        return request()
                .body(rawBody)
                .post(ApiEndpoints.ADD_USER);
    }

    @Step("PUT /users/{id}")
    public Response updateUser(int id, User user) {
        return request()
                .pathParam("id", id)
                .body(user)
                .put(ApiEndpoints.USER_BY_ID);
    }

    @Step("DELETE /users/{id}")
    public Response deleteUser(int id) {
        return request()
                .pathParam("id", id)
                .delete(ApiEndpoints.USER_BY_ID);
    }
}
