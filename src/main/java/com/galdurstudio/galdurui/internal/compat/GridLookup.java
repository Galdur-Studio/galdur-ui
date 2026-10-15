package com.galdurstudio.galdurui.internal.compat;

import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Recherche de l'objet survolé dans les {@link GaldurItemGrid} d'un écran, partagée par les
 * intégrations JEI et EMI. Ne dépend d'aucun des deux : chargeable même sans eux.
 */
public final class GridLookup {
    /**
     * Un objet trouvé sous la souris.
     *
     * @param stack l'objet
     * @param area  le cadre de sa case, en coordonnées d'écran
     */
    public record Hit(ItemStack stack, Rect2i area) {
    }

    private GridLookup() {
    }

    /**
     * {@return l'objet d'une grille visible sous ce point, ou {@code null}}
     *
     * @param screen l'écran affiché
     * @param mouseX abscisse de la souris
     * @param mouseY ordonnée de la souris
     */
    @Nullable
    public static Hit find(@Nullable Screen screen, double mouseX, double mouseY) {
        if (screen == null) {
            return null;
        }
        for (GuiEventListener child : screen.children()) {
            if (child instanceof GaldurItemGrid grid && grid.visible && grid.isMouseOver(mouseX, mouseY)) {
                GaldurItemGrid.Entry entry = grid.getEntryAt(mouseX, mouseY);
                Rect2i area = grid.getCellAreaAt(mouseX, mouseY);
                if (entry != null && area != null && !entry.stack().isEmpty()) {
                    return new Hit(entry.stack(), area);
                }
            }
        }
        return null;
    }
}
