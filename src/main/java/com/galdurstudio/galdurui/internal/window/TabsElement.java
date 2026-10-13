package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.window.TabsHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Élément de fenêtre qui affiche une rangée d'onglets et, dessous, le contenu de l'onglet actif.
 *
 * <p>Comme une ligne ({@link RowElement}), il n'est pas construit ici mais par
 * {@link WindowScreen}, qui construit le contenu des onglets avec l'état de la rangée
 * (désactivée) et réserve la place du plus grand.</p>
 *
 * @param tabs     les titres et contenus des onglets
 * @param handle   l'état de la rangée (onglet actif, désactivée, cachée, infobulle), partagé avec le mod
 * @param centered {@code true} pour centrer la rangée dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record TabsElement(TabsModel tabs, TabsHandle handle, boolean centered) implements WindowElement {
    /**
     * Non utilisée : des onglets sont construits par {@link WindowScreen}, qui a besoin de
     * construire leur contenu avec l'état de la rangée.
     *
     * @throws UnsupportedOperationException toujours
     */
    @Override
    public LayoutElement createWidget(boolean enabled) {
        throw new UnsupportedOperationException("Des onglets sont construits par WindowScreen.");
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
