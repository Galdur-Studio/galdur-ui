package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stockage interne des couleurs du thème courant.
 *
 * <p>C'est le seul endroit où l'état du thème est conservé. L'API publique
 * ({@code api.theme.GaldurTheme}) ne fait que lire ici, et le chargeur
 * ({@link ThemeLoader}) ne fait qu'écrire ici. Le remplacement est atomique : on
 * échange la référence d'une carte immuable, jamais son contenu, pour que le fil de
 * rendu ne puisse pas observer un thème à moitié chargé.</p>
 */
public final class ThemeStore {
    /** Thème courant. Immuable ; remplacé en bloc par {@link #apply(Map)}. */
    private static volatile Map<String, Integer> current = ThemeDefaults.COLORS;

    /**
     * Remplace le thème courant.
     *
     * <p>Les clés absentes de {@code colors} retombent sur
     * {@link ThemeDefaults#COLORS}, pour qu'un {@code theme.json} partiel reste utilisable.</p>
     *
     * @param colors couleurs lues dans {@code theme.json}, en ARGB
     */
    public static void apply(Map<String, Integer> colors) {
        Map<String, Integer> merged = new LinkedHashMap<>(ThemeDefaults.COLORS);
        merged.putAll(colors);
        current = Collections.unmodifiableMap(merged);
    }

    /** Revient aux couleurs de la charte, par exemple quand {@code theme.json} est illisible. */
    public static void resetToDefaults() {
        current = ThemeDefaults.COLORS;
    }

    /**
     * {@return la couleur ARGB associée à {@code key}}
     *
     * <p>Une clé inconnue ne lève pas d'exception : on renvoie le magenta
     * {@code 0xFFFF00FF}, visible immédiatement à l'écran, et on avertit dans le journal.
     * Un écran qui s'affiche en couleurs fausses est plus facile à corriger qu'un plantage.</p>
     *
     * @param key nom de la couleur, par exemple {@code "accent"}
     */
    public static int color(String key) {
        Integer value = current.get(key);
        if (value == null) {
            GaldurUI.LOGGER.warn("Couleur de thème inconnue : '{}'. Utilisation du magenta de débogage.", key);
            return 0xFFFF00FF;
        }
        return value;
    }

    /**
     * {@return la couleur ARGB associée à {@code key}, ou {@code fallback} si la clé est absente}
     *
     * @param key      nom de la couleur
     * @param fallback couleur ARGB à utiliser en l'absence de la clé
     */
    public static int colorOrDefault(String key, int fallback) {
        Integer value = current.get(key);
        return value == null ? fallback : value;
    }

    /** {@return {@code true} si le thème courant définit {@code key}} */
    public static boolean has(String key) {
        return current.containsKey(key);
    }

    /** {@return une vue immuable du thème courant} */
    public static Map<String, Integer> all() {
        return current;
    }

    private ThemeStore() {
    }
}
