package com.galdurstudio.galdurui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constants shared by every loader: the mod id and the logger. The library itself is client
 * only; each loader module has its own entry point that hooks up the theme loading and the player
 * settings ({@code GaldurUINeoForge}, {@code GaldurUIFabric}...). {@code api.menu} is the only
 * package that may be loaded on a dedicated server.
 */
public final class GaldurUI {
    /** Mod id, as declared in the metadata of every loader ({@code neoforge.mods.toml}, {@code fabric.mod.json}...). */
    public static final String MODID = "galdurui";

    public static final Logger LOGGER = LoggerFactory.getLogger("GaldurUI");

    private GaldurUI() {
    }
}
