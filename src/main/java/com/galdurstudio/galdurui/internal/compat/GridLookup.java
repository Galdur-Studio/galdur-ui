package com.galdurstudio.galdurui.internal.compat;

import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the hovered item in the {@link GaldurItemGrid}s of a screen, shared by the JEI and EMI
 * integrations. Depends on neither, so it loads without them.
 */
public final class GridLookup {
    /**
     * An item found under the mouse.
     *
     * @param stack the item
     * @param area  its cell bounds, in screen coordinates
     */
    public record Hit(ItemStack stack, Rect2i area) {
    }

    private GridLookup() {
    }

    /**
     * {@return the item of a visible grid under this point, or {@code null}}
     *
     * @param screen the shown screen
     * @param mouseX mouse x
     * @param mouseY mouse y
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
