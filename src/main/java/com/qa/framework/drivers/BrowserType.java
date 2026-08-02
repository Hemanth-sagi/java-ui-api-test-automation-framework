package com.qa.framework.drivers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Browsers the framework knows how to launch.
 *
 * <p>Parsing the config string into an enum here means an unsupported value fails immediately,
 * with a message that lists the valid options, rather than surfacing later as a confusing
 * {@code NullPointerException} inside the factory.
 */
public enum BrowserType {

    CHROME,
    FIREFOX,
    EDGE;

    public static BrowserType from(String value) {
        if (value == null || value.isBlank()) {
            return CHROME;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + value + "'. Supported values: " + supported(), e);
        }
    }

    private static String supported() {
        return Arrays.stream(values())
                .map(browser -> browser.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
}
