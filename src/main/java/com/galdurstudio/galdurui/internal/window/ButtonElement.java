package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche un bouton cliquable.
 *
 * <p>Le widget attend une action qui reçoit le bouton ; l'API publique donne un simple
 * {@link Runnable}. {@link #createWidget(boolean)} fait le pont entre les deux.</p>
 *
 * @param label     le libellé du bouton
 * @param action   exécutée à chaque activation du bouton, à la souris ou au clavier
 * @param centered {@code true} pour centrer le bouton dans la fenêtre, {@code false} pour l'aligner à gauche
 * @param handle   l'état commun (désactivé, caché, infobulle), partagé avec le mod
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
