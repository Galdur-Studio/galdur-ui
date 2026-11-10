package com.galdurstudio.galdurui.api.neoforge;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Registration helper for {@link GaldurContainerScreen} on NeoForge. */
public final class GaldurNeoForgeScreens {
    private GaldurNeoForgeScreens() {
    }

    /**
     * {@return a screen constructor for {@code RegisterMenuScreensEvent.register}, without a
     * subclass}; a plain {@code GaldurContainerScreen::new} does not work there because Java cannot
     * infer the screen type
     *
     * <pre>{@code
     * // client side, on the mod bus
     * event.register(MY_MENUS.CHEST.get(), GaldurNeoForgeScreens.factory());
     * }</pre>
     *
     * @param <M> the menu type
     */
    public static <M extends AbstractContainerMenu> MenuScreens.ScreenConstructor<M, GaldurContainerScreen<M>> factory() {
        return GaldurContainerScreen::new;
    }
}
