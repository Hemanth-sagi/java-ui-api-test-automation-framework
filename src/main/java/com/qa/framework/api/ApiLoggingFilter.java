package com.qa.framework.api;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Routes REST Assured traffic into Log4j2 instead of {@code System.out}.
 *
 * <p>REST Assured's built-in {@code log().all()} prints straight to standard out, which in a
 * parallel suite interleaves into noise that no appender captures. Logging through the framework's
 * own logger keeps request/response detail in the same timestamped, thread-tagged file as everything
 * else.
 *
 * <p>One line per call at INFO; full bodies only at DEBUG, or at WARN when the call failed — so a
 * green run stays readable and a red one has everything needed to triage it.
 */
public class ApiLoggingFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(ApiLoggingFilter.class);
    private static final int BODY_PREVIEW_LIMIT = 2000;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {

        long startedAt = System.currentTimeMillis();
        Response response = context.next(requestSpec, responseSpec);
        long elapsed = System.currentTimeMillis() - startedAt;

        int status = response.getStatusCode();
        LOG.info("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(), status, elapsed);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request body : {}", preview(requestSpec.getBody()));
            LOG.debug("Response body: {}", preview(response.asString()));
        } else if (status >= 400) {
            LOG.warn("Failed call {} {} -> {} | response: {}",
                    requestSpec.getMethod(), requestSpec.getURI(), status, preview(response.asString()));
        }
        return response;
    }

    private static String preview(Object body) {
        if (body == null) {
            return "<empty>";
        }
        String text = String.valueOf(body);
        return text.length() <= BODY_PREVIEW_LIMIT ? text : text.substring(0, BODY_PREVIEW_LIMIT) + "... (truncated)";
    }
}
