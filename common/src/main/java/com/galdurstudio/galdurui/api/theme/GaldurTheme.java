package com.galdurstudio.galdurui.api.theme;

import com.galdurstudio.galdurui.internal.theme.ThemeStore;

/**
 * Colours and spacings of the Galdur UI theme.
 *
 * <p>Values come from {@code assets/galdurui/theme.json} and are reloaded with the game resources
 * (F3+T, or a resource pack change). Read them every frame or every {@code init()}; never cache
 * them in a {@code static final} field. Colours are packed ARGB ({@code 0xAARRGGBB}), the format
 * Minecraft's rendering methods expect.</p>
 *
 * <pre>{@code
 * int accent = GaldurTheme.accent();
 * int custom = GaldurTheme.colorOrDefault("mymod_energy", 0xFF3FC1D9);
 * }</pre>
 */
public final class GaldurTheme {
    /** Main screen background (charcoal). */
    public static final String BACKGROUND = "background";
    /** Background of elements placed on the main background (lists, fields). */
    public static final String SURFACE = "surface";
    /** Main text colour (cream). */
    public static final String TEXT = "text";
    /** Secondary, less contrasted text. */
    public static final String TEXT_MUTED = "text_muted";
    /** Accent colour at rest (redstone). */
    public static final String ACCENT = "accent";
    /** Accent colour under the mouse or keyboard focus. */
    public static final String ACCENT_HOVER = "accent_hover";
    /** Accent colour while pressed. */
    public static final String ACCENT_PRESSED = "accent_pressed";
    /** Warm secondary colour, for highlights (ember). */
    public static final String EMBER = "ember";

    // Spacings ("spacing" section of theme.json), in GUI pixels.

    /** Inner margin of a panel, between its edge and its contents. */
    public static final String PANEL_PADDING = "panel_padding";
    /** Gap between two neighbouring layout elements. */
    public static final String GAP = "gap";
    /** Horizontal margin between a button edge and its label. */
    public static final String BUTTON_PADDING_X = "button_padding_x";
    /** Minimum width of a button sized from its label. */
    public static final String BUTTON_MIN_WIDTH = "button_min_width";
    /** Height of a button. */
    public static final String BUTTON_HEIGHT = "button_height";
    /** Height of a scrolling list row, text included. */
    public static final String LIST_ROW_HEIGHT = "list_row_height";
    /** Width of a scrollbar (lists and item grids). */
    public static final String SCROLLBAR_WIDTH = "scrollbar_width";
    /** Height of a text field (search field). */
    public static final String FIELD_HEIGHT = "field_height";
    /** Height of a tab row, bottom line included. */
    public static final String TAB_HEIGHT = "tab_height";
    /** Horizontal margin between a tab edge and its title. */
    public static final String TAB_PADDING_X = "tab_padding_x";
    /** Height of a progress bar, frame included. */
    public static final String PROGRESS_HEIGHT = "progress_height";

    /**
     * {@return the ARGB colour of {@code key}}
     *
     * <p>An unknown key returns debug magenta ({@code 0xFFFF00FF}) instead of throwing: the
     * mistake shows on screen without breaking rendering. A warning is logged once per key until
     * the next resource reload.</p>
     *
     * @param key colour name; see the constants of this class
     */
    public static int color(String key) {
        return ThemeStore.color(key);
    }

    /**
     * {@return the ARGB colour of {@code key}, or {@code fallback} if the theme does not define
     * it}; for mod-specific colours that a {@code theme.json} may provide, silently
     */
    public static int colorOrDefault(String key, int fallback) {
        return ThemeStore.colorOrDefault(key, fallback);
    }

    /** {@return {@code true} if the current theme defines the colour {@code key}} */
    public static boolean has(String key) {
        return ThemeStore.has(key);
    }

    /** {@return the {@link #BACKGROUND} colour, ARGB} */
    public static int background() {
        return color(BACKGROUND);
    }

    /** {@return the {@link #SURFACE} colour, ARGB} */
    public static int surface() {
        return color(SURFACE);
    }

    /** {@return the {@link #TEXT} colour, ARGB} */
    public static int text() {
        return color(TEXT);
    }

    /** {@return the {@link #TEXT_MUTED} colour, ARGB} */
    public static int textMuted() {
        return color(TEXT_MUTED);
    }

    /** {@return the {@link #ACCENT} colour, ARGB} */
    public static int accent() {
        return color(ACCENT);
    }

    /** {@return the {@link #ACCENT_HOVER} colour, ARGB} */
    public static int accentHover() {
        return color(ACCENT_HOVER);
    }

    /** {@return the {@link #ACCENT_PRESSED} colour, ARGB} */
    public static int accentPressed() {
        return color(ACCENT_PRESSED);
    }

    /** {@return the {@link #EMBER} colour, ARGB} */
    public static int ember() {
        return color(EMBER);
    }

    /**
     * {@return the spacing of {@code key}, in GUI pixels}; an unknown key returns {@code 0} with a
     * warning
     *
     * @param key spacing name; see the constants of this class
     */
    public static int spacing(String key) {
        return ThemeStore.spacing(key);
    }

    /** {@return the {@link #PANEL_PADDING} spacing, in pixels} */
    public static int panelPadding() {
        return spacing(PANEL_PADDING);
    }

    /** {@return the {@link #GAP} spacing, in pixels} */
    public static int gap() {
        return spacing(GAP);
    }

    /** {@return the {@link #BUTTON_PADDING_X} spacing, in pixels} */
    public static int buttonPaddingX() {
        return spacing(BUTTON_PADDING_X);
    }

    /** {@return the {@link #BUTTON_MIN_WIDTH} spacing, in pixels} */
    public static int buttonMinWidth() {
        return spacing(BUTTON_MIN_WIDTH);
    }

    /** {@return the {@link #BUTTON_HEIGHT} spacing, in pixels} */
    public static int buttonHeight() {
        return spacing(BUTTON_HEIGHT);
    }

    /** {@return the {@link #LIST_ROW_HEIGHT} spacing, in pixels} */
    public static int listRowHeight() {
        return spacing(LIST_ROW_HEIGHT);
    }

    /** {@return the {@link #SCROLLBAR_WIDTH} spacing, in pixels} */
    public static int scrollbarWidth() {
        return spacing(SCROLLBAR_WIDTH);
    }

    /** {@return the {@link #FIELD_HEIGHT} spacing, in pixels} */
    public static int fieldHeight() {
        return spacing(FIELD_HEIGHT);
    }

    /** {@return the {@link #TAB_HEIGHT} spacing, in pixels} */
    public static int tabHeight() {
        return spacing(TAB_HEIGHT);
    }

    /** {@return the {@link #TAB_PADDING_X} spacing, in pixels} */
    public static int tabPaddingX() {
        return spacing(TAB_PADDING_X);
    }

    /** {@return the {@link #PROGRESS_HEIGHT} spacing, in pixels} */
    public static int progressHeight() {
        return spacing(PROGRESS_HEIGHT);
    }

    private GaldurTheme() {
    }
}
