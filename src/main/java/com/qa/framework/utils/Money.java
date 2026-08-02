package com.qa.framework.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses and compares the money strings the UI renders.
 *
 * <p>{@link BigDecimal} rather than {@code double}, because these values feed real arithmetic
 * assertions — "total equals subtotal plus tax" is exactly the check binary floating point gets
 * wrong, and a test that fails on 43.18 vs 43.179999999999996 teaches everyone to distrust the suite.
 */
public final class Money {

    private Money() {
        // static utility
    }

    /**
     * Extracts an amount from a label such as {@code "Item total: $39.98"} or {@code "$29.99"}.
     *
     * @throws IllegalArgumentException when the text holds no parseable amount, which is a more
     *         useful failure than a {@link NumberFormatException} with no context
     */
    public static BigDecimal parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Cannot parse a money value from null");
        }
        String digits = text.replaceAll("[^0-9.]", "");
        if (digits.isEmpty() || digits.equals(".")) {
            throw new IllegalArgumentException("No money value found in '" + text + "'");
        }
        return new BigDecimal(digits).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    /** Rounds to 2dp half-up — the convention retail checkouts use. */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentageOf(BigDecimal value, String percentage) {
        return round(value.multiply(new BigDecimal(percentage)).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
    }
}
