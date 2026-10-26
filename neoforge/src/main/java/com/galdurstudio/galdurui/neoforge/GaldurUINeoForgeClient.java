package com.galdurstudio.galdurui.neoforge;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge client entry point of Galdur UI: theme loading and the player settings (Mods menu).
 * Nothing in this class may be reachable from a dedicated server.
 */
@Mod(value = GaldurUI.MODID, dist = Dist.CLIENT)
public class GaldurUINeoForgeClient {
    public GaldurUINeoForgeClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener(new ThemeLoader()));
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
        GaldurConfig.register(NeoForgeConfig.STORE);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
