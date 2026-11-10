package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.window.RowHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Window element that places its children side by side, left to right.
 *
 * <p>Its children are not built here but by {@link WindowScreen}, which passes them the row state
 * (disabled, tooltip) and skips the hidden ones.</p>
 *
 * @param children the row's elements, in insertion order
 * @param centered {@code true} to centre the row in the window, {@code false} to align it left
 * @param handle   the row's common state, shared with the mod
 */
public record RowElement(WindowModel children, boolean centered, RowHandle handle) implements WindowElement {
    /**
     * Unused: a row is built by {@link WindowScreen}, which needs to build its children with the
     * row state.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public LayoutElement createWidget(boolean enabled) {
        throw new UnsupportedOperationException("A row is built by WindowScreen.");
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
