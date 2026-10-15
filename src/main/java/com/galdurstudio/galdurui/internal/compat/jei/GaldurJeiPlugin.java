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
 * Intégration JEI de Galdur UI, découverte par JEI grâce à {@link JeiPlugin} et chargée seulement
 * s'il est installé :
 * <ul>
 *   <li>la liste d'objets de JEI évite les zones de
 *       {@link GaldurContainerScreen#getExclusionZones()} ;</li>
 *   <li>survoler un objet d'une {@code GaldurItemGrid} puis appuyer sur R ou U montre ses
 *       recettes.</li>
 * </ul>
 *
 * <p>Aucune autre classe de Galdur UI ne référence celle-ci : sans JEI, elle n'est jamais
 * chargée.</p>
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
