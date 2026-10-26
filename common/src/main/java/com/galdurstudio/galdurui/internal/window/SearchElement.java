package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.api.window.SearchHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Window element showing a search field.
 *
 * <p>The text lives in the {@link SearchHandle}: the widget reads it every frame and passes it
 * every keystroke.</p>
 *
 * @param placeholder hint text, shown while the field is empty
 * @param handle      the field state, shared with the mod
 * @param centered    {@code true} to centre the field in the window, {@code false} to align it left
 */
public record SearchElement(Component placeholder, SearchHandle handle, boolean centered) implements WindowElement {
    /** Minimum field width, in pixels, even with a short placeholder. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurSearchField.preferredWidth(placeholder, MIN_WIDTH);
        return GaldurSearchField.of(width, placeholder, handle::getText, handle::setText);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
