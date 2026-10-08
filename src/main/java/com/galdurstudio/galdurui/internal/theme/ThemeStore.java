package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stockage interne du thème courant (couleurs et espacements).
 *
 * <p>C'est le seul endroit où l'état du thème est conservé. L'API publique
 * ({@code api.theme.GaldurTheme}) ne fait que lire ici, et le chargeur
 * ({@link ThemeLoader}) ne fait qu'écrire ici. Le remplacement est atomique : on
 * échange la référence d'un instantané immuable, jamais son contenu, pour que le fil de
 * rendu ne puisse pas observer un thème à moitié chargé.</p>
 */
public final class ThemeStore {
    /** Instantané immuable du thème : couleurs ARGB et espacements en pixels. */
    private record Snapshot(Map<String, Integer> colors, Map<String, Integer> spacing) {}

    private static final Snapshot DEFAULTS = new Snapshot(ThemeDefaults.COLORS, ThemeDefaults.SPACING);

    /** Thème courant. Remplacé en bloc par {@link #apply(Map, Map)}. */
    private static volatile Snapshot current = DEFAULTS;

    /**
     * Remplace le thème courant.
     *
     * <p>Les clés absentes retombent sur {@link ThemeDefaults}, pour qu'un {@code theme.json}
     * partiel reste utilisable.</p>
     *
     * @param colors  couleurs lues dans {@code theme.json}, en ARGB
     * @param spacing espacements lus dans la section {@code "spacing"}, en pixels
     */
    public static void apply(Map<String, Integer> colors, Map<String, Integer> spacing) {
        current = new Snapshot(merge(ThemeDefaults.COLORS, colors), merge(ThemeDefaults.SPACING, spacing));
    }

    private static Map<String, Integer> merge(Map<String, Integer> defaults, Map<String, Integer> values) {
        Map<String, Integer> merged = new LinkedHashMap<>(defaults);
        merged.putAll(values);
        return Collections.unmodifiableMap(merged);
    }

    /** Revient à la charte par défaut, par exemple quand {@code theme.json} est illisible. */
    public static void resetToDefaults() {
        current = DEFAULTS;
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
        Integer value = current.colors().get(key);
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
        Integer value = current.colors().get(key);
        return value == null ? fallback : value;
    }

    /** {@return {@code true} si le thème courant définit la couleur {@code key}} */
    public static boolean has(String key) {
        return current.colors().containsKey(key);
    }

    /**
     * {@return l'espacement associé à {@code key}, en pixels d'interface}
     *
     * <p>Une clé inconnue renvoie {@code 0} avec un avertissement, sans interrompre le rendu.</p>
     *
     * @param key nom de l'espacement, par exemple {@code "gap"}
     */
    public static int spacing(String key) {
        Integer value = current.spacing().get(key);
        if (value == null) {
            GaldurUI.LOGGER.warn("Espacement de thème inconnu : '{}'. Utilisation de 0.", key);
            return 0;
        }
        return value;
    }

    /** {@return une vue immuable des couleurs du thème courant} */
    public static Map<String, Integer> all() {
        return current.colors();
    }

    /** {@return une vue immuable des espacements du thème courant} */
    public static Map<String, Integer> allSpacing() {
        return current.spacing();
    }

    private ThemeStore() {
    }
}
