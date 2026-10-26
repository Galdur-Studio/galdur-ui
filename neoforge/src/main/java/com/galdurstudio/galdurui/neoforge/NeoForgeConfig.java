package com.galdurstudio.galdurui.neoforge;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings of Galdur UI on NeoForge: {@code config/galdurui-client.toml}. */
final class NeoForgeConfig {
    /** Settings spec, registered by the client entry point. */
    static final ModConfigSpec SPEC;

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

    static final GaldurConfig.Store STORE = new GaldurConfig.Store() {
        @Override
        public boolean animations() {
            return !SPEC.isLoaded() || ANIMATIONS.get();
        }

        @Override
        public void setAnimations(boolean enabled) {
            ANIMATIONS.set(enabled);
            SPEC.save();
        }
    };

    private NeoForgeConfig() {
    }
}
