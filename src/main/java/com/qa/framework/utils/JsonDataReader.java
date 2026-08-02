package com.qa.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads JSON test data off the classpath into typed objects.
 *
 * <p>Deserialising into a record or POJO rather than passing {@code Map<String, Object>} around
 * means a renamed field breaks the build instead of surfacing as a null halfway through a test run.
 *
 * <p>The shared {@link ObjectMapper} is safe to reuse: Jackson mappers are thread-safe once
 * configured, and building one per call is measurably wasteful in a parallel suite.
 */
public final class JsonDataReader {

    private static final Logger LOG = LogManager.getLogger(JsonDataReader.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            // Test data files carry only the fields a test cares about; unknown extras in a
            // response POJO must not blow up deserialisation.
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JsonDataReader() {
        // static utility
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /** Reads a JSON array from {@code src/test/resources} into a list. */
    public static <T> List<T> readList(String classpathResource, Class<T> type) {
        try (InputStream in = open(classpathResource)) {
            List<T> data = MAPPER.readValue(in, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
            LOG.debug("Loaded {} record(s) of {} from {}", data.size(), type.getSimpleName(), classpathResource);
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON test data: " + classpathResource, e);
        }
    }

    /** Reads a single JSON object into the given type. */
    public static <T> T read(String classpathResource, Class<T> type) {
        try (InputStream in = open(classpathResource)) {
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON test data: " + classpathResource, e);
        }
    }

    /** Reads into a generic type, e.g. {@code new TypeReference<Map<String, User>>() {}}. */
    public static <T> T read(String classpathResource, TypeReference<T> type) {
        try (InputStream in = open(classpathResource)) {
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON test data: " + classpathResource, e);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialise " + value, e);
        }
    }

    private static InputStream open(String classpathResource) {
        InputStream in = JsonDataReader.class.getClassLoader().getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException("Test data file not found on classpath: " + classpathResource);
        }
        return in;
    }
}
