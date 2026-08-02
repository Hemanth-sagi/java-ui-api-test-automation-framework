package com.qa.framework.api;

/**
 * Every path the suite calls, in one place.
 *
 * <p>Paths are the API's contract surface. Keeping them here means a versioning change is a
 * one-file edit, and it stops the same URL being spelled three slightly different ways across
 * three test classes.
 */
public final class ApiEndpoints {

    // --- auth ---
    public static final String LOGIN = "/auth/login";
    public static final String CURRENT_USER = "/auth/me";

    // --- users ---
    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{id}";
    public static final String ADD_USER = "/users/add";
    public static final String SEARCH_USERS = "/users/search";

    private ApiEndpoints() {
        // constants only
    }
}
