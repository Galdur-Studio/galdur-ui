package com.galdurstudio.galdurui.api.text;

/**
 * Formats the amounts shown by Galdur UI. Plain Java: usable anywhere, including on the server and
 * in unit tests.
 */
public final class GaldurFormat {
    private static final String[] UNITS = {"k", "M", "G", "T", "P", "E"};
    /** Narrow no-break space. */
    private static final char THOUSANDS_SEPARATOR = '\u202F';

    private GaldurFormat() {
    }

    /**
     * {@return an amount abbreviated to fit in an item slot}
     *
     * <p>Up to 999 the number is written in full. Above, it gets a suffix ({@code k}, {@code M},
     * {@code G}, {@code T}, {@code P}, {@code E}) and at most three digits: one decimal below 10,
     * none above. It always rounds down, so it never shows more than what exists: 1 999 gives
     * {@code 1,9k}, 999 999 gives {@code 999k}. The decimal separator is a comma.</p>
     *
     * <pre>{@code
     * GaldurFormat.amount(64)        // "64"
     * GaldurFormat.amount(1_234)     // "1,2k"
     * GaldurFormat.amount(64_000)    // "64k"
     * GaldurFormat.amount(1_250_000) // "1,2M"
     * }</pre>
     *
     * @param amount the amount; a negative value shows as {@code 0}
     */
    public static String amount(long amount) {
        long value = Math.max(0, amount);
        if (value < 1000) {
            return Long.toString(value);
        }
        int unit = 0;
        long divisor = 1000;
        while (value / divisor >= 1000 && unit < UNITS.length - 1) {
            divisor *= 1000;
            unit++;
        }
        long whole = value / divisor;
        if (whole >= 10) {
            return whole + UNITS[unit];
        }
        long tenth = (value % divisor) / (divisor / 10);
        return tenth == 0 ? whole + UNITS[unit] : whole + "," + tenth + UNITS[unit];
    }

    /**
     * {@return the exact amount, thousands separated by a narrow no-break space}, for places with
     * room such as tooltips: {@code 1234567} gives {@code 1 234 567}
     *
     * @param amount the amount; a negative value shows as {@code 0}
     */
    public static String fullAmount(long amount) {
        String digits = Long.toString(Math.max(0, amount));
        StringBuilder result = new StringBuilder(digits.length() + digits.length() / 3);
        int firstGroup = digits.length() % 3 == 0 ? 3 : digits.length() % 3;
        result.append(digits, 0, firstGroup);
        for (int index = firstGroup; index < digits.length(); index += 3) {
            result.append(THOUSANDS_SEPARATOR).append(digits, index, index + 3);
        }
        return result.toString();
    }
}
