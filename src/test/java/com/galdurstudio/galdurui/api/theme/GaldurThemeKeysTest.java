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

/** Chaque clé exposée par {@link GaldurTheme} doit exister dans le {@code theme.json} livré. */
class GaldurThemeKeysTest {
    @Test
    void chaque_constante_de_cle_existe_dans_theme_json() throws Exception {
        List<String> warnings = new ArrayList<>();
        ThemeParser.Theme theme;
        try (InputStream stream = GaldurThemeKeysTest.class.getResourceAsStream("/assets/galdurui/theme.json")) {
            assertNotNull(stream, "theme.json absent du classpath");
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                theme = ThemeParser.parse(reader, warnings::add);
            }
        }
        assertTrue(warnings.isEmpty(), () -> "theme.json produit des avertissements : " + warnings);

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
        assertFalse(keys.isEmpty(), "aucune constante de clé trouvée dans GaldurTheme");
        assertTrue(missing.isEmpty(), () -> "clés absentes de theme.json : " + missing);
    }
}
