package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurScrollList;
import com.galdurstudio.galdurui.api.window.ListHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Window element showing a scrolling list.
 *
 * <p>Lines, selection and scroll offset live in the {@link ListHandle}: the widget reads them
 * every frame, and the scroll offset is restored when the screen is rebuilt.</p>
 *
 * @param handle      the list state, shared with the mod
 * @param visibleRows number of lines visible at once
 * @param centered    {@code true} to centre the list in the window, {@code false} to align it left
 */
public record ListElement(ListHandle handle, int visibleRows, boolean centered) implements WindowElement {
    /** Minimum list width, in pixels, even with short or no lines. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurScrollList.preferredWidth(handle.getLines(), MIN_WIDTH);
        GaldurScrollList list = GaldurScrollList.of(width, visibleRows, handle::getLines,
                handle::getSelected, handle::setSelected);
        list.setScroll(handle.getScroll());
        return list.onScroll(handle::setScroll);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
