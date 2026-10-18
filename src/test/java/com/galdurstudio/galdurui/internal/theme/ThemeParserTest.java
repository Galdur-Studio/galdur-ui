package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ThemeParserTest {
    private final List<String> warnings = new ArrayList<>();

    private ThemeParser.Theme parse(String json) {
        return ThemeParser.parse(new StringReader(json), warnings::add);
    }

    @Test
    void readsRrggbbAsOpaque() {
        assertEquals(0xFFB8341F, parse("{\"accent\": \"#B8341F\"}").colors().get("accent"));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void readsRrggbbWithoutHash() {
        assertEquals(0xFFB8341F, parse("{\"accent\": \"B8341F\"}").colors().get("accent"));
    }

    @Test
    void keepsAlphaOfAarrggbb() {
        assertEquals(0x80B8341F, parse("{\"accent\": \"#80B8341F\"}").colors().get("accent"));
    }

    @Test
    void readsIntegerColor() {
        assertEquals(0xFF00FF00, parse("{\"accent\": " + 0xFF00FF00 + "}").colors().get("accent"));
    }

    @Test
    void forcesAlphaOf24BitIntegerWithWarning() {
        assertEquals(0xFFFF0000, parse("{\"accent\": 16711680}").colors().get("accent"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("#FF0000"), () -> "missing #RRGGBB hint: " + warnings);
    }

    @Test
    void keepsIntegerWithAlphaAsIs() {
        // 0x80B8341F written as an unsigned decimal, as in a JSON file.
        assertEquals(0x80B8341F, parse("{\"accent\": " + Integer.toUnsignedString(0x80B8341F) + "}").colors().get("accent"));
        assertTrue(warnings.isEmpty(), () -> "unexpected warnings: " + warnings);
    }

    @Test
    void ignoresUnderscoreKeys() {
        ThemeParser.Theme theme = parse("""
                {
                  "_comment": "text",
                  "text": "#FFFFFF",
                  "spacing": { "_comment": "text", "gap": 8 }
                }
                """);
        assertEquals(List.of("text"), List.copyOf(theme.colors().keySet()));
        assertEquals(List.of("gap"), List.copyOf(theme.spacing().keySet()));
        assertTrue(warnings.isEmpty(), () -> "unexpected warnings: " + warnings);
    }

    @Test
    void spacingSectionIsNotAColor() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": 8}}");
        assertFalse(theme.colors().containsKey("spacing"));
        assertEquals(8, theme.spacing().get("gap"));
    }

    @Test
    void skipsInvalidColorWithWarning() {
        ThemeParser.Theme theme = parse("{\"text\": \"#FFF\", \"accent\": \"#B8341F\"}");
        assertFalse(theme.colors().containsKey("text"));
        assertEquals(0xFFB8341F, theme.colors().get("accent"));
        assertEquals(1, warnings.size());
    }

    @Test
    void rejectsNegativeSpacing() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": -4, \"panel_padding\": 16}}");
        assertFalse(theme.spacing().containsKey("gap"));
        assertEquals(16, theme.spacing().get("panel_padding"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("gap"));
    }

    @Test
    void roundsDecimalSpacingDownWithWarning() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": 4.7}}");
        assertEquals(4, theme.spacing().get("gap"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Decimal"));
    }

    @Test
    void skipsNonNumericSpacing() {
        assertFalse(parse("{\"spacing\": {\"gap\": \"8\"}}").spacing().containsKey("gap"));
        assertEquals(1, warnings.size());
    }

    @Test
    void warnsWhenSpacingIsNotAnObject() {
        assertTrue(parse("{\"spacing\": 8}").spacing().isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void rejectsNonObjectDocument() {
        assertThrows(IllegalArgumentException.class, () -> parse("[1, 2, 3]"));
    }
}
