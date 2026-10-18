package com.galdurstudio.galdurui.internal.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reads the {@code theme.json} format. Depends on Gson only, so it can be unit tested without the
 * game.
 *
 * <p>Format: an object mapping colour names to {@code "#RRGGBB"} (opaque) or {@code "#AARRGGBB"}
 * strings ({@code #} optional; a JSON integer is accepted too, read as opaque {@code RRGGBB} if it
 * fits in 24 bits), plus a {@code "spacing"} section of non-negative integers, in GUI pixels. Keys
 * starting with {@code _} are comments.</p>
 *
 * <pre>{@code
 * { "background": "#2A2420", "accent": "#B8341F", "spacing": { "gap": 8 } }
 * }</pre>
 *
 * <p>Invalid entries are skipped one by one, with a warning: one typo does not lose the whole
 * file.</p>
 */
public final class ThemeParser {
    public static final String SPACING = "spacing";

    private static final int OPAQUE = 0xFF000000;
    private static final int RGB_MASK = 0xFFFFFF;

    /**
     * A parsed theme: ARGB colours and pixel spacings, in file order.
     *
     * @param colors  colours by name
     * @param spacing spacings by name
     */
    public record Theme(Map<String, Integer> colors, Map<String, Integer> spacing) {
        public Theme {
            colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
            spacing = Collections.unmodifiableMap(new LinkedHashMap<>(spacing));
        }
    }

    /**
     * Parses a theme.
     *
     * @param reader JSON source, not closed by this method
     * @param warn   receives a message for each skipped or corrected entry
     * @return the parsed theme
     * @throws IllegalArgumentException if the document is not a JSON object
     * @throws com.google.gson.JsonParseException if the JSON is malformed
     */
    public static Theme parse(Reader reader, Consumer<String> warn) {
        JsonElement root = JsonParser.parseReader(reader);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("theme.json is not a JSON object");
        }
        JsonObject json = root.getAsJsonObject();
        return new Theme(readColors(json, warn), readSpacing(json, warn));
    }

    private static Map<String, Integer> readColors(JsonObject json, Consumer<String> warn) {
        Map<String, Integer> colors = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_") || key.equals(SPACING)) {
                continue;
            }
            try {
                colors.put(key, parseColor(key, entry.getValue(), warn));
            } catch (RuntimeException exception) {
                warn.accept("Invalid colour '" + key + "' in theme.json (" + entry.getValue() + "): entry ignored.");
            }
        }
        return colors;
    }

    private static Map<String, Integer> readSpacing(JsonObject json, Consumer<String> warn) {
        Map<String, Integer> spacing = new LinkedHashMap<>();
        if (!json.has(SPACING)) {
            return spacing;
        }
        if (!json.get(SPACING).isJsonObject()) {
            warn.accept("The '" + SPACING + "' section of theme.json is not an object: default spacings used.");
            return spacing;
        }
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(SPACING).entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                warn.accept("Invalid spacing '" + key + "' in theme.json (" + value + "): number expected, entry ignored.");
                continue;
            }
            double number = value.getAsDouble();
            if (number < 0) {
                warn.accept("Negative spacing '" + key + "' in theme.json (" + value + "): entry ignored.");
                continue;
            }
            int pixels = (int) Math.floor(number);
            if (pixels != number) {
                warn.accept("Decimal spacing '" + key + "' in theme.json (" + value + "): rounded down to " + pixels + ".");
            }
            spacing.put(key, pixels);
        }
        return spacing;
    }

    /**
     * {@return the ARGB colour described by {@code element}}
     *
     * <p>An integer that fits in 24 bits has no alpha byte: it is read as opaque {@code RRGGBB},
     * with a warning, rather than giving an invisible colour. A larger integer is taken as
     * {@code AARRGGBB}.</p>
     */
    private static int parseColor(String key, JsonElement element, Consumer<String> warn) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("colour must be a string or an integer");
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            int value = primitive.getAsInt();
            if (value >= 0 && value <= RGB_MASK) {
                warn.accept("Colour '" + key + "' given as an integer without alpha in theme.json (" + element
                        + "): alpha forced to FF. Prefer \"#" + String.format("%06X", value) + "\".");
                return OPAQUE | value;
            }
            return value;
        }

        String raw = primitive.getAsString().trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        return switch (raw.length()) {
            case 6 -> OPAQUE | Integer.parseInt(raw, 16);
            case 8 -> (int) Long.parseLong(raw, 16);
            default -> throw new IllegalArgumentException("unexpected hexadecimal length: " + raw.length());
        };
    }

    private ThemeParser() {
    }
}
