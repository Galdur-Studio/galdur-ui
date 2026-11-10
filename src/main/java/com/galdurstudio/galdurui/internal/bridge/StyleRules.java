package com.galdurstudio.galdurui.internal.bridge;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.bridge.GaldurBridge.Element;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Where each part of the Galdur look applies: one switch per {@link Element} and per scope.
 *
 * <p>Scopes, from the widest to the most precise: {@code *} (every screen),
 * {@code group:inventories} and {@code group:menus} (vanilla screens with and without slots),
 * {@code mod:<package>} (a mod's screens, by Java package) or {@code modid:<id>} (its menus with
 * slots, by mod id), and {@code screen:<class>} (one screen and its subclasses). For a screen and
 * an element, the most precise scope that says something wins.</p>
 *
 * <p>Three sources, in this order: locks (the modpack's {@code "locked"} section of
 * {@code config/galdurui-style.json}, then the mods' {@code GaldurBridge.lock}), which the player
 * cannot change; the player's rules (the {@code "rules"} section, set in the settings); and the
 * defaults (what mods registered with {@code GaldurBridge.restyle}, and {@code restyleAll}).
 * Pure logic: no Minecraft class, so it can be unit tested.</p>
 */
public final class StyleRules {
    public static final String ALL = "*";
    public static final String INVENTORIES = "group:inventories";
    public static final String MENUS = "group:menus";

    /** What a screen is, to find its scopes. */
    public record Target(String className, List<String> parentClasses, boolean hasSlots, @Nullable String menuNamespace) {
        boolean vanilla() {
            return className.startsWith("net.minecraft.");
        }
    }

    /** Where a value comes from, to show it in the settings. */
    public enum Source { DEFAULT, PLAYER, LOCKED }

    /** A value and where it comes from. */
    public record Answer(boolean value, Source source, String scope) {
    }

    private final Map<String, Map<Element, Boolean>> player = new ConcurrentHashMap<>();
    private final Map<String, Map<Element, Boolean>> packLocks = new ConcurrentHashMap<>();
    private final Map<String, Map<Element, Boolean>> modLocks = new ConcurrentHashMap<>();

    // --- reading

    /**
     * {@return whether {@code element} applies to {@code target}, and why}
     *
     * @param fallback the default when nobody says anything: what mods registered and restyleAll
     */
    public Answer resolve(Target target, Element element, boolean fallback) {
        List<String> scopes = scopesOf(target);
        Answer locked = find(packLocks, scopes, element, Source.LOCKED);
        if (locked == null) {
            locked = find(modLocks, scopes, element, Source.LOCKED);
        }
        if (locked != null) {
            return locked;
        }
        Answer chosen = find(player, scopes, element, Source.PLAYER);
        return chosen != null ? chosen : new Answer(fallback, Source.DEFAULT, "");
    }

    /** {@return the player's value for a scope, {@code null} when the scope inherits} */
    @Nullable
    public Boolean playerValue(String scope, Element element) {
        Map<Element, Boolean> values = player.get(scope);
        return values == null ? null : values.get(element);
    }

    /** {@return the locked value of a scope itself, {@code null} if it is not locked there} */
    @Nullable
    public Boolean lockedValue(String scope, Element element) {
        Map<Element, Boolean> pack = packLocks.get(scope);
        if (pack != null && pack.containsKey(element)) {
            return pack.get(element);
        }
        Map<Element, Boolean> mod = modLocks.get(scope);
        return mod == null ? null : mod.get(element);
    }

    /** {@return the scopes that apply to a screen, the most precise first} */
    public static List<String> scopesOf(Target target) {
        List<String> scopes = new ArrayList<>();
        scopes.add("screen:" + target.className());
        for (String parent : target.parentClasses()) {
            scopes.add("screen:" + parent);
        }
        if (target.menuNamespace() != null && !target.menuNamespace().equals("minecraft")) {
            scopes.add("modid:" + target.menuNamespace());
        }
        // Packages from the longest to the shortest: "com.example.mymod.client", "com.example.mymod"...
        String name = target.className();
        for (int dot = name.lastIndexOf('.'); dot > 0; dot = name.lastIndexOf('.', dot - 1)) {
            scopes.add("mod:" + name.substring(0, dot));
        }
        if (target.vanilla()) {
            scopes.add(target.hasSlots() ? INVENTORIES : MENUS);
        }
        scopes.add(ALL);
        return scopes;
    }

    /** {@return the scope a mod's screens are grouped under in the settings: its first three packages} */
    public static String modScope(String className) {
        String[] parts = className.split("\\.");
        int keep = Math.min(3, parts.length - 1);
        return "mod:" + String.join(".", java.util.Arrays.copyOf(parts, Math.max(1, keep)));
    }

    @Nullable
    private static Answer find(Map<String, Map<Element, Boolean>> layer, List<String> scopes, Element element, Source source) {
        for (String scope : scopes) {
            Map<Element, Boolean> values = layer.get(scope);
            if (values != null && values.containsKey(element)) {
                return new Answer(values.get(element), source, scope);
            }
        }
        return null;
    }

    // --- writing

    /** Sets or clears ({@code null}: inherit) the player's value of a scope. */
    public void setPlayer(String scope, Element element, @Nullable Boolean value) {
        Map<Element, Boolean> values = player.computeIfAbsent(scope, key -> new ConcurrentHashMap<>());
        if (value == null) {
            values.remove(element);
        } else {
            values.put(element, value);
        }
        if (values.isEmpty()) {
            player.remove(scope);
        }
    }

    /** Locks a value for a mod ({@code GaldurBridge.lock}). */
    public void lockForMod(String scope, Element element, boolean value) {
        modLocks.computeIfAbsent(scope, key -> new ConcurrentHashMap<>()).put(element, value);
    }

    /** Forgets every rule of the player. */
    public void clearPlayer() {
        player.clear();
    }

    /** {@return the scopes the player or a lock set something on} */
    public Set<String> knownScopes() {
        Set<String> scopes = new java.util.TreeSet<>(player.keySet());
        scopes.addAll(packLocks.keySet());
        scopes.addAll(modLocks.keySet());
        return scopes;
    }

    // --- file

    /** Reads {@code "rules"} (the player's) and {@code "locked"} (the modpack's) from a JSON text. */
    public void read(Reader reader) {
        JsonElement root = JsonParser.parseReader(reader);
        player.clear();
        packLocks.clear();
        if (!root.isJsonObject()) {
            return;
        }
        readLayer(root.getAsJsonObject().get("rules"), player);
        readLayer(root.getAsJsonObject().get("locked"), packLocks);
    }

    /** {@return the file content: the player's rules and the modpack's locks, kept as they were} */
    public String write() {
        JsonObject root = new JsonObject();
        root.addProperty("_comment", "Where the Galdur look applies. \"rules\": set in the Galdur UI settings. "
                + "\"locked\": for modpacks, values players cannot change. Scopes: \"*\", \"group:inventories\", "
                + "\"group:menus\", \"mod:<package>\", \"modid:<mod id>\", \"screen:<class>\". Elements: "
                + String.join(", ", Element.ids()) + ".");
        root.addProperty("_commentaire", "Où s'applique le style Galdur. \"rules\" : réglé dans les réglages de Galdur UI. "
                + "\"locked\" : pour les modpacks, valeurs que les joueurs ne peuvent pas changer.");
        root.add("rules", writeLayer(player));
        root.add("locked", writeLayer(packLocks));
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root);
    }

    /** Loads the file; a missing or broken file gives no rule and is never an error. */
    public void load(Path file) {
        if (!Files.isRegularFile(file)) {
            player.clear();
            packLocks.clear();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            read(reader);
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Could not read {}: no style rule.", file, exception);
        }
    }

    public void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, write(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            GaldurUI.LOGGER.warn("Could not save {}.", file, exception);
        }
    }

    private static void readLayer(@Nullable JsonElement element, Map<String, Map<Element, Boolean>> layer) {
        if (element == null || !element.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> scope : element.getAsJsonObject().entrySet()) {
            if (!scope.getValue().isJsonObject()) {
                continue;
            }
            Map<Element, Boolean> values = new ConcurrentHashMap<>();
            for (Map.Entry<String, JsonElement> entry : scope.getValue().getAsJsonObject().entrySet()) {
                Element parsed = Element.byId(entry.getKey().toLowerCase(Locale.ROOT));
                JsonElement value = entry.getValue();
                if (parsed != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                    values.put(parsed, value.getAsBoolean());
                }
            }
            if (!values.isEmpty()) {
                layer.put(scope.getKey().trim(), values);
            }
        }
    }

    private static JsonObject writeLayer(Map<String, Map<Element, Boolean>> layer) {
        JsonObject json = new JsonObject();
        new TreeMap<>(layer).forEach((scope, values) -> {
            JsonObject entries = new JsonObject();
            Map<Element, Boolean> sorted = new LinkedHashMap<>();
            for (Element element : Element.values()) {
                if (values.containsKey(element)) {
                    sorted.put(element, values.get(element));
                }
            }
            sorted.forEach((element, value) -> entries.addProperty(element.id(), value));
            json.add(scope, entries);
        });
        return json;
    }

    /** {@return the elements, for display} */
    public static List<Element> elements() {
        return Collections.unmodifiableList(List.of(Element.values()));
    }
}
