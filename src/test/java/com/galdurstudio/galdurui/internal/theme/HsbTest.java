package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HsbTest {
    @Test
    void primaryColours() {
        assertEquals(0xFFFF0000, Hsb.toRgb(0, 1, 1));
        assertEquals(0xFF00FF00, Hsb.toRgb(1 / 3F, 1, 1));
        assertEquals(0xFF0000FF, Hsb.toRgb(2 / 3F, 1, 1));
        assertEquals(0xFFFFFFFF, Hsb.toRgb(0.5F, 0, 1));
        assertEquals(0xFF000000, Hsb.toRgb(0.5F, 1, 0));
    }

    @Test
    void theThemeColoursSurviveTheRoundTrip() {
        for (int rgb : new int[] {0x2A2420, 0x352E29, 0xF8F3E8, 0xC9BEAD, 0xB8341F, 0xD9573D, 0x952A18, 0xD98A1F}) {
            float[] hsb = Hsb.fromRgb(rgb);
            assertEquals(0xFF000000 | rgb, Hsb.toRgb(hsb[0], hsb[1], hsb[2]), Integer.toHexString(rgb));
        }
    }
}
