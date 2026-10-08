package com.galdurstudio.galdurui.internal.theme;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Couleurs de secours de la charte Galdur Studio.
 *
 * <p>Ces valeurs sont utilisées tant que {@code theme.json} n'a pas été chargé, et servent
 * également de repli clé par clé lorsque le fichier est absent, illisible ou incomplet.
 * La bibliothèque doit toujours pouvoir dessiner, même sans thème valide.</p>
 */
public final class ThemeDefaults {
    /** Couleurs par défaut, en ARGB opaque. L'ordre d'insertion est conservé pour les logs. */
    public static final Map<String, Integer> COLORS;

    static {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        defaults.put("background", 0xFF2A2420);     // charbon
        defaults.put("surface", 0xFF352E29);
        defaults.put("text", 0xFFF8F3E8);           // crème
        defaults.put("text_muted", 0xFFC9BEAD);
        defaults.put("accent", 0xFFB8341F);         // redstone
        defaults.put("accent_hover", 0xFFD9573D);
        defaults.put("accent_pressed", 0xFF952A18);
        defaults.put("ember", 0xFFD98A1F);
        COLORS = Collections.unmodifiableMap(defaults);
    }

    private ThemeDefaults() {
    }
}
