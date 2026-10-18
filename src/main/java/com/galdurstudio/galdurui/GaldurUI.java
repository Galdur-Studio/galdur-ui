package com.galdurstudio.galdurui;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point of Galdur UI: the mod id and the shared logger. The library itself is client
 * only; rendering is hooked up in {@link GaldurUIClient}. {@code api.menu} is the only package
 * that may be loaded on a dedicated server.
 */
@Mod(GaldurUI.MODID)
public class GaldurUI {
    /** Mod id, as declared in {@code neoforge.mods.toml}. */
    public static final String MODID = "galdurui";

    public static final Logger LOGGER = LoggerFactory.getLogger("GaldurUI");
}
