package com.qa.tests.base;

import com.qa.framework.api.clients.AuthClient;
import com.qa.framework.api.clients.UserClient;
import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;

import io.restassured.module.jsv.JsonSchemaValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;

/**
 * Lifecycle for API tests.
 *
 * <p>There is nothing to start or stop: the clients are stateless and the request specification is
 * built once, so these tests parallelise freely and run in seconds. That asymmetry with the UI base
 * classes is the argument for pushing coverage down to this layer wherever it can live here.
 */
public abstract class BaseApiTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final FrameworkConfig config = ConfigManager.get();

    protected final AuthClient authClient = new AuthClient();
    protected final UserClient userClient = new UserClient();

    /**
     * Matcher asserting a response body satisfies a schema in {@code src/test/resources/schemas}.
     *
     * <p>Schema validation is the cheapest guard against the failure mode status-code assertions
     * miss entirely: a 200 whose body has quietly changed type, dropped a field or started
     * returning null.
     */
    protected static Matcher<?> matchesSchema(String schemaFileName) {
        return JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/" + schemaFileName);
    }
}
