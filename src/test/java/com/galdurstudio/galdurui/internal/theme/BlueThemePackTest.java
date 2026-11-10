package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The test mod's "blue theme" pack must stay what it claims to be: a partial theme with exactly
 * one invalid value, reported and skipped.
 */
class BlueThemePackTest {
    private static final Path PACK_THEME =
            Path.of("src/testmod/resources/resourcepacks/blue_theme/assets/galdurui/theme.json");

    @Test
    void isPartialAndReportsItsInvalidValue() throws IOException {
        List<String> warnings = new ArrayList<>();
        ThemeParser.Theme theme;
        try (Reader reader = Files.newBufferedReader(PACK_THEME, StandardCharsets.UTF_8)) {
            theme = ThemeParser.parse(reader, warnings::add);
        }

        assertEquals(0xFF2F6FB8, theme.colors().get("accent"));
        assertFalse(theme.colors().containsKey("text"), "text must stay missing to test the fallback");
        assertFalse(theme.colors().containsKey("text_muted"), "the invalid value must be skipped");
        assertEquals(1, warnings.size(), () -> "warnings: " + warnings);
        assertTrue(warnings.get(0).contains("text_muted"));
        assertEquals(24, theme.spacing().get("button_height"));
    }
}
