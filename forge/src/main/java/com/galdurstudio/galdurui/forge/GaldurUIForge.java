package com.galdurstudio.galdurui.forge;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entry point present on both sides. The library itself is client only: everything is
 * hooked up in {@link GaldurUIForgeClient}, which a dedicated server never loads.
 */
@Mod(GaldurUI.MODID)
public class GaldurUIForge {
    public GaldurUIForge(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist.isClient()) {
            GaldurUIForgeClient.init(context);
        }
    }
}
