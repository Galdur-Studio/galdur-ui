package com.galdurstudio.galdurui.internal.theme;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Valeurs de secours de la charte Galdur Studio.
 *
 * <p>Ces valeurs sont utilisées tant que {@code theme.json} n'a pas été chargé, et servent
 * également de repli clé par clé lorsque le fichier est absent, illisible ou incomplet.
 * La bibliothèque doit toujours pouvoir dessiner, même sans thème valide.</p>
 */
public final class ThemeDefaults {
    /** Couleurs par défaut, en ARGB opaque. L'ordre d'insertion est conservé pour les logs. */
    public static final Map<String, Integer> COLORS;

    /** Espacements et tailles par défaut, en pixels d'interface (section {@code "spacing"}). */
    public static final Map<String, Integer> SPACING;

    static {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        defaults.put("background", 0xFF2A2420);
        defaults.put("surface", 0xFF352E29);
        defaults.put("text", 0xFFF8F3E8);
        defaults.put("text_muted", 0xFFC9BEAD);
        defaults.put("accent", 0xFFB8341F);
        defaults.put("accent_hover", 0xFFD9573D);
        defaults.put("accent_pressed", 0xFF952A18);
        defaults.put("ember", 0xFFD98A1F);
        COLORS = Collections.unmodifiableMap(defaults);

        Map<String, Integer> spacing = new LinkedHashMap<>();
        spacing.put("panel_padding", 16);
        spacing.put("gap", 8);
        spacing.put("button_padding_x", 6);
        spacing.put("button_min_width", 40);
        spacing.put("button_height", 20);
        SPACING = Collections.unmodifiableMap(spacing);
    }

    private ThemeDefaults() {
    }
}
