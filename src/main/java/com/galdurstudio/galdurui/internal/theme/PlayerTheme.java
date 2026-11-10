package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * The player's theme (theme editor), stored in {@code config/galdurui-theme.json}, and the themes
 * shared between players.
 *
 * <p>Every file uses the {@code theme.json} format, with only what the player changed: it also
 * works as {@code assets/galdurui/theme.json} in a resource pack. Before each save, the previous
 * theme is kept in {@code galdurui-theme.backup.json}. Exported themes go to
 * {@code config/galdurui-themes/} and to the clipboard, so they can be pasted in a message. A
 * missing or broken file gives no player theme and is never an error.</p>
 */
public final class PlayerTheme {
    private static final String FILE = "galdurui-theme.json";
    private static final String BACKUP = "galdurui-theme.backup.json";
    private static final String FOLDER = "galdurui-themes";

    /** The player's theme, as read from or written to a file. */
    public record Theme(Map<String, GaldurPaint> paints, Map<String, Integer> spacing, String name, String author) {
        public Theme {
            paints = Map.copyOf(paints);
            spacing = Map.copyOf(spacing);
        }

        /** No change: the Galdur theme (and the resource packs) as they are. */
        public static final Theme EMPTY = new Theme(Map.of(), Map.of(), "", "");

        public boolean isEmpty() {
            return paints.isEmpty() && spacing.isEmpty();
        }
    }

    private PlayerTheme() {
    }

    /** Reads the player's file into {@link ThemeStore}; called after each resource reload. */
    public static void load() {
        Theme theme = read(file());
        ThemeStore.setPlayerTheme(theme.paints(), theme.spacing());
    }

    /** {@return the theme of the player's file, {@link Theme#EMPTY} if there is none} */
    public static Theme current() {
        return read(file());
    }

    /**
     * Saves the player's theme and applies it. The theme it replaces is kept as the backup, so
     * {@link #restoreBackup()} can bring it back.
     */
    public static void save(Theme theme) {
        Path file = file();
        try {
            if (Files.isRegularFile(file)) {
                Files.copy(file, file.resolveSibling(BACKUP), StandardCopyOption.REPLACE_EXISTING);
            }
            if (theme.isEmpty()) {
                Files.deleteIfExists(file);
            } else {
                Files.createDirectories(file.getParent());
                Files.writeString(file, toJson(theme), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            GaldurUI.LOGGER.warn("Could not save {}.", file, exception);
        }
        ThemeStore.setPlayerTheme(theme.paints(), theme.spacing());
    }

    /** {@return {@code true} if a previous theme was kept} */
    public static boolean hasBackup() {
        return Files.isRegularFile(file().resolveSibling(BACKUP));
    }

    /** Brings back the theme replaced by the last save (and keeps the current one as the backup). */
    public static void restoreBackup() {
        Theme backup = read(file().resolveSibling(BACKUP));
        save(backup);
    }

    /**
     * Exports a theme: copies it to the clipboard, to paste in a message, and writes it to
     * {@code config/galdurui-themes/<name>.json}.
     *
     * @return the file written, {@code null} if it could not be written
     */
    @Nullable
    public static Path export(Theme theme) {
        String json = toJson(theme);
        com.galdurstudio.galdurui.internal.client.Client.setClipboard(json);
        Path file = folder().resolve(fileName(theme.name()) + ".json");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, json, StandardCharsets.UTF_8);
            return file;
        } catch (IOException exception) {
            GaldurUI.LOGGER.warn("Could not export the theme to {}.", file, exception);
            return null;
        }
    }

    /**
     * {@return the theme in {@code text} (a {@code theme.json} pasted from a message), {@code null}
     * if it holds no theme}; the text may have other words around the JSON
     */
    @Nullable
    public static Theme parse(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try (Reader reader = new StringReader(text.substring(start, end + 1))) {
            ThemeParser.Theme parsed = ThemeParser.parse(reader, warning -> GaldurUI.LOGGER.warn("Imported theme: {}", warning));
            Theme theme = new Theme(parsed.paints(), parsed.spacing(), parsed.name(), parsed.author());
            return theme.isEmpty() ? null : theme;
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    /** {@return the theme of a file, {@code null} if it holds none} */
    @Nullable
    public static Theme readFile(Path file) {
        try {
            return parse(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    /** {@return the themes saved in {@code config/galdurui-themes/}, by file name} */
    public static List<Path> savedThemes() {
        List<Path> files = new ArrayList<>();
        try (var listing = Files.list(folder())) {
            listing.filter(path -> path.toString().endsWith(".json")).sorted().forEach(files::add);
        } catch (IOException exception) {
            // No folder yet: no saved theme.
        }
        return files;
    }

    /** {@return the folder of exported themes} */
    public static Path folder() {
        return configFolder().resolve(FOLDER);
    }

    /** {@return the content of a theme file, in the format of {@code theme.json}, readable by people} */
    public static String toJson(Theme theme) {
        JsonObject json = new JsonObject();
        json.addProperty("_comment", "Galdur UI theme. Same format as theme.json: paste it in the theme editor, or put it in a resource pack as assets/galdurui/theme.json.");
        json.addProperty("_commentaire", "Thème Galdur UI, au format de theme.json : colle-le dans l'éditeur de thème, ou mets-le dans un pack de ressources (assets/galdurui/theme.json).");
        if (!theme.name().isEmpty()) {
            json.addProperty(ThemeParser.NAME, theme.name());
        }
        if (!theme.author().isEmpty()) {
            json.addProperty(ThemeParser.AUTHOR, theme.author());
        }
        new TreeMap<>(theme.paints()).forEach((key, paint) -> {
            if (paint.isSolid()) {
                json.addProperty(key, hex(paint.first()));
                return;
            }
            JsonArray stops = new JsonArray();
            paint.colors().forEach(argb -> stops.add(hex(argb)));
            if (paint.direction() == GaldurPaint.Direction.VERTICAL) {
                json.add(key, stops);
            } else {
                JsonObject gradient = new JsonObject();
                gradient.add("gradient", stops);
                gradient.addProperty("direction", "horizontal");
                json.add(key, gradient);
            }
        });
        if (!theme.spacing().isEmpty()) {
            JsonObject spacing = new JsonObject();
            new TreeMap<>(theme.spacing()).forEach(spacing::addProperty);
            json.add(ThemeParser.SPACING, spacing);
        }
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json);
    }

    /** {@return the colour as {@code #RRGGBB} when opaque, {@code #AARRGGBB} otherwise} */
    public static String hex(int argb) {
        return (argb >>> 24) == 0xFF
                ? String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF)
                : String.format(Locale.ROOT, "#%08X", argb);
    }

    /** {@return the colour of {@code #RRGGBB} (opaque) or {@code #AARRGGBB}, {@code null} if incomplete or invalid} */
    @Nullable
    static Integer parseHex(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6 && hex.length() != 8) {
            return null;
        }
        try {
            int value = Integer.parseUnsignedInt(hex, 16);
            return hex.length() == 6 ? 0xFF000000 | value : value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /** {@return a file name for a theme: lower case letters, digits and dashes; "theme" when empty} */
    static String fileName(String name) {
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return plain.isEmpty() ? "theme" : plain;
    }

    private static Theme read(Path file) {
        if (!Files.isRegularFile(file)) {
            return Theme.EMPTY;
        }
        Theme theme = readFile(file);
        if (theme == null) {
            GaldurUI.LOGGER.warn("Could not read {}: keeping the resource pack theme.", file);
            return Theme.EMPTY;
        }
        return theme;
    }

    private static Path file() {
        return configFolder().resolve(FILE);
    }

    private static Path configFolder() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }
}
