package com.galdurstudio.galdurui.testmod.neoforge;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.testmod.BridgeDemoScreen;
import com.galdurstudio.galdurui.testmod.MediaSession;
import com.galdurstudio.galdurui.testmod.ScreenshotTour;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client entry point of the test mod: commands, debug overlays, menu screens and the test resource pack. */
@Mod(value = GaldurUITest.MODID, dist = Dist.CLIENT)
public class GaldurUITestClient {
    public GaldurUITestClient(IEventBus modEventBus) {
        TestCommands.register();
        LayoutDebug.register();
        FpsOverlay.register();
        TestScreens.register(modEventBus);
        TestPacks.register(modEventBus);
        ScreenshotTour.MENU_COMMANDS.add("galduruidev terminal");
        ScreenshotTour.MENU_COMMANDS.add("galduruidev vanillachest");
        BridgeDemoScreen.register();
        // A vanilla chest with the Galdur panel in place of its texture (GaldurBridge, REPLACE).
        GaldurBridge.restyle(ContainerScreen.class, GaldurBridge.Background.REPLACE);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            ScreenshotTour.tick(Minecraft.getInstance());
            MediaSession.tick(Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Post.class, event -> MediaSession.frame(Minecraft.getInstance()));
    }
}
