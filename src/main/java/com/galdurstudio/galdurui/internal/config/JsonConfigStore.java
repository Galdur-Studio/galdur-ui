package com.galdurstudio.galdurui.internal.config;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.galdurstudio.galdurui.GaldurUI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link GaldurConfig.Store} backed by a small JSON file, for loaders without a config API
 * (Fabric): {@code config/galdurui-client.json}. A missing or broken file gives the defaults and is
 * never an error.
 */
public final class JsonConfigStore implements GaldurConfig.Store {
    private static final String ANIMATIONS = "animations";
    private static final String RESTYLE_ALL = "restyleAll";
    private static final String EXCLUDED_SCREENS = "excludedScreens";

    private final Path file;
    private volatile boolean animations = true;
    private volatile boolean restyleAll;
    private volatile List<String> excludedScreens = List.of();

    public JsonConfigStore(Path file) {
        this.file = file;
        load();
    }

    @Override
    public boolean animations() {
        return animations;
    }

    @Override
    public void setAnimations(boolean enabled) {
        animations = enabled;
        save();
    }

    @Override
    public boolean restyleAll() {
        return restyleAll;
    }

    @Override
    public void setRestyleAll(boolean enabled) {
        restyleAll = enabled;
        save();
    }

    @Override
    public List<? extends String> excludedScreens() {
        return excludedScreens;
    }

    @Override
    public void setExcludedScreens(List<String> screens) {
        excludedScreens = List.copyOf(screens);
        save();
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has(ANIMATIONS) && json.get(ANIMATIONS).getAsJsonPrimitive().isBoolean()) {
                animations = json.get(ANIMATIONS).getAsBoolean();
            }
            if (json.has(RESTYLE_ALL) && json.get(RESTYLE_ALL).getAsJsonPrimitive().isBoolean()) {
                restyleAll = json.get(RESTYLE_ALL).getAsBoolean();
            }
            if (json.has(EXCLUDED_SCREENS) && json.get(EXCLUDED_SCREENS).isJsonArray()) {
                List<String> excluded = new ArrayList<>();
                for (JsonElement element : json.getAsJsonArray(EXCLUDED_SCREENS)) {
                    excluded.add(element.getAsString());
                }
                excludedScreens = List.copyOf(excluded);
            }
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Could not read {}: using the default settings.", file, exception);
        }
    }

    private void save() {
        JsonObject json = new JsonObject();
        json.addProperty(ANIMATIONS, animations);
        json.addProperty(RESTYLE_ALL, restyleAll);
        JsonArray excluded = new JsonArray();
        excludedScreens.forEach(excluded::add);
        json.add(EXCLUDED_SCREENS, excluded);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(json.toString());
            }
        } catch (IOException exception) {
            GaldurUI.LOGGER.warn("Could not save {}.", file, exception);
        }
    }
}
