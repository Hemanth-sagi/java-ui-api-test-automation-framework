package com.qa.tests.api;

import java.util.List;
import java.util.Locale;

import com.qa.framework.api.models.User;
import com.qa.framework.api.models.UserListResponse;
import com.qa.tests.base.BaseApiTest;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Search and the edges of pagination. */
@Epic("DummyJSON API")
@Feature("User search")
public class UserSearchApiTest extends BaseApiTest {

    @Test(groups = {"regression", "api"},
            description = "Every search result actually matches the query")
    @Story("Users can be searched by free text")
    @Severity(SeverityLevel.CRITICAL)
    public void everySearchResultMatchesTheQuery() {
        String query = "Emily";

        Response response = userClient.searchUsers(query);

        assertEquals(response.statusCode(), 200, "Search should succeed. Body: " + response.asString());
        response.then().body(matchesSchema("user-list-schema.json"));

        UserListResponse results = response.as(UserListResponse.class);
        assertFalse(results.getUsers().isEmpty(),
                "'" + query + "' is known to match at least one user, but nothing came back");

        // The assertion that matters: not "some results came back" but "no result is unrelated".
        // A search that quietly returns the whole table would still pass a count-based check.
        List<User> unmatched = results.getUsers().stream()
                .filter(user -> !matches(user, query))
                .toList();

        assertTrue(unmatched.isEmpty(),
                "Every result should match '" + query + "' in some field, but these did not: "
                        + unmatched.stream().map(User::fullName).toList());
    }

    @Test(groups = {"regression", "api"},
            description = "A query with no matches returns an empty page, not an error")
    @Story("Users can be searched by free text")
    @Severity(SeverityLevel.NORMAL)
    public void searchWithNoMatchesReturnsAnEmptyPage() {
        Response response = userClient.searchUsers("zzz-no-such-person-zzz");

        assertEquals(response.statusCode(), 200,
                "An empty result set is a successful search, not a 404. Body: " + response.asString());

        UserListResponse results = response.as(UserListResponse.class);
        assertTrue(results.getUsers().isEmpty(), "No user should match a nonsense query");
        assertEquals(results.getTotal(), Integer.valueOf(0), "The total should be 0 for an empty result set");
    }

    @Test(groups = {"regression", "api"},
            description = "Skipping past the end of the collection returns an empty page")
    @Story("Pagination behaves at its edges")
    @Severity(SeverityLevel.NORMAL)
    public void skippingBeyondTheEndReturnsAnEmptyPage() {
        Response firstPage = userClient.getUsers(1, 0);
        int total = firstPage.jsonPath().getInt("total");

        Response beyondEnd = userClient.getUsers(5, total + 100);

        assertEquals(beyondEnd.statusCode(), 200, "Paging past the end is not an error");
        assertTrue(beyondEnd.jsonPath().getList("users").isEmpty(),
                "There should be no users beyond the end of the collection");
        assertEquals(beyondEnd.jsonPath().getInt("total"), total,
                "The reported total should not change just because the caller skipped too far");
    }

    @Test(groups = {"regression", "api"},
            description = "limit=0 is documented to mean 'no limit' rather than 'no rows'")
    @Story("Pagination behaves at its edges")
    @Severity(SeverityLevel.MINOR)
    public void limitOfZeroReturnsEveryUser() {
        Response response = userClient.getUsers(0, 0);

        assertEquals(response.statusCode(), 200, "Requesting limit=0 should succeed");

        int total = response.jsonPath().getInt("total");
        // Worth pinning down precisely because it is counter-intuitive: a client that assumes
        // limit=0 means "give me nothing" would silently pull the entire collection instead.
        assertEquals(response.jsonPath().getList("users").size(), total,
                "limit=0 is treated as 'no limit', so the whole collection should be returned");
    }

    private static boolean matches(User user, String query) {
        String needle = query.toLowerCase(Locale.ROOT);
        return contains(user.getFirstName(), needle)
                || contains(user.getLastName(), needle)
                || contains(user.getMaidenName(), needle)
                || contains(user.getUsername(), needle)
                || contains(user.getEmail(), needle);
    }

    private static boolean contains(String field, String needle) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(needle);
    }
}
