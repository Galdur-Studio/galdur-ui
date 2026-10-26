package com.galdurstudio.galdurui.fabric;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Gives {@link ThemeLoader} the identifier Fabric API needs to register a reload listener. */
final class FabricThemeReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "theme");

    private final ThemeLoader loader = new ThemeLoader();

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        loader.onResourceManagerReload(resourceManager);
    }
}
