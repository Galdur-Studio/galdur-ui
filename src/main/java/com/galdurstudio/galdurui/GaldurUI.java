package com.galdurstudio.galdurui;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constants shared by every loader: the mod id, the logger and resource ids. The library itself is client
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

    /**
     * @param path path in the {@code galdurui} namespace
     * @return the resource id {@code galdurui:<path>}
     */
    public static ResourceLocation id(String path) {
        return id(MODID, path);
    }

    /**
     * Builds a resource id on every Minecraft version ({@code ResourceLocation.fromNamespaceAndPath}
     * only exists since 1.21).
     *
     * @param namespace namespace, usually a mod id
     * @param path      path in that namespace
     * @return the resource id {@code <namespace>:<path>}
     */
    public static ResourceLocation id(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }
}
