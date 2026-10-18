package com.galdurstudio.galdurui.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GaldurFormatTest {
    @Test
    void writesWholeNumbersUpTo999() {
        assertEquals("0", GaldurFormat.amount(0));
        assertEquals("1", GaldurFormat.amount(1));
        assertEquals("64", GaldurFormat.amount(64));
        assertEquals("999", GaldurFormat.amount(999));
    }

    @Test
    void abbreviatesWithOneDecimalBelowTen() {
        assertEquals("1k", GaldurFormat.amount(1_000));
        assertEquals("1,2k", GaldurFormat.amount(1_234));
        assertEquals("9,9k", GaldurFormat.amount(9_999));
        assertEquals("1,2M", GaldurFormat.amount(1_250_000));
    }

    @Test
    void abbreviatesWithoutDecimalFromTen() {
        assertEquals("10k", GaldurFormat.amount(10_000));
        assertEquals("64k", GaldurFormat.amount(64_000));
        assertEquals("999k", GaldurFormat.amount(999_999));
        assertEquals("1M", GaldurFormat.amount(1_000_000));
        assertEquals("123G", GaldurFormat.amount(123_456_789_012L));
    }

    @Test
    void alwaysRoundsDown() {
        assertEquals("1,9k", GaldurFormat.amount(1_999));
        assertEquals("999M", GaldurFormat.amount(999_999_999));
    }

    @Test
    void handlesLongMaxValue() {
        assertEquals("9,2E", GaldurFormat.amount(Long.MAX_VALUE));
        assertEquals("9\u202F223\u202F372\u202F036\u202F854\u202F775\u202F807", GaldurFormat.fullAmount(Long.MAX_VALUE));
    }

    @Test
    void showsZeroForNegativeAmounts() {
        assertEquals("0", GaldurFormat.amount(-5));
        assertEquals("0", GaldurFormat.fullAmount(-5));
    }

    @Test
    void separatesThousandsWithThinSpace() {
        assertEquals("999", GaldurFormat.fullAmount(999));
        assertEquals("1\u202F000", GaldurFormat.fullAmount(1_000));
        assertEquals("1\u202F234\u202F567", GaldurFormat.fullAmount(1_234_567));
        assertEquals("123\u202F456", GaldurFormat.fullAmount(123_456));
    }
}
