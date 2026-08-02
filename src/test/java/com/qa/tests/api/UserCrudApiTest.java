package com.qa.tests.api;

import com.qa.framework.api.models.User;
import com.qa.tests.base.BaseApiTest;
import com.qa.tests.dataproviders.TestDataProviders;
import com.qa.tests.model.NewUserScenario;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** CRUD contract for the users resource. */
@Epic("DummyJSON API")
@Feature("Users CRUD")
public class UserCrudApiTest extends BaseApiTest {

    private static final int KNOWN_USER_ID = 1;

    @Test(groups = {"smoke", "api"},
            description = "A known user is returned with the documented shape")
    @Story("A user can be read by id")
    @Severity(SeverityLevel.BLOCKER)
    public void knownUserIsReturnedWithTheExpectedShape() {
        Response response = userClient.getUser(KNOWN_USER_ID);

        assertEquals(response.statusCode(), 200,
                "Reading a known user should succeed. Body: " + response.asString());
        response.then()
                .time(org.hamcrest.Matchers.lessThan(5000L))
                .body(matchesSchema("user-schema.json"));

        User user = response.as(User.class);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(user.getId(), Integer.valueOf(KNOWN_USER_ID),
                "The response should describe the user that was requested");
        softly.assertTrue(user.getFirstName() != null && !user.getFirstName().isBlank(),
                "A user record must carry a first name");
        softly.assertNotNull(user.getAddress(),
                "The nested address object should be populated");
        softly.assertTrue(user.getAddress().getCity() != null && !user.getAddress().getCity().isBlank(),
                "The address should include a city, but was: " + user.getAddress());
        softly.assertAll();
    }

    @Test(groups = {"regression", "api"},
            description = "Pagination honours limit and skip, and reports a stable total")
    @Story("Users can be listed with pagination")
    @Severity(SeverityLevel.CRITICAL)
    public void paginationHonoursLimitAndSkip() {
        int limit = 5;
        int skip = 10;

        Response response = userClient.getUsers(limit, skip);

        assertEquals(response.statusCode(), 200, "Listing users should succeed");
        response.then().body(matchesSchema("user-list-schema.json"));

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(response.jsonPath().getList("users").size(), limit,
                "The page should hold exactly the requested number of users");
        softly.assertEquals(response.jsonPath().getInt("limit"), limit, "The echoed limit should match the request");
        softly.assertEquals(response.jsonPath().getInt("skip"), skip, "The echoed skip should match the request");
        softly.assertTrue(response.jsonPath().getInt("total") > limit + skip,
                "The reported total should describe the whole collection, not just this page");
        softly.assertAll();
    }

    @Test(groups = {"regression", "api"},
            description = "Consecutive pages do not overlap")
    @Story("Users can be listed with pagination")
    @Severity(SeverityLevel.NORMAL)
    public void consecutivePagesReturnDifferentUsers() {
        var firstPageIds = userClient.getUsers(3, 0).jsonPath().getList("users.id");
        var secondPageIds = userClient.getUsers(3, 3).jsonPath().getList("users.id");

        assertEquals(firstPageIds.size(), 3, "The first page should hold 3 users");
        assertTrue(java.util.Collections.disjoint(firstPageIds, secondPageIds),
                "Paging must not repeat records: page 1 " + firstPageIds + " overlaps page 2 " + secondPageIds);
    }

    @Test(dataProvider = "newUsers", dataProviderClass = TestDataProviders.class,
            groups = {"regression", "api"},
            description = "A created user is echoed back intact with a new id")
    @Story("A user can be created")
    @Severity(SeverityLevel.CRITICAL)
    public void createdUserIsEchoedBackWithAnId(NewUserScenario scenario) {
        User payload = scenario.toUser();

        Response response = userClient.createUser(payload);

        assertEquals(response.statusCode(), 201,
                "Creating a user should return 201 Created. Body: " + response.asString());

        User created = response.as(User.class);

        SoftAssert softly = new SoftAssert();
        softly.assertNotNull(created.getId(), "A created user must be assigned an id");
        softly.assertEquals(created.getFirstName(), payload.getFirstName(),
                "The first name should survive the round trip for '" + scenario.getScenario() + "'");
        softly.assertEquals(created.getLastName(), payload.getLastName(),
                "The last name should survive the round trip for '" + scenario.getScenario() + "'");
        softly.assertEquals(created.getEmail(), payload.getEmail(),
                "The email should survive the round trip");
        softly.assertEquals(created.getAge(), payload.getAge(),
                "The age should survive the round trip, including the boundary value 0");
        softly.assertAll();
    }

    @Test(groups = {"regression", "api"},
            description = "An update changes only the fields that were sent")
    @Story("A user can be updated")
    @Severity(SeverityLevel.CRITICAL)
    public void updateChangesOnlyTheSuppliedFields() {
        User before = userClient.getUser(2).as(User.class);
        String newLastName = "Updated-" + System.currentTimeMillis();

        Response response = userClient.updateUser(2, User.builder().lastName(newLastName).build());

        assertEquals(response.statusCode(), 200,
                "Updating an existing user should succeed. Body: " + response.asString());

        User after = response.as(User.class);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(after.getLastName(), newLastName, "The submitted field should be updated");
        softly.assertEquals(after.getId(), before.getId(), "An update must not change the record's id");
        softly.assertEquals(after.getFirstName(), before.getFirstName(),
                "A partial update must leave untouched fields alone");
        softly.assertAll();
    }

    @Test(groups = {"regression", "api"},
            description = "A delete is acknowledged and flagged on the returned record")
    @Story("A user can be deleted")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteMarksTheRecordAsDeleted() {
        Response response = userClient.deleteUser(3);

        assertEquals(response.statusCode(), 200,
                "Deleting an existing user should succeed. Body: " + response.asString());

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(response.jsonPath().getInt("id"), 3,
                "The response should identify the record that was deleted");
        softly.assertTrue(response.jsonPath().getBoolean("isDeleted"),
                "The deleted record should be flagged isDeleted");
        softly.assertNotNull(response.jsonPath().getString("deletedOn"),
                "A deletion should be timestamped");
        softly.assertAll();
    }

    @Test(groups = {"regression", "api"},
            description = "An unknown id returns 404 with a useful message")
    @Story("Unknown records are reported as missing")
    @Severity(SeverityLevel.CRITICAL)
    public void unknownUserReturnsNotFound() {
        int unknownId = 999_999;

        Response response = userClient.getUser(unknownId);

        assertEquals(response.statusCode(), 404,
                "An unknown id must return 404, not an empty 200. Body: " + response.asString());
        response.then().body(matchesSchema("error-schema.json"));

        String message = response.jsonPath().getString("message");
        assertNotNull(message, "A 404 should explain itself");
        assertTrue(message.contains(String.valueOf(unknownId)),
                "The message should name the id that was not found, but was: " + message);
    }
}
