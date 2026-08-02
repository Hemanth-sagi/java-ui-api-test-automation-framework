package com.qa.framework.api;

import java.util.Map;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/**
 * Base for every endpoint client: one request specification, one authentication story.
 *
 * <p>Endpoint clients extend this and expose intent-revealing methods ({@code getUser},
 * {@code searchUsers}); no test ever calls {@code given()} directly. That keeps base URI,
 * serialisation, logging and reporting concerns in one place, so switching environments or adding a
 * header happens once rather than in every test file.
 *
 * <p>The specification is immutable and built once during class initialisation. REST Assured builds
 * a fresh request from it per call, so parallel threads share it safely.
 */
public abstract class BaseApiClient {

    private static final Logger LOG = LogManager.getLogger(BaseApiClient.class);

    private static final FrameworkConfig CONFIG = ConfigManager.get();
    private static final RequestSpecification REQUEST_SPEC = buildRequestSpec();
    private static final ResponseSpecification JSON_RESPONSE_SPEC = buildResponseSpec();

    /** Cached bearer token, shared by every client and every thread in the run. */
    private static volatile String cachedToken;

    private static RequestSpecification buildRequestSpec() {
        LOG.info("REST Assured base URI: {}{}", CONFIG.apiBaseUri(), CONFIG.apiBasePath());
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.apiBaseUri())
                .setBasePath(CONFIG.apiBasePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                // AllureRestAssured records the full request and response as report attachments;
                // ApiLoggingFilter mirrors a summary into the run log.
                .addFilter(new AllureRestAssured())
                .addFilter(new ApiLoggingFilter())
                .build();
    }

    private static ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    /** An unauthenticated request. */
    protected RequestSpecification request() {
        return given().spec(REQUEST_SPEC);
    }

    /** A request carrying the bearer token. */
    protected RequestSpecification authenticatedRequest() {
        return given().spec(REQUEST_SPEC).header("Authorization", "Bearer " + token());
    }

    /** Expectations that hold for every JSON response, e.g. {@code response.then().spec(jsonResponse())}. */
    protected ResponseSpecification jsonResponse() {
        return JSON_RESPONSE_SPEC;
    }

    /**
     * Returns a bearer token, minting one on first use.
     *
     * <p>A pre-issued token from {@code api.token} — injected by CI from a secret — always wins.
     * Otherwise the framework logs in once with the configured credentials and reuses the result:
     * authentication is a precondition of the tests, not the thing under test, so paying for it on
     * every call would be waste.
     *
     * <p>Double-checked locking with a {@code volatile} field: the common path is an unsynchronised
     * read, and only the first callers ever contend.
     */
    protected static String token() {
        if (!CONFIG.apiToken().isBlank()) {
            return CONFIG.apiToken();
        }
        String token = cachedToken;
        if (token == null) {
            synchronized (BaseApiClient.class) {
                token = cachedToken;
                if (token == null) {
                    token = login();
                    cachedToken = token;
                }
            }
        }
        return token;
    }

    private static String login() {
        LOG.info("Authenticating as '{}' to obtain a bearer token", CONFIG.apiUsername());
        String token = given().spec(REQUEST_SPEC)
                .body(Map.of("username", CONFIG.apiUsername(), "password", CONFIG.apiPassword()))
                .post(ApiEndpoints.LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Authentication succeeded but returned no accessToken. Check api.username/api.password for env '"
                            + CONFIG.env() + "'.");
        }
        return token;
    }

    /** Drops the cached token so the next authenticated call logs in again. */
    public static void resetToken() {
        synchronized (BaseApiClient.class) {
            cachedToken = null;
        }
    }
}
