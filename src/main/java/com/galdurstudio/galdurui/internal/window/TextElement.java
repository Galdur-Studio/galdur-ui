package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Window element showing a line of text.
 *
 * @param text     the text to show
 * @param centered {@code true} to centre the text in the window, {@code false} to align it left
 * @param handle   the common state (disabled, hidden, tooltip), shared with the mod
 */
public record TextElement(Component text, boolean centered, ElementHandle handle) implements WindowElement {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        return enabled ? GaldurLabel.of(text) : GaldurLabel.muted(text);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
