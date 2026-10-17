package com.galdurstudio.galdurui.internal.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Réglages côté client de Galdur UI, dans {@code config/galdurui-client.toml} et dans l'écran de
 * configuration du mod (menu Mods → Galdur UI → Configurer).
 *
 * <p>Réglages du <strong>joueur</strong>, pas des mods qui utilisent la bibliothèque : ils
 * s'appliquent à toutes les interfaces Galdur UI à la fois.</p>
 */
public final class GaldurConfig {
    /** Description des réglages, enregistrée par le point d'entrée client. */
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
     * {@return {@code true} si les animations sont activées}
     *
     * <p>Avant le chargement du fichier (tout début du démarrage), renvoie la valeur par
     * défaut.</p>
     */
    public static boolean animations() {
        return !SPEC.isLoaded() || ANIMATIONS.get();
    }
}
