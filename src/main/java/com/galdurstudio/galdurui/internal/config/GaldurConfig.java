package com.galdurstudio.galdurui.internal.config;

import java.util.List;

/**
 * Client settings of Galdur UI, in {@code config/galdurui-client.toml} (NeoForge, Forge) or
 * {@code config/galdurui-client.json} (Fabric), and in the settings screen of the Mods menu.
 *
 * <p>These are <strong>player</strong> settings, not settings of the mods using the library:
 * they apply to every Galdur UI interface at once.</p>
 *
 * <p>This class holds no file format: each loader module registers a {@link Store} at startup
 * (a NeoForge or Forge config spec, a JSON file on Fabric).</p>
 */
public final class GaldurConfig {
    /** Where the settings are kept; implemented by each loader module. */
    public interface Store {
        /** {@return {@code true} if animations are enabled, or the default value if not loaded yet} */
        boolean animations();

        /** Changes the setting and saves it to the file. */
        void setAnimations(boolean enabled);

        /** {@return {@code true} if every menu of the game takes the Galdur look; {@code false} by default} */
        boolean restyleAll();

        /** Changes the setting and saves it to the file. */
        void setRestyleAll(boolean enabled);

        /** {@return screen classes or packages the Galdur look must leave alone, empty by default} */
        List<? extends String> excludedScreens();

        /** Changes the setting and saves it to the file. */
        void setExcludedScreens(List<String> screens);
    }

    /** Before a loader registers its store (very early in startup), the defaults apply. */
    private static volatile Store store;

    private GaldurConfig() {
    }

    /** Called once by the loader entry point. */
    public static void register(Store store) {
        GaldurConfig.store = store;
        // Where the player's colours apply (mods and modpacks may lock them). The rules file is read
        // on first use: the game is not created yet when the loaders call this.
        com.galdurstudio.galdurui.internal.theme.ThemeStore.setPlayerThemeFilter(
                com.galdurstudio.galdurui.api.bridge.GaldurBridge::playerThemeAllowedHere);
    }

    /**
     * {@return {@code true} if animations are enabled}
     *
     * <p>Before the file is loaded (very early in startup), returns the default value.</p>
     */
    public static boolean animations() {
        Store current = store;
        return current == null || current.animations();
    }

    /**
     * {@return {@code true} if the player asked for the Galdur look on every menu of the game}
     * ({@code GaldurBridge}); {@code false} until the file is loaded
     */
    public static boolean restyleAll() {
        Store current = store;
        return current != null && current.restyleAll();
    }

    /** Turns the Galdur look on every menu on or off and saves the choice (settings screen). */
    public static void setRestyleAll(boolean enabled) {
        Store current = store;
        if (current != null) {
            current.setRestyleAll(enabled);
        }
        com.galdurstudio.galdurui.api.bridge.GaldurBridge.saveRules();
    }

    /** {@return screen classes or packages the Galdur look on every menu leaves alone} */
    public static List<? extends String> excludedScreens() {
        Store current = store;
        return current == null ? List.of() : current.excludedScreens();
    }

    /** Replaces the menus the Galdur look leaves alone and saves the choice (settings screen). */
    public static void setExcludedScreens(List<String> screens) {
        Store current = store;
        if (current != null) {
            current.setExcludedScreens(List.copyOf(screens));
        }
    }

    /** Turns animations on or off and saves the choice (settings screen). */
    public static void setAnimations(boolean enabled) {
        Store current = store;
        if (current != null) {
            current.setAnimations(enabled);
        }
    }
}
