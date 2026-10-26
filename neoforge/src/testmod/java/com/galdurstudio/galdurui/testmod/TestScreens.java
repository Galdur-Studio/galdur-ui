package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.neoforge.GaldurNeoForgeScreens;
import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Screens of the test menus. The test chest uses {@link GaldurContainerScreen} as is, without a
 * subclass; the test terminal adds an item grid in {@link TestTerminalScreen}.
 */
public final class TestScreens {
    private TestScreens() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterMenuScreensEvent.class, TestScreens::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(TestMenus.TEST_CHEST.get(), GaldurNeoForgeScreens.factory());
        event.register(TestMenus.TEST_TERMINAL.get(), TestTerminalScreen::new);
    }
}
