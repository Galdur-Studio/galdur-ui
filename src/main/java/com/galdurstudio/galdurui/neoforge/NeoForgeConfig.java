package com.galdurstudio.galdurui.neoforge;

import java.util.List;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings of Galdur UI on NeoForge: {@code config/galdurui-client.toml}. */
final class NeoForgeConfig {
    /** Settings spec, registered by the client entry point. */
    static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ANIMATIONS;
    private static final ModConfigSpec.BooleanValue RESTYLE_ALL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_SCREENS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ANIMATIONS = builder
                .comment("Animate Galdur UI screens: hover fades, screen opening, tab changes, smooth scrolling.",
                        "Turn off for instant changes (reduced motion).",
                        "FR : animations des écrans Galdur UI (survol, ouverture, onglets, défilement). Désactiver pour des changements immédiats.")
                .translation("galdurui.configuration.animations")
                .define("animations", true);
        RESTYLE_ALL = builder
                .comment("Give the Galdur look to the buttons, text fields, checkboxes and sliders of every menu,",
                        "other mods included. Vanilla chests and hoppers also get the Galdur panel.",
                        "FR : style Galdur pour les boutons, champs, cases et curseurs de tous les menus, autres mods compris.")
                .translation("galdurui.configuration.restyleAll")
                .define("restyleAll", false);
        EXCLUDED_SCREENS = builder
                .comment("Menus to leave as they are, even when a mod restyles them, by class name or package,",
                        "for instance \"net.minecraft.client.gui.screens.TitleScreen\" or \"com.example.mymod\".",
                        "Find a menu's class name in the log, after \"Galdur look given to\".",
                        "FR : menus laissés tels quels, par classe ou package. Le nom de classe est dans le log, après \"Galdur look given to\".")
                .translation("galdurui.configuration.excludedScreens")
                .defineListAllowEmpty("excludedScreens", List.of(), () -> "", value -> value instanceof String);
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

        @Override
        public boolean restyleAll() {
            return SPEC.isLoaded() && RESTYLE_ALL.get();
        }

        @Override
        public void setRestyleAll(boolean enabled) {
            RESTYLE_ALL.set(enabled);
            SPEC.save();
        }

        @Override
        public List<? extends String> excludedScreens() {
            return SPEC.isLoaded() ? EXCLUDED_SCREENS.get() : List.of();
        }

        @Override
        public void setExcludedScreens(List<String> screens) {
            EXCLUDED_SCREENS.set(screens);
            SPEC.save();
        }
    };

    private NeoForgeConfig() {
    }
}
