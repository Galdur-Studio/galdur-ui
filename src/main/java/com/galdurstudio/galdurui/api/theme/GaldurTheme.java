package com.galdurstudio.galdurui.api.theme;

import com.galdurstudio.galdurui.internal.theme.ThemeStore;

/**
 * Accès aux couleurs et aux espacements du thème Galdur UI.
 *
 * <p>Point d'entrée stable pour les mods qui utilisent la bibliothèque. Les couleurs sont
 * définies dans {@code assets/galdurui/theme.json} et rechargées avec les ressources du jeu ;
 * il ne faut donc <strong>pas</strong> mettre une couleur en cache dans un champ
 * {@code static final}, mais la relire à chaque image.</p>
 *
 * <p>Recharger les ressources : F3+T, mais Minecraft l'ignore tant qu'un écran est ouvert. Dans
 * ce cas, utiliser le bouton « Recharger le thème » de {@code /galdurui test} (environnement de
 * développement), qui appelle {@code Minecraft.reloadResourcePacks()}.</p>
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

    // Espacements (section "spacing" de theme.json), en pixels d'interface.

    /** Marge intérieure d'un panneau, entre son bord et son contenu. */
    public static final String PANEL_PADDING = "panel_padding";
    /** Écart entre deux éléments voisins d'un layout. */
    public static final String GAP = "gap";
    /** Marge horizontale entre le bord d'un bouton et son libellé. */
    public static final String BUTTON_PADDING_X = "button_padding_x";
    /** Largeur minimale d'un bouton dimensionné d'après son texte. */
    public static final String BUTTON_MIN_WIDTH = "button_min_width";
    /** Hauteur par défaut d'un bouton. */
    public static final String BUTTON_HEIGHT = "button_height";
    /** Hauteur d'une ligne de liste défilante, texte compris. */
    public static final String LIST_ROW_HEIGHT = "list_row_height";
    /** Largeur de la barre de défilement d'une liste. */
    public static final String SCROLLBAR_WIDTH = "scrollbar_width";

    /**
     * {@return la couleur ARGB associée à {@code key}}
     *
     * <p>Une clé inconnue renvoie le magenta de débogage {@code 0xFFFF00FF} plutôt que de lever
     * une exception : le défaut saute aux yeux à l'écran sans interrompre le rendu. Un
     * avertissement est écrit une fois par clé, jusqu'au prochain rechargement des ressources.</p>
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

    /** {@return la couleur d'accentuation au survol, en ARGB} */
    public static int accentHover() {
        return color(ACCENT_HOVER);
    }

    /** {@return la couleur d'accentuation pendant l'appui, en ARGB} */
    public static int accentPressed() {
        return color(ACCENT_PRESSED);
    }

    /** {@return la couleur chaude secondaire, en ARGB} */
    public static int ember() {
        return color(EMBER);
    }

    /**
     * {@return l'espacement associé à {@code key}, en pixels d'interface}
     *
     * <p>Lu dans la section {@code "spacing"} de {@code theme.json}. Comme les couleurs, il est
     * rechargé avec les ressources : le relire à chaque {@code init()} plutôt que de le
     * garder dans un {@code static final}. Une clé inconnue renvoie {@code 0}.</p>
     *
     * @param key nom de l'espacement ; voir les constantes de cette classe
     */
    public static int spacing(String key) {
        return ThemeStore.spacing(key);
    }

    /** {@return la marge intérieure d'un panneau, en pixels} */
    public static int panelPadding() {
        return spacing(PANEL_PADDING);
    }

    /** {@return l'écart entre deux éléments voisins d'un layout, en pixels} */
    public static int gap() {
        return spacing(GAP);
    }

    /** {@return la marge horizontale entre le bord d'un bouton et son libellé, en pixels} */
    public static int buttonPaddingX() {
        return spacing(BUTTON_PADDING_X);
    }

    /** {@return la largeur minimale d'un bouton, en pixels} */
    public static int buttonMinWidth() {
        return spacing(BUTTON_MIN_WIDTH);
    }

    /** {@return la hauteur par défaut d'un bouton, en pixels} */
    public static int buttonHeight() {
        return spacing(BUTTON_HEIGHT);
    }

    /** {@return la hauteur d'une ligne de liste défilante, en pixels} */
    public static int listRowHeight() {
        return spacing(LIST_ROW_HEIGHT);
    }

    /** {@return la largeur de la barre de défilement d'une liste, en pixels} */
    public static int scrollbarWidth() {
        return spacing(SCROLLBAR_WIDTH);
    }

    private GaldurTheme() {
    }
}
