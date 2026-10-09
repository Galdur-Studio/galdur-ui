package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche un titre, pour l'instant en gras.
 *
 * <p>Le texte reçu n'est pas modifié : le gras est appliqué sur une copie, pour ne pas changer le
 * {@code Component} du mod qui nous l'a donné.</p>
 *
 * @param title    le titre à afficher
 * @param centered {@code true} pour centrer le titre dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record TitleElement(Component title, boolean centered) implements WindowElement {
    @Override
    public LayoutElement createWidget() {
        return GaldurLabel.of(title.copy().withStyle(ChatFormatting.BOLD));
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}