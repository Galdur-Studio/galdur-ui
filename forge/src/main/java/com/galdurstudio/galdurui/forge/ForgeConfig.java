package com.galdurstudio.galdurui.forge;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/** Client settings of Galdur UI on Forge: {@code config/galdurui-client.toml}. */
final class ForgeConfig {
    /** Settings spec, registered by the client entry point. */
    static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ANIMATIONS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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

    private ForgeConfig() {
    }
}
