package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Default theme values: the {@code theme.json} embedded in the Galdur UI jar.
 *
 * <p>Used until the resource manager has loaded the theme, and as a per-key fallback when the
 * resource {@code theme.json} (possibly replaced by a pack) is missing, unreadable or incomplete.
 * Read straight from the jar with {@link ThemeParser}, so {@code theme.json} stays the single
 * source of the palette. If even that file is unreadable, a neutral grey fallback is used, with a
 * warning: the library must always be able to draw.</p>
 */
public final class ThemeDefaults {
    private static final String EMBEDDED_THEME = "/assets/galdurui/theme.json";

    /** Neutral fallback, only used if the embedded theme is unreadable. Not the brand palette. */
    private static final ThemeParser.Theme FALLBACK = new ThemeParser.Theme(
            Map.of(
                    GaldurTheme.BACKGROUND, 0xFF202020,
                    GaldurTheme.SURFACE, 0xFF303030,
                    GaldurTheme.TEXT, 0xFFFFFFFF,
                    GaldurTheme.TEXT_MUTED, 0xFFA0A0A0,
                    GaldurTheme.ACCENT, 0xFF606060,
                    GaldurTheme.ACCENT_HOVER, 0xFF707070,
                    GaldurTheme.ACCENT_PRESSED, 0xFF505050,
                    GaldurTheme.EMBER, 0xFF808080),
            Map.ofEntries(
                    Map.entry(GaldurTheme.PANEL_PADDING, 16),
                    Map.entry(GaldurTheme.GAP, 8),
                    Map.entry(GaldurTheme.BUTTON_PADDING_X, 6),
                    Map.entry(GaldurTheme.BUTTON_MIN_WIDTH, 40),
                    Map.entry(GaldurTheme.BUTTON_HEIGHT, 20),
                    Map.entry(GaldurTheme.LIST_ROW_HEIGHT, 12),
                    Map.entry(GaldurTheme.SCROLLBAR_WIDTH, 4),
                    Map.entry(GaldurTheme.FIELD_HEIGHT, 16),
                    Map.entry(GaldurTheme.TAB_HEIGHT, 16),
                    Map.entry(GaldurTheme.TAB_PADDING_X, 8),
                    Map.entry(GaldurTheme.PROGRESS_HEIGHT, 12)));

    private static final ThemeParser.Theme EMBEDDED = loadEmbedded();

    /** Default ARGB colours, in file order. */
    public static final Map<String, Integer> COLORS = EMBEDDED.colors();

    /** Default spacings and sizes in GUI pixels (the {@code "spacing"} section). */
    public static final Map<String, Integer> SPACING = EMBEDDED.spacing();

    private static ThemeParser.Theme loadEmbedded() {
        InputStream stream = ThemeDefaults.class.getResourceAsStream(EMBEDDED_THEME);
        if (stream == null) {
            GaldurUI.LOGGER.warn("{} not found in the jar: using a minimal neutral theme.", EMBEDDED_THEME);
            return FALLBACK;
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return ThemeParser.parse(reader, message -> GaldurUI.LOGGER.warn("Embedded theme: {}", message));
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Could not read {}: using a minimal neutral theme.", EMBEDDED_THEME, exception);
            return FALLBACK;
        }
    }

    private ThemeDefaults() {
    }
}
