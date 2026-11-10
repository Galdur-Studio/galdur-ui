package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerThemeTest {
    private static final PlayerTheme.Theme OCEAN = new PlayerTheme.Theme(
            Map.of("accent", GaldurPaint.solid(0xFF2F6FB5),
                    "panel", GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF1E2A38, 0xFF2A2420),
                    "button", GaldurPaint.gradient(GaldurPaint.Direction.HORIZONTAL, 0x80102030, 0xFF2F6FB5, 0xFF24578F)),
            Map.of("gap", 6), "Océan", "Jules");

    @Test
    void anExportedThemeReadsBackTheSame() {
        List<String> warnings = new ArrayList<>();
        ThemeParser.Theme parsed = ThemeParser.parse(new StringReader(PlayerTheme.toJson(OCEAN)), warnings::add);
        assertEquals(OCEAN.paints(), parsed.paints());
        assertEquals(OCEAN.spacing(), parsed.spacing());
        assertEquals("Océan", parsed.name());
        assertEquals("Jules", parsed.author());
        assertTrue(warnings.isEmpty(), warnings.toString());
    }

    @Test
    void aThemePastedFromAMessageIsFoundInTheText() {
        String message = "Here is my theme :)\n" + PlayerTheme.toJson(OCEAN) + "\nhave fun";
        assertEquals(OCEAN, PlayerTheme.parse(message));
        assertNull(PlayerTheme.parse("no theme here"));
        assertNull(PlayerTheme.parse("{\"_comment\": \"empty\"}"));
    }

    @Test
    void hexIsShortWhenOpaque() {
        assertEquals("#B8341F", PlayerTheme.hex(0xFFB8341F));
        assertEquals("#80B8341F", PlayerTheme.hex(0x80B8341F));
    }

    @Test
    void theEditorReadsCompleteHexCodesOnly() {
        assertEquals(0xFFB8341F, PlayerTheme.parseHex("#b8341f"));
        assertEquals(0x80B8341F, PlayerTheme.parseHex("80B8341F"));
        assertNull(PlayerTheme.parseHex("#B834"));
        assertNull(PlayerTheme.parseHex("#GGGGGG"));
    }

    @Test
    void fileNamesAreSafe() {
        assertEquals("ocean-bleu", PlayerTheme.fileName("Océan bleu !"));
        assertEquals("theme", PlayerTheme.fileName("  "));
    }

    @Test
    void gradientsMixTheirColours() {
        GaldurPaint paint = GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF000000, 0xFFFFFFFF);
        assertEquals(0xFF000000, paint.at(0));
        assertEquals(0xFFFFFFFF, paint.at(1));
        assertEquals(0xFF808080, paint.at(0.5F));
    }
}
