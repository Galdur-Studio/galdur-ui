package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.text.GaldurFonts;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Window element showing a title, in the title font ({@link GaldurFonts#TITLE}).
 *
 * <p>The given text is not modified: the font is applied to a copy, so the mod's
 * {@code Component} stays unchanged.</p>
 *
 * @param title    the title to show
 * @param centered {@code true} to centre the title in the window, {@code false} to align it left
 * @param handle   the common state (disabled, hidden, tooltip), shared with the mod
 */
public record TitleElement(Component title, boolean centered, ElementHandle handle) implements WindowElement {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        Component styled = GaldurFonts.title(title);
        return enabled ? GaldurLabel.of(styled) : GaldurLabel.muted(styled);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
