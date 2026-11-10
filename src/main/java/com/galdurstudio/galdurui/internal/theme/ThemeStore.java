package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Holds the current theme (colours, gradients, spacings). The public {@code api.theme.GaldurTheme}
 * only reads from here. Three layers, each over the previous one: the defaults of the jar, the
 * resource packs' {@code theme.json}, and the player's theme (theme editor). Replacement swaps a
 * reference to an immutable snapshot, so the render thread never sees a half-loaded theme.
 *
 * <p>An element colour that no layer sets ({@code button}, {@code field}...) takes the colour it
 * falls back on ({@link GaldurTheme#parentOf}). Where the player's theme is not allowed (a mod
 * locked its screens, or the modpack locked the theme), the snapshot without it is used.</p>
 */
public final class ThemeStore {
    /** Debug magenta returned for an unknown colour. */
    public static final int MISSING_COLOR = 0xFFFF00FF;

    private record Snapshot(Map<String, GaldurPaint> paints, Map<String, Integer> spacing, Map<String, Integer> colors) {
        static Snapshot of(Map<String, GaldurPaint> paints, Map<String, Integer> spacing) {
            Map<String, Integer> colors = new LinkedHashMap<>();
            paints.forEach((key, paint) -> colors.put(key, paint.first()));
            return new Snapshot(paints, spacing, Collections.unmodifiableMap(colors));
        }
    }

    private static volatile Map<String, GaldurPaint> packPaints = Map.of();
    private static volatile Map<String, Integer> packSpacing = Map.of();
    private static volatile Map<String, GaldurPaint> playerPaints = Map.of();
    private static volatile Map<String, Integer> playerSpacing = Map.of();

    private static volatile Snapshot withPlayer = Snapshot.of(ThemeDefaults.PAINTS, ThemeDefaults.SPACING);
    private static volatile Snapshot packOnly = withPlayer;
    /** {@code false} where the player's theme must not apply; set by the client at startup. */
    private static volatile BooleanSupplier playerThemeAllowed = () -> true;

    private static final WarnOnce UNKNOWN_COLORS = new WarnOnce(GaldurUI.LOGGER::warn);
    private static final WarnOnce UNKNOWN_SPACINGS = new WarnOnce(GaldurUI.LOGGER::warn);

    private ThemeStore() {
    }

    /**
     * Replaces the resource packs' theme. Missing keys fall back to {@link ThemeDefaults}, so a
     * partial {@code theme.json} stays usable.
     */
    public static void apply(Map<String, GaldurPaint> paints, Map<String, Integer> spacing) {
        packPaints = Map.copyOf(paints);
        packSpacing = Map.copyOf(spacing);
        rebuild();
    }

    /** Forgets the resource packs' theme: the jar defaults apply. */
    public static void resetToDefaults() {
        packPaints = Map.of();
        packSpacing = Map.of();
        rebuild();
    }

    /** Replaces the player's theme (theme editor), applied over the resource packs on the next frame. */
    public static void setPlayerTheme(Map<String, GaldurPaint> paints, Map<String, Integer> spacing) {
        playerPaints = Map.copyOf(paints);
        playerSpacing = Map.copyOf(spacing);
        rebuild();
    }

    /** {@return the paints the player chose, without the packs' and the defaults} */
    public static Map<String, GaldurPaint> playerPaints() {
        return playerPaints;
    }

    /** {@return the spacings the player chose, without the packs' and the defaults} */
    public static Map<String, Integer> playerSpacing() {
        return playerSpacing;
    }

    /** Sets where the player's theme applies: {@code false} uses the packs' theme only. */
    public static void setPlayerThemeFilter(BooleanSupplier allowed) {
        playerThemeAllowed = allowed;
    }

    /** {@return the paint of {@code key} without the player's theme: resource packs, then defaults} */
    public static GaldurPaint packPaint(String key) {
        GaldurPaint paint = find(packOnly.paints(), key);
        return paint != null ? paint : GaldurPaint.solid(MISSING_COLOR);
    }

    private static void rebuild() {
        Map<String, GaldurPaint> packs = merge(ThemeDefaults.PAINTS, packPaints);
        Map<String, Integer> spacing = merge(ThemeDefaults.SPACING, packSpacing);
        packOnly = Snapshot.of(packs, spacing);
        withPlayer = Snapshot.of(merge(packs, playerPaints), merge(spacing, playerSpacing));
        forgetWarnings();
    }

    private static <T> Map<String, T> merge(Map<String, T> base, Map<String, T> over) {
        Map<String, T> merged = new LinkedHashMap<>(base);
        merged.putAll(over);
        return Collections.unmodifiableMap(merged);
    }

    /** After a reload, a still unknown key must be reported again. */
    private static void forgetWarnings() {
        UNKNOWN_COLORS.reset();
        UNKNOWN_SPACINGS.reset();
    }

    private static Snapshot current() {
        return playerThemeAllowed.getAsBoolean() ? withPlayer : packOnly;
    }

    /** {@return the paint of {@code key}, following the element fallbacks; {@code null} if none} */
    private static GaldurPaint find(Map<String, GaldurPaint> paints, String key) {
        for (String current = key; current != null; current = GaldurTheme.parentOf(current)) {
            GaldurPaint paint = paints.get(current);
            if (paint != null) {
                return paint;
            }
        }
        return null;
    }

    /** {@return the paint of {@code key}; debug magenta with a warning if unknown} */
    public static GaldurPaint paint(String key) {
        GaldurPaint paint = find(current().paints(), key);
        if (paint == null) {
            UNKNOWN_COLORS.warn(key, "Unknown theme colour '" + key + "': using debug magenta.");
            return GaldurPaint.solid(MISSING_COLOR);
        }
        return paint;
    }

    /** {@return the colour of {@code key}: the whole paint when solid, the start of a gradient} */
    public static int color(String key) {
        Integer value = current().colors().get(key);
        return value != null ? value : paint(key).first();
    }

    /** {@return the colour of {@code key}, or {@code fallback} if unknown (no warning)} */
    public static int colorOrDefault(String key, int fallback) {
        GaldurPaint paint = find(current().paints(), key);
        return paint == null ? fallback : paint.first();
    }

    /** {@return {@code true} if {@code key} has a colour, directly or through its fallback} */
    public static boolean has(String key) {
        return find(current().paints(), key) != null;
    }

    /** {@return the spacing of {@code key}, in GUI pixels; {@code 0} with a warning if unknown} */
    public static int spacing(String key) {
        Integer value = current().spacing().get(key);
        if (value == null) {
            UNKNOWN_SPACINGS.warn(key, "Unknown theme spacing '" + key + "': using 0.");
            return 0;
        }
        return value;
    }

    /** {@return every colour of the current theme (first colour of gradients)} */
    public static Map<String, Integer> all() {
        return current().colors();
    }

    /** {@return every spacing of the current theme} */
    public static Map<String, Integer> allSpacing() {
        return current().spacing();
    }
}
