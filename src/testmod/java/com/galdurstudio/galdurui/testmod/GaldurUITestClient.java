package com.galdurstudio.galdurui.testmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Client entry point of the test mod: commands, debug overlays, menu screens and the test resource pack. */
@Mod(value = GaldurUITest.MODID, dist = Dist.CLIENT)
public class GaldurUITestClient {
    public GaldurUITestClient(IEventBus modEventBus) {
        TestCommands.register();
        LayoutDebug.register();
        FpsOverlay.register();
        TestScreens.register(modEventBus);
        TestPacks.register(modEventBus);
    }
}
