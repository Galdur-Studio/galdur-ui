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
    void lit_une_couleur_RRGGBB_avec_alpha_opaque() {
        assertEquals(0xFFB8341F, parse("{\"accent\": \"#B8341F\"}").colors().get("accent"));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void lit_une_couleur_RRGGBB_sans_diese() {
        assertEquals(0xFFB8341F, parse("{\"accent\": \"B8341F\"}").colors().get("accent"));
    }

    @Test
    void lit_une_couleur_AARRGGBB_avec_son_alpha() {
        assertEquals(0x80B8341F, parse("{\"accent\": \"#80B8341F\"}").colors().get("accent"));
    }

    @Test
    void lit_une_couleur_donnee_en_entier() {
        assertEquals(0xFF00FF00, parse("{\"accent\": " + 0xFF00FF00 + "}").colors().get("accent"));
    }

    @Test
    void ignore_les_cles_commencant_par_un_tiret_bas() {
        ThemeParser.Theme theme = parse("""
                {
                  "_comment": "texte",
                  "text": "#FFFFFF",
                  "spacing": { "_comment": "texte", "gap": 8 }
                }
                """);
        assertEquals(List.of("text"), List.copyOf(theme.colors().keySet()));
        assertEquals(List.of("gap"), List.copyOf(theme.spacing().keySet()));
        assertTrue(warnings.isEmpty(), () -> "avertissements inattendus : " + warnings);
    }

    @Test
    void la_section_spacing_n_est_pas_une_couleur() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": 8}}");
        assertFalse(theme.colors().containsKey("spacing"));
        assertEquals(8, theme.spacing().get("gap"));
    }

    @Test
    void ignore_une_couleur_invalide_avec_un_avertissement() {
        ThemeParser.Theme theme = parse("{\"text\": \"#FFF\", \"accent\": \"#B8341F\"}");
        assertFalse(theme.colors().containsKey("text"));
        assertEquals(0xFFB8341F, theme.colors().get("accent"));
        assertEquals(1, warnings.size());
    }

    @Test
    void rejette_un_espacement_negatif() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": -4, \"panel_padding\": 16}}");
        assertFalse(theme.spacing().containsKey("gap"));
        assertEquals(16, theme.spacing().get("panel_padding"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("gap"));
    }

    @Test
    void avertit_sur_un_espacement_decimal_et_l_arrondit_a_l_entier_inferieur() {
        ThemeParser.Theme theme = parse("{\"spacing\": {\"gap\": 4.7}}");
        assertEquals(4, theme.spacing().get("gap"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("décimal"));
    }

    @Test
    void ignore_un_espacement_qui_n_est_pas_un_nombre() {
        assertFalse(parse("{\"spacing\": {\"gap\": \"8\"}}").spacing().containsKey("gap"));
        assertEquals(1, warnings.size());
    }

    @Test
    void refuse_un_document_qui_n_est_pas_un_objet() {
        assertThrows(IllegalArgumentException.class, () -> parse("[1, 2, 3]"));
    }
}
