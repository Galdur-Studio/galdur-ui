package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche une ligne de texte.
 *
 * @param text     le texte à afficher
 * @param centered {@code true} pour centrer le texte dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record TextElement(Component text, boolean centered) implements WindowElement {
    @Override
    public LayoutElement createWidget() {
        return GaldurLabel.of(text);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
