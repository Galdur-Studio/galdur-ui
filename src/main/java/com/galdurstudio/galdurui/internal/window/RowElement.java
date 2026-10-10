package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.window.RowHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Élément de fenêtre qui place ses enfants côte à côte, de gauche à droite.
 *
 * <p>Ses enfants ne sont pas construits ici mais par {@link WindowScreen}, qui leur transmet
 * l'état de la ligne (désactivée, infobulle) et saute ceux qui sont cachés.</p>
 *
 * @param children les éléments de la ligne, dans l'ordre d'ajout
 * @param centered {@code true} pour centrer la ligne dans la fenêtre, {@code false} pour l'aligner à gauche
 * @param handle   l'état commun de la ligne, partagé avec le mod
 */
public record RowElement(WindowModel children, boolean centered, RowHandle handle) implements WindowElement {
    /**
     * Non utilisée : une ligne est construite par {@link WindowScreen}, qui a besoin de
     * construire ses enfants avec l'état de la ligne.
     *
     * @throws UnsupportedOperationException toujours
     */
    @Override
    public LayoutElement createWidget(boolean enabled) {
        throw new UnsupportedOperationException("Une ligne est construite par WindowScreen.");
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
