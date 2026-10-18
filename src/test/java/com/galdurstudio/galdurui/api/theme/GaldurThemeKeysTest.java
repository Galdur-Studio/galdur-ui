package com.galdurstudio.galdurui.api.theme;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.galdurstudio.galdurui.internal.theme.ThemeParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Every key exposed by {@link GaldurTheme} must exist in the shipped {@code theme.json}. */
class GaldurThemeKeysTest {
    @Test
    void everyKeyConstantExistsInThemeJson() throws Exception {
        List<String> warnings = new ArrayList<>();
        ThemeParser.Theme theme;
        try (InputStream stream = GaldurThemeKeysTest.class.getResourceAsStream("/assets/galdurui/theme.json")) {
            assertNotNull(stream, "theme.json missing from the classpath");
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                theme = ThemeParser.parse(reader, warnings::add);
            }
        }
        assertTrue(warnings.isEmpty(), () -> "theme.json produces warnings: " + warnings);

        List<String> keys = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Field field : GaldurTheme.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || field.getType() != String.class) {
                continue;
            }
            String key = (String) field.get(null);
            keys.add(key);
            if (!theme.colors().containsKey(key) && !theme.spacing().containsKey(key)) {
                missing.add(field.getName() + " = \"" + key + "\"");
            }
        }
        assertFalse(keys.isEmpty(), "no key constant found in GaldurTheme");
        assertTrue(missing.isEmpty(), () -> "keys missing from theme.json: " + missing);
    }
}
