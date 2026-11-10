package com.galdurstudio.galdurui.neoforge;

import com.galdurstudio.galdurui.internal.bridge.BridgeCheck;
import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.config.GaldurSettingsScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
//? if >=26.1 {
/*import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
*///?} else {
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
//?}
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge client entry point of Galdur UI: theme loading and the player settings (Mods menu).
 * Nothing in this class may be reachable from a dedicated server.
 */
@Mod(value = GaldurUI.MODID, dist = Dist.CLIENT)
public class GaldurUINeoForgeClient {
    public GaldurUINeoForgeClient(IEventBus modEventBus, ModContainer container) {
        //? if >=26.1 {
        /*modEventBus.addListener(AddClientReloadListenersEvent.class,
                event -> event.addListener(GaldurUI.id("theme"), new ThemeLoader()));
        *///?} else {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener(new ThemeLoader()));
        //?}
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
        GaldurConfig.register(NeoForgeConfig.STORE);
        BridgeCheck.runIfAsked();
        // The Galdur UI settings screen, as on the other loaders: it also opens the theme editor.
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new GaldurSettingsScreen(parent));
    }
}
