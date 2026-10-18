package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.window.TabsHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Window element showing a tab row and, below it, the content of the active tab.
 *
 * <p>Like a row ({@link RowElement}), it is not built here but by {@link WindowScreen}, which
 * builds the tab contents with the row state (disabled) and reserves the space of the largest.</p>
 *
 * @param tabs     the tab titles and contents
 * @param handle   the row state (active tab, disabled, hidden, tooltip), shared with the mod
 * @param centered {@code true} to centre the row in the window, {@code false} to align it left
 */
public record TabsElement(TabsModel tabs, TabsHandle handle, boolean centered) implements WindowElement {
    /**
     * Unused: tabs are built by {@link WindowScreen}, which needs to build their content with the
     * row state.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public LayoutElement createWidget(boolean enabled) {
        throw new UnsupportedOperationException("Tabs are built by WindowScreen.");
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
