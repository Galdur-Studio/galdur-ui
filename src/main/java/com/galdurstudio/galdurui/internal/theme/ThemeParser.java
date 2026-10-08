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
 * Lecture du format {@code theme.json}, sans dépendance à Minecraft.
 *
 * <p>Utilisé par {@link ThemeLoader} (thème des ressources, relu à chaque rechargement) et par
 * {@link ThemeDefaults} (thème embarqué dans le jar, servant de valeurs par défaut). Ne dépend
 * que de Gson, ce qui permet de le tester sans démarrer le jeu.</p>
 *
 * <p>Format : un objet de noms de couleurs vers des chaînes hexadécimales {@code "#RRGGBB"}
 * (alpha forcé à 100 %) ou {@code "#AARRGGBB"} (le {@code #} est optionnel ; un entier JSON est
 * également accepté, et lu comme {@code RRGGBB} opaque s'il tient sur 24 bits), plus une
 * section {@code "spacing"} d'entiers positifs ou nuls, en pixels d'interface. Les clés qui
 * commencent par {@code _} sont des commentaires.</p>
 *
 * <pre>{@code
 * { "background": "#2A2420", "accent": "#B8341F", "spacing": { "gap": 8 } }
 * }</pre>
 *
 * <p>Les entrées invalides sont ignorées une à une, avec un avertissement : une seule valeur
 * mal écrite ne fait pas perdre tout le fichier.</p>
 */
public final class ThemeParser {
    public static final String SPACING = "spacing";

    /** Octet alpha à 100 %, ajouté aux couleurs données sans alpha. */
    private static final int OPAQUE = 0xFF000000;
    /** Plus grande couleur sans octet alpha ({@code RRGGBB}). */
    private static final int RGB_MASK = 0xFFFFFF;

    /**
     * Thème lu : couleurs en ARGB et espacements en pixels, dans l'ordre du fichier.
     *
     * @param colors  couleurs, par nom
     * @param spacing espacements, par nom
     */
    public record Theme(Map<String, Integer> colors, Map<String, Integer> spacing) {
        public Theme {
            colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
            spacing = Collections.unmodifiableMap(new LinkedHashMap<>(spacing));
        }
    }

    /**
     * Lit un thème.
     *
     * @param reader source JSON, non fermée par cette méthode
     * @param warn   reçoit un message pour chaque entrée ignorée ou corrigée
     * @return le thème lu
     * @throws IllegalArgumentException si le document n'est pas un objet JSON
     * @throws com.google.gson.JsonParseException si le JSON est mal formé
     */
    public static Theme parse(Reader reader, Consumer<String> warn) {
        JsonElement root = JsonParser.parseReader(reader);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("theme.json n'est pas un objet JSON");
        }
        JsonObject json = root.getAsJsonObject();
        return new Theme(readColors(json, warn), readSpacing(json, warn));
    }

    private static Map<String, Integer> readColors(JsonObject json, Consumer<String> warn) {
        Map<String, Integer> colors = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            // Clé réservée aux commentaires du fichier, ou section d'espacements : pas une couleur.
            if (key.startsWith("_") || key.equals(SPACING)) {
                continue;
            }
            try {
                colors.put(key, parseColor(key, entry.getValue(), warn));
            } catch (RuntimeException exception) {
                warn.accept("Couleur '" + key + "' invalide dans theme.json (" + entry.getValue() + ") : entrée ignorée.");
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
            warn.accept("La section '" + SPACING + "' de theme.json n'est pas un objet : espacements par défaut.");
            return spacing;
        }
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(SPACING).entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                warn.accept("Espacement '" + key + "' invalide dans theme.json (" + value + ") : nombre attendu, entrée ignorée.");
                continue;
            }
            double number = value.getAsDouble();
            if (number < 0) {
                warn.accept("Espacement '" + key + "' négatif dans theme.json (" + value + ") : entrée ignorée.");
                continue;
            }
            int pixels = (int) Math.floor(number);
            if (pixels != number) {
                warn.accept("Espacement '" + key + "' décimal dans theme.json (" + value + ") : arrondi à " + pixels + ".");
            }
            spacing.put(key, pixels);
        }
        return spacing;
    }

    /**
     * {@return la couleur ARGB décrite par {@code element}}
     *
     * <p>Un entier qui tient sur 24 bits ({@code <= 0xFFFFFF}) n'a pas d'octet alpha : il est lu
     * comme {@code RRGGBB} opaque, avec un avertissement, plutôt que de donner une couleur
     * invisible. Un entier plus grand est pris tel quel ({@code AARRGGBB}).</p>
     */
    private static int parseColor(String key, JsonElement element, Consumer<String> warn) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("couleur attendue sous forme de chaîne ou d'entier");
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            int value = primitive.getAsInt();
            if (value >= 0 && value <= RGB_MASK) {
                warn.accept("Couleur '" + key + "' donnée en entier sans alpha dans theme.json (" + element
                        + ") : alpha forcé à FF. Écrire plutôt \"#" + String.format("%06X", value) + "\".");
                return OPAQUE | value;
            }
            return value;
        }

        String raw = primitive.getAsString().trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        // 6 chiffres : on complète l'alpha à 100 %. 8 chiffres : alpha explicite (AARRGGBB).
        return switch (raw.length()) {
            case 6 -> OPAQUE | Integer.parseInt(raw, 16);
            case 8 -> (int) Long.parseLong(raw, 16);
            default -> throw new IllegalArgumentException("longueur hexadécimale inattendue : " + raw.length());
        };
    }

    private ThemeParser() {
    }
}
