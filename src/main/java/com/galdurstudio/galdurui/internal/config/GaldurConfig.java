package com.galdurstudio.galdurui.internal.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client settings of Galdur UI, in {@code config/galdurui-client.toml} and in the mod config
 * screen (Mods menu → Galdur UI → Config).
 *
 * <p>These are <strong>player</strong> settings, not settings of the mods using the library:
 * they apply to every Galdur UI interface at once.</p>
 */
public final class GaldurConfig {
    /** Settings spec, registered by the client entry point. */
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ANIMATIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ANIMATIONS = builder
                .comment("Animate Galdur UI screens: hover fades, screen opening, tab changes, smooth scrolling.",
                        "Turn off for instant changes (reduced motion).")
                .translation("galdurui.configuration.animations")
                .define("animations", true);
        SPEC = builder.build();
    }

    private GaldurConfig() {
    }

    /**
     * {@return {@code true} if animations are enabled}
     *
     * <p>Before the file is loaded (very early in startup), returns the default value.</p>
     */
    public static boolean animations() {
        return !SPEC.isLoaded() || ANIMATIONS.get();
    }
}
