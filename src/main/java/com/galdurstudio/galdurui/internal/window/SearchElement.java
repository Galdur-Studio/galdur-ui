package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.api.window.SearchHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche un champ de recherche.
 *
 * <p>Le texte vit dans le {@link SearchHandle} : le widget le relit à chaque image et lui
 * transmet chaque frappe du joueur.</p>
 *
 * @param placeholder texte d'indication, affiché tant que le champ est vide
 * @param handle      l'état du champ, partagé avec le mod
 * @param centered    {@code true} pour centrer le champ dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record SearchElement(Component placeholder, SearchHandle handle, boolean centered) implements WindowElement {
    /** Largeur minimale d'un champ, en pixels, même si son texte d'indication est court. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurSearchField.preferredWidth(placeholder, MIN_WIDTH);
        return GaldurSearchField.of(width, placeholder, handle::getText, handle::setText);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
