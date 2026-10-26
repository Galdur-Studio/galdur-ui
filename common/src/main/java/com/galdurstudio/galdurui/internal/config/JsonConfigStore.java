package com.galdurstudio.galdurui.internal.config;

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

    private final Path file;
    private volatile boolean animations = true;

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

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has(ANIMATIONS) && json.get(ANIMATIONS).getAsJsonPrimitive().isBoolean()) {
                animations = json.get(ANIMATIONS).getAsBoolean();
            }
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Could not read {}: using the default settings.", file, exception);
        }
    }

    private void save() {
        JsonObject json = new JsonObject();
        json.addProperty(ANIMATIONS, animations);
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
