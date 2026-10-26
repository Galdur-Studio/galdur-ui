package com.galdurstudio.galdurui.api.forge;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Registration helper for {@link GaldurContainerScreen} on Forge. */
public final class GaldurForgeScreens {
    private GaldurForgeScreens() {
    }

    /**
     * Shows {@code type} in a {@link GaldurContainerScreen}, without a subclass.
     *
     * <pre>{@code
     * // client side, on the mod bus
     * modEventBus.addListener((FMLClientSetupEvent event) -> GaldurForgeScreens.register(event, MY_MENUS.CHEST.get()));
     * }</pre>
     *
     * @param <M> the menu type
     */
    public static <M extends AbstractContainerMenu> void register(FMLClientSetupEvent event, MenuType<M> type) {
        event.enqueueWork(() -> MenuScreens.<M, GaldurContainerScreen<M>>register(type, GaldurContainerScreen::new));
    }
}
