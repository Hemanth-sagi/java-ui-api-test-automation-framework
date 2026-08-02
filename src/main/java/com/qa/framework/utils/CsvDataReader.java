package com.qa.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads CSV test data off the classpath.
 *
 * <p>CSV earns its place next to {@link JsonDataReader} for wide, flat matrices — a checkout table
 * of first name / last name / postcode / expectation is far easier for a non-engineer to extend in
 * a spreadsheet than the equivalent nested JSON.
 */
public final class CsvDataReader {

    private static final Logger LOG = LogManager.getLogger(CsvDataReader.class);

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setCommentMarker('#')
            .build();

    private CsvDataReader() {
        // static utility
    }

    /** @return one map per row, keyed by column header */
    public static List<Map<String, String>> readRows(String classpathResource) {
        try (InputStream in = open(classpathResource);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, FORMAT)) {

            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(record.toMap());
            }
            LOG.debug("Loaded {} row(s) from {}", rows.size(), classpathResource);
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CSV test data: " + classpathResource, e);
        }
    }

    /** @return rows shaped for a TestNG {@code @DataProvider}, one map per invocation */
    public static Object[][] readAsDataProvider(String classpathResource) {
        List<Map<String, String>> rows = readRows(classpathResource);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    private static InputStream open(String classpathResource) {
        InputStream in = CsvDataReader.class.getClassLoader().getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException("Test data file not found on classpath: " + classpathResource);
        }
        return in;
    }
}
