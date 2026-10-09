package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche une ligne de texte.
 *
 * @param text le texte à afficher
 */
public record TextElement(Component text) implements WindowElement {
    @Override
    public LayoutElement createWidget() {
        return GaldurLabel.of(text);
    }
}
