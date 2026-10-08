package com.galdurstudio.galdurui.api.theme;

import com.galdurstudio.galdurui.internal.theme.ThemeStore;

/**
 * Accès aux couleurs du thème Galdur UI.
 *
 * <p>Point d'entrée stable pour les mods qui utilisent la bibliothèque. Les couleurs sont
 * définies dans {@code assets/galdurui/theme.json} et rechargées avec les ressources du jeu
 * (F3+T) ; il ne faut donc <strong>pas</strong> mettre une couleur en cache dans un champ
 * {@code static final}, mais la relire à chaque image.</p>
 *
 * <p>Les couleurs sont renvoyées en ARGB empaqueté ({@code 0xAARRGGBB}), le format attendu
 * par les méthodes de rendu de Minecraft.</p>
 *
 * <pre>{@code
 * int accent = GaldurTheme.color(GaldurTheme.ACCENT);
 * }</pre>
 */
public final class GaldurTheme {
    /** Fond principal des écrans (charbon). */
    public static final String BACKGROUND = "background";
    /** Fond des éléments posés sur le fond principal (cartouches, listes). */
    public static final String SURFACE = "surface";
    /** Couleur du texte courant (crème). */
    public static final String TEXT = "text";
    /** Couleur du texte secondaire, moins contrasté. */
    public static final String TEXT_MUTED = "text_muted";
    /** Couleur d'accentuation au repos (redstone). */
    public static final String ACCENT = "accent";
    /** Couleur d'accentuation au survol. */
    public static final String ACCENT_HOVER = "accent_hover";
    /** Couleur d'accentuation pendant l'appui. */
    public static final String ACCENT_PRESSED = "accent_pressed";
    /** Couleur chaude secondaire, pour les mises en avant (braise). */
    public static final String EMBER = "ember";

    /**
     * {@return la couleur ARGB associée à {@code key}}
     *
     * <p>Une clé inconnue renvoie un magenta vif plutôt que de lever une exception : le défaut
     * saute aux yeux à l'écran sans interrompre le rendu.</p>
     *
     * @param key nom de la couleur ; voir les constantes de cette classe
     */
    public static int color(String key) {
        return ThemeStore.color(key);
    }

    /**
     * {@return la couleur ARGB associée à {@code key}, ou {@code fallback} si elle n'est pas définie}
     *
     * <p>À utiliser pour les couleurs propres à un mod, qu'un {@code theme.json} peut
     * fournir mais dont l'absence doit rester silencieuse.</p>
     *
     * @param key      nom de la couleur
     * @param fallback couleur ARGB de repli
     */
    public static int colorOrDefault(String key, int fallback) {
        return ThemeStore.colorOrDefault(key, fallback);
    }

    /**
     * {@return {@code true} si le thème courant définit {@code key}}
     *
     * @param key nom de la couleur
     */
    public static boolean has(String key) {
        return ThemeStore.has(key);
    }

    /** {@return le fond principal, en ARGB} */
    public static int background() {
        return color(BACKGROUND);
    }

    /** {@return le fond des éléments posés sur le fond principal, en ARGB} */
    public static int surface() {
        return color(SURFACE);
    }

    /** {@return la couleur du texte courant, en ARGB} */
    public static int text() {
        return color(TEXT);
    }

    /** {@return la couleur du texte secondaire, en ARGB} */
    public static int textMuted() {
        return color(TEXT_MUTED);
    }

    /** {@return la couleur d'accentuation au repos, en ARGB} */
    public static int accent() {
        return color(ACCENT);
    }

    /** {@return la couleur chaude secondaire, en ARGB} */
    public static int ember() {
        return color(EMBER);
    }

    private GaldurTheme() {
    }
}
