package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the current theme (colours and spacings). The public {@code api.theme.GaldurTheme} only
 * reads from here and {@link ThemeLoader} only writes here. Replacement swaps a reference to an
 * immutable snapshot, so the render thread never sees a half-loaded theme.
 */
public final class ThemeStore {
    /** Debug magenta returned for an unknown colour. */
    public static final int MISSING_COLOR = 0xFFFF00FF;

    private record Snapshot(Map<String, Integer> colors, Map<String, Integer> spacing) {}

    private static final Snapshot DEFAULTS = new Snapshot(ThemeDefaults.COLORS, ThemeDefaults.SPACING);

    private static volatile Snapshot current = DEFAULTS;

    private static final WarnOnce UNKNOWN_COLORS = new WarnOnce(GaldurUI.LOGGER::warn);
    private static final WarnOnce UNKNOWN_SPACINGS = new WarnOnce(GaldurUI.LOGGER::warn);

    /**
     * Replaces the current theme. Missing keys fall back to {@link ThemeDefaults}, so a partial
     * {@code theme.json} stays usable.
     *
     * @param colors  ARGB colours read from {@code theme.json}
     * @param spacing pixel spacings read from its {@code "spacing"} section
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

    public static void resetToDefaults() {
        current = DEFAULTS;
        forgetWarnings();
    }

    /** After a reload, a still unknown key must be reported again. */
    private static void forgetWarnings() {
        UNKNOWN_COLORS.reset();
        UNKNOWN_SPACINGS.reset();
    }

    /**
     * {@return the ARGB colour of {@code key}}
     *
     * <p>An unknown key does not throw: it returns {@link #MISSING_COLOR}, visible at once on
     * screen, and logs a warning once per key until the next reload.</p>
     */
    public static int color(String key) {
        Integer value = current.colors().get(key);
        if (value == null) {
            UNKNOWN_COLORS.warn(key, "Unknown theme colour '" + key + "': using debug magenta.");
            return MISSING_COLOR;
        }
        return value;
    }

    /** {@return the ARGB colour of {@code key}, or {@code fallback} if the key is missing} */
    public static int colorOrDefault(String key, int fallback) {
        Integer value = current.colors().get(key);
        return value == null ? fallback : value;
    }

    public static boolean has(String key) {
        return current.colors().containsKey(key);
    }

    /**
     * {@return the spacing of {@code key}, in GUI pixels}; an unknown key returns {@code 0} with a
     * warning, once per key until the next reload
     */
    public static int spacing(String key) {
        Integer value = current.spacing().get(key);
        if (value == null) {
            UNKNOWN_SPACINGS.warn(key, "Unknown theme spacing '" + key + "': using 0.");
            return 0;
        }
        return value;
    }

    public static Map<String, Integer> all() {
        return current.colors();
    }

    public static Map<String, Integer> allSpacing() {
        return current.spacing();
    }

    private ThemeStore() {
    }
}
