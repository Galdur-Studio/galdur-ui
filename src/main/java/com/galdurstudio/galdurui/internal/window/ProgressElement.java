package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurProgressBar;
import com.galdurstudio.galdurui.api.window.ProgressHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Window element showing a progress bar.
 *
 * <p>Progress and text live in the {@link ProgressHandle}: the widget reads them every frame.</p>
 *
 * @param handle   the bar state, shared with the mod
 * @param centered {@code true} to centre the bar, {@code false} to align it left
 */
public record ProgressElement(ProgressHandle handle, boolean centered) implements WindowElement {
    /** Minimum bar width, in pixels, even without text or with a short one. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurProgressBar.preferredWidth(handle.getLabel(), MIN_WIDTH);
        return GaldurProgressBar.of(width, handle::getProgress, handle::getLabel);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
