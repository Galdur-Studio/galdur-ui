package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurCheckbox;
import com.galdurstudio.galdurui.api.window.CheckboxHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Window element showing a checkbox.
 *
 * <p>The state is not stored here (the record is immutable and widgets are rebuilt on every
 * {@code init()}) but in the {@link CheckboxHandle}, which the widget reads and updates.</p>
 *
 * <p>The widget is a {@link GaldurCheckbox}: it reads the state from the handle every frame and
 * passes it the changes the player asks for.</p>
 *
 * @param label    the text next to the box
 * @param handle   the handle holding the state, shared with the mod
 * @param centered {@code true} to centre the checkbox in the window, {@code false} to align it left
 */
public record CheckboxElement(Component label, CheckboxHandle handle, boolean centered) implements WindowElement {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        return GaldurCheckbox.of(label, handle::isChecked, handle::setChecked);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
