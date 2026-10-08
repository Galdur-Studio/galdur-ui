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
 * Valeurs par défaut du thème : le {@code theme.json} embarqué dans le jar de Galdur UI.
 *
 * <p>Ces valeurs sont utilisées tant que le gestionnaire de ressources n'a pas chargé le thème,
 * et servent de repli clé par clé lorsque le {@code theme.json} des ressources (éventuellement
 * remplacé par un pack) est absent, illisible ou incomplet. Elles sont lues directement dans le
 * jar, avec {@link ThemeParser} : {@code theme.json} reste la seule source de la charte.</p>
 *
 * <p>Si même ce fichier est illisible, un repli minimal neutre (gris lisibles, espacements
 * courants) est utilisé, avec un avertissement : la bibliothèque doit toujours pouvoir
 * dessiner.</p>
 */
public final class ThemeDefaults {
    /** Emplacement du thème dans le jar. */
    private static final String EMBEDDED_THEME = "/assets/galdurui/theme.json";

    /** Repli neutre, utilisé seulement si le thème embarqué est illisible. Pas la charte. */
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
            Map.of(
                    GaldurTheme.PANEL_PADDING, 16,
                    GaldurTheme.GAP, 8,
                    GaldurTheme.BUTTON_PADDING_X, 6,
                    GaldurTheme.BUTTON_MIN_WIDTH, 40,
                    GaldurTheme.BUTTON_HEIGHT, 20));

    private static final ThemeParser.Theme EMBEDDED = loadEmbedded();

    /** Couleurs par défaut, en ARGB. Ordre du fichier conservé. */
    public static final Map<String, Integer> COLORS = EMBEDDED.colors();

    /** Espacements et tailles par défaut, en pixels d'interface (section {@code "spacing"}). */
    public static final Map<String, Integer> SPACING = EMBEDDED.spacing();

    private static ThemeParser.Theme loadEmbedded() {
        InputStream stream = ThemeDefaults.class.getResourceAsStream(EMBEDDED_THEME);
        if (stream == null) {
            GaldurUI.LOGGER.warn("{} introuvable dans le jar : repli sur un thème neutre minimal.", EMBEDDED_THEME);
            return FALLBACK;
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return ThemeParser.parse(reader, message -> GaldurUI.LOGGER.warn("Thème embarqué : {}", message));
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Lecture de {} impossible : repli sur un thème neutre minimal.", EMBEDDED_THEME, exception);
            return FALLBACK;
        }
    }

    private ThemeDefaults() {
    }
}
