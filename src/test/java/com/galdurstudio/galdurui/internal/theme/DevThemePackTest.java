package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Le pack de développement {@code theme_bleu} doit rester ce qu'il prétend être : un thème
 * partiel, avec exactement une valeur invalide, signalée et ignorée.
 */
class DevThemePackTest {
    private static final String PATH = "/resourcepacks/theme_bleu/assets/galdurui/theme.json";

    @Test
    void le_pack_bleu_est_partiel_et_signale_sa_valeur_invalide() throws IOException {
        List<String> warnings = new ArrayList<>();
        ThemeParser.Theme theme;
        try (InputStream in = DevThemePackTest.class.getResourceAsStream(PATH)) {
            assertNotNull(in, "pack de test introuvable : " + PATH);
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                theme = ThemeParser.parse(reader, warnings::add);
            }
        }

        assertEquals(0xFF2F6FB8, theme.colors().get("accent"));
        assertFalse(theme.colors().containsKey("text"), "text doit rester absent pour tester le repli");
        assertFalse(theme.colors().containsKey("text_muted"), "la valeur invalide doit être ignorée");
        assertEquals(1, warnings.size(), () -> "avertissements : " + warnings);
        assertTrue(warnings.get(0).contains("text_muted"));
        assertEquals(24, theme.spacing().get("button_height"));
    }
}
