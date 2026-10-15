package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Écrans des menus de développement (client uniquement) : le coffre de test s'affiche dans un
 * {@link GaldurContainerScreen} tel quel, sans sous-classe.
 */
public final class DevScreens {
    private DevScreens() {
    }

    /**
     * Branche l'enregistrement des écrans sur le bus du mod.
     *
     * @param modEventBus le bus du mod
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterMenuScreensEvent.class, DevScreens::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(DevMenus.TEST_CHEST.get(), GaldurContainerScreen.factory());
    }
}
