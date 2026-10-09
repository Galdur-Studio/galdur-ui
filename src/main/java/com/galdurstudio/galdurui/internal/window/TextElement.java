package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche une ligne de texte.
 *
 * @param text     le texte à afficher
 * @param centered {@code true} pour centrer le texte dans la fenêtre, {@code false} pour l'aligner à gauche
 * @param handle   l'état commun (désactivé, caché, infobulle), partagé avec le mod
 */
public record TextElement(Component text, boolean centered, ElementHandle handle) implements WindowElement {
    @Override
    public LayoutElement createWidget() {
        return handle.isEnabled() ? GaldurLabel.of(text) : GaldurLabel.muted(text);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
