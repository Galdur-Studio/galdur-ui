package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Charge {@code assets/galdurui/theme.json} et alimente {@link ThemeStore}.
 *
 * <p>Branché sur le gestionnaire de ressources client : le thème est donc relu à chaque
 * rechargement des ressources, y compris via F3+T, ce qui permet d'ajuster les couleurs
 * sans redémarrer le jeu. Comme le fichier vit dans {@code assets/}, un pack de ressources
 * peut le remplacer pour retoucher la charte.</p>
 *
 * <p>Format attendu : un objet de noms de couleurs vers des chaînes hexadécimales
 * {@code "#RRGGBB"} ou {@code "#AARRGGBB"} (le {@code #} est optionnel, un entier JSON est
 * également accepté), plus une section {@code "spacing"} d'entiers en pixels d'interface.</p>
 *
 * <pre>{@code
 * { "background": "#2A2420", "accent": "#B8341F", "spacing": { "gap": 8 } }
 * }</pre>
 */
public final class ThemeLoader implements ResourceManagerReloadListener {
    /** Emplacement du thème, résolu en {@code assets/galdurui/theme.json}. */
    private static final ResourceLocation THEME = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "theme.json");

    /** Nom de la section des espacements dans {@code theme.json}. */
    private static final String SPACING = "spacing";

    /**
     * Enregistre le chargeur sur le gestionnaire de ressources client.
     *
     * @param modEventBus bus d'événements du mod, fourni au constructeur du point d'entrée client
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener(new ThemeLoader()));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(THEME);
        if (resource.isEmpty()) {
            GaldurUI.LOGGER.warn("theme.json introuvable ({}) : utilisation des couleurs par défaut de la charte.", THEME);
            ThemeStore.resetToDefaults();
            return;
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                GaldurUI.LOGGER.warn("theme.json n'est pas un objet JSON : utilisation des couleurs par défaut.");
                ThemeStore.resetToDefaults();
                return;
            }
            JsonObject json = root.getAsJsonObject();
            ThemeStore.apply(readColors(json), readSpacing(json));
            GaldurUI.LOGGER.debug("Thème Galdur UI chargé ({} couleurs, {} espacements).",
                    ThemeStore.all().size(), ThemeStore.allSpacing().size());
        } catch (IOException | RuntimeException exception) {
            // Un thème cassé ne doit jamais empêcher le jeu de démarrer : on se rabat sur la charte.
            GaldurUI.LOGGER.warn("Lecture de theme.json impossible : utilisation des couleurs par défaut.", exception);
            ThemeStore.resetToDefaults();
        }
    }

    /**
     * Convertit les entrées de l'objet JSON en couleurs ARGB.
     *
     * <p>Les entrées invalides sont ignorées une à une, avec un avertissement : une seule
     * couleur mal écrite ne fait pas perdre tout le fichier.</p>
     */
    private static Map<String, Integer> readColors(JsonObject json) {
        Map<String, Integer> colors = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            // Clé réservée aux commentaires du fichier, ou section d'espacements : pas une couleur.
            if (key.startsWith("_") || key.equals(SPACING)) {
                continue;
            }
            try {
                colors.put(key, parseColor(entry.getValue()));
            } catch (RuntimeException exception) {
                GaldurUI.LOGGER.warn("Couleur '{}' invalide dans theme.json ({}) : entrée ignorée.", key, entry.getValue());
            }
        }
        return colors;
    }

    /**
     * Lit la section {@code "spacing"} : des entiers en pixels d'interface.
     *
     * <p>Section absente : tous les espacements retombent sur la charte. Comme pour les
     * couleurs, une entrée invalide est ignorée seule, avec un avertissement.</p>
     */
    private static Map<String, Integer> readSpacing(JsonObject json) {
        Map<String, Integer> spacing = new LinkedHashMap<>();
        if (!json.has(SPACING)) {
            return spacing;
        }
        if (!json.get(SPACING).isJsonObject()) {
            GaldurUI.LOGGER.warn("La section '{}' de theme.json n'est pas un objet : espacements par défaut.", SPACING);
            return spacing;
        }
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(SPACING).entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && value.getAsInt() >= 0) {
                spacing.put(key, value.getAsInt());
            } else {
                GaldurUI.LOGGER.warn("Espacement '{}' invalide dans theme.json ({}) : entrée ignorée.", key, value);
            }
        }
        return spacing;
    }

    /** {@return la couleur ARGB décrite par {@code element}} */
    private static int parseColor(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("couleur attendue sous forme de chaîne ou d'entier");
        }
        if (element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }

        String raw = element.getAsString().trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        // 6 chiffres : on complète l'alpha à 100 %. 8 chiffres : alpha explicite (AARRGGBB).
        return switch (raw.length()) {
            case 6 -> 0xFF000000 | Integer.parseInt(raw, 16);
            case 8 -> (int) Long.parseLong(raw, 16);
            default -> throw new IllegalArgumentException("longueur hexadécimale inattendue : " + raw.length());
        };
    }
}
