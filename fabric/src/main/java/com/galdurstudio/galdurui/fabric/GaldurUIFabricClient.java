package com.galdurstudio.galdurui.fabric;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.config.JsonConfigStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;

/**
 * Fabric client entry point of Galdur UI: theme loading and the player settings. Declared in
 * {@code fabric.mod.json} as a {@code client} entrypoint, so a dedicated server never loads it.
 */
public final class GaldurUIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GaldurConfig.register(new JsonConfigStore(
                FabricLoader.getInstance().getConfigDir().resolve("galdurui-client.json")));
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricThemeReloadListener());
    }
}
