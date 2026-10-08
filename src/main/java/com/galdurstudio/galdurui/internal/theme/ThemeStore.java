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
    /** Couleur de débogage (magenta) renvoyée pour une couleur inconnue. */
    public static final int MISSING_COLOR = 0xFFFF00FF;

    /** Instantané immuable du thème : couleurs ARGB et espacements en pixels. */
    private record Snapshot(Map<String, Integer> colors, Map<String, Integer> spacing) {}

    private static final Snapshot DEFAULTS = new Snapshot(ThemeDefaults.COLORS, ThemeDefaults.SPACING);

    /** Thème courant. Remplacé en bloc par {@link #apply(Map, Map)}. */
    private static volatile Snapshot current = DEFAULTS;

    /** Clés inconnues déjà signalées : un avertissement par clé, pas un par image. */
    private static final WarnOnce UNKNOWN_COLORS = new WarnOnce(GaldurUI.LOGGER::warn);
    private static final WarnOnce UNKNOWN_SPACINGS = new WarnOnce(GaldurUI.LOGGER::warn);

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
        forgetWarnings();
    }

    private static Map<String, Integer> merge(Map<String, Integer> defaults, Map<String, Integer> values) {
        Map<String, Integer> merged = new LinkedHashMap<>(defaults);
        merged.putAll(values);
        return Collections.unmodifiableMap(merged);
    }

    /** Revient à la charte par défaut, par exemple quand {@code theme.json} est illisible. */
    public static void resetToDefaults() {
        current = DEFAULTS;
        forgetWarnings();
    }

    /** Après un rechargement, une clé toujours inconnue doit pouvoir être signalée de nouveau. */
    private static void forgetWarnings() {
        UNKNOWN_COLORS.reset();
        UNKNOWN_SPACINGS.reset();
    }

    /**
     * {@return la couleur ARGB associée à {@code key}}
     *
     * <p>Une clé inconnue ne lève pas d'exception : on renvoie {@link #MISSING_COLOR}, visible
     * immédiatement à l'écran, et on avertit dans le journal, une fois par clé jusqu'au
     * prochain rechargement. Un écran qui s'affiche en couleurs fausses est plus facile à
     * corriger qu'un plantage.</p>
     *
     * @param key nom de la couleur, par exemple {@code "accent"}
     */
    public static int color(String key) {
        Integer value = current.colors().get(key);
        if (value == null) {
            UNKNOWN_COLORS.warn(key, "Couleur de thème inconnue : '" + key + "'. Utilisation du magenta de débogage.");
            return MISSING_COLOR;
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
     * <p>Une clé inconnue renvoie {@code 0} avec un avertissement (une fois par clé jusqu'au
     * prochain rechargement), sans interrompre le rendu.</p>
     *
     * @param key nom de l'espacement, par exemple {@code "gap"}
     */
    public static int spacing(String key) {
        Integer value = current.spacing().get(key);
        if (value == null) {
            UNKNOWN_SPACINGS.warn(key, "Espacement de thème inconnu : '" + key + "'. Utilisation de 0.");
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
