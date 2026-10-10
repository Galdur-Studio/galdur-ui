package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurScrollList;
import com.galdurstudio.galdurui.api.window.ListHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Élément de fenêtre qui affiche une liste défilante.
 *
 * <p>Les lignes, la sélection et le défilement vivent dans le {@link ListHandle} : le widget les
 * relit à chaque image, et le défilement est rétabli quand l'écran est reconstruit.</p>
 *
 * @param handle      l'état de la liste, partagé avec le mod
 * @param visibleRows nombre de lignes visibles à la fois
 * @param centered    {@code true} pour centrer la liste dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record ListElement(ListHandle handle, int visibleRows, boolean centered) implements WindowElement {
    /** Largeur minimale d'une liste, en pixels, même si ses lignes sont courtes ou absentes. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurScrollList.preferredWidth(handle.getLines(), MIN_WIDTH);
        GaldurScrollList list = GaldurScrollList.of(width, visibleRows, handle::getLines,
                handle::getSelected, handle::setSelected);
        list.setScroll(handle.getScroll());
        return list.onScroll(handle::setScroll);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
