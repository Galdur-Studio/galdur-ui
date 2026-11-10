package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Window element showing a clickable button.
 *
 * <p>The widget expects an action that receives the button; the public API takes a plain
 * {@link Runnable}. {@link #createWidget(boolean)} bridges the two.</p>
 *
 * @param label    the button label
 * @param action   run every time the button is activated, by mouse or keyboard
 * @param centered {@code true} to centre the button in the window, {@code false} to align it left
 * @param handle   the common state (disabled, hidden, tooltip), shared with the mod
 */
public record ButtonElement(Component label, Runnable action, boolean centered, ElementHandle handle) implements WindowElement  {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        return GaldurButton.of(label, button -> action.run());
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if  (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
