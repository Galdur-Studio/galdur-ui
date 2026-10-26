package com.galdurstudio.galdurui.forge;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.config.GaldurSettingsScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Forge client side of Galdur UI: theme loading and the player settings. Never loaded on a server. */
final class GaldurUIForgeClient {
    private GaldurUIForgeClient() {
    }

    static void init(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(
                (RegisterClientReloadListenersEvent event) -> event.registerReloadListener(new ThemeLoader()));
        context.registerConfig(ModConfig.Type.CLIENT, ForgeConfig.SPEC);
        GaldurConfig.register(ForgeConfig.STORE);
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new GaldurSettingsScreen(parent)));
    }
}
