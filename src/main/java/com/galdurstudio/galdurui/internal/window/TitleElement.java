package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.text.GaldurFonts;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche un titre, dans la police des titres ({@link GaldurFonts#TITLE}).
 *
 * <p>Le texte reçu n'est pas modifié : la police est appliquée sur une copie, pour ne pas changer le
 * {@code Component} du mod qui nous l'a donné.</p>
 *
 * @param title    le titre à afficher
 * @param centered {@code true} pour centrer le titre dans la fenêtre, {@code false} pour l'aligner à gauche
 * @param handle   l'état commun (désactivé, caché, infobulle), partagé avec le mod
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