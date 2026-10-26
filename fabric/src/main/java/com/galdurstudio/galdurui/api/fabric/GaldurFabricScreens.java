package com.galdurstudio.galdurui.api.fabric;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Registration helper for {@link GaldurContainerScreen} on Fabric. */
public final class GaldurFabricScreens {
    private GaldurFabricScreens() {
    }

    /**
     * Shows {@code type} in a {@link GaldurContainerScreen}, without a subclass.
     *
     * <pre>{@code
     * // client side, in ClientModInitializer
     * GaldurFabricScreens.register(MY_MENUS.CHEST);
     * }</pre>
     *
     * @param <M> the menu type
     */
    public static <M extends AbstractContainerMenu> void register(MenuType<M> type) {
        MenuScreens.<M, GaldurContainerScreen<M>>register(type, GaldurContainerScreen::new);
    }
}
