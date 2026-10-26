package com.galdurstudio.galdurui.internal.compat.jei;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import com.galdurstudio.galdurui.internal.compat.GridLookup;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

/**
 * JEI integration of Galdur UI, found by JEI through {@link JeiPlugin} and loaded only when JEI
 * is installed:
 * <ul>
 *   <li>JEI's item list avoids the {@link GaldurContainerScreen#getExclusionZones()} areas;</li>
 *   <li>hovering an item of a {@code GaldurItemGrid} and pressing R or U shows its recipes.</li>
 * </ul>
 *
 * <p>No other Galdur UI class references this one: without JEI it is never loaded.</p>
 */
@JeiPlugin
public class GaldurJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(GaldurContainerScreen.class, new IGuiContainerHandler<GaldurContainerScreen<?>>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(GaldurContainerScreen<?> screen) {
                return screen.getExclusionZones();
            }
        });
        registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
            @Override
            public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
                    IClickableIngredientFactory factory, double mouseX, double mouseY) {
                GridLookup.Hit hit = GridLookup.find(Minecraft.getInstance().screen, mouseX, mouseY);
                return hit == null ? Optional.empty() : factory.createBuilder(hit.stack()).buildWithArea(hit.area());
            }
        });
    }
}
