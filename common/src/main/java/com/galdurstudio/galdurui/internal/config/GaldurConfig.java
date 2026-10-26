package com.galdurstudio.galdurui.internal.config;

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
    }

    /** Before a loader registers its store (very early in startup), the defaults apply. */
    private static volatile Store store;

    private GaldurConfig() {
    }

    /** Called once by the loader entry point. */
    public static void register(Store store) {
        GaldurConfig.store = store;
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

    /** Turns animations on or off and saves the choice (settings screen). */
    public static void setAnimations(boolean enabled) {
        Store current = store;
        if (current != null) {
            current.setAnimations(enabled);
        }
    }
}
