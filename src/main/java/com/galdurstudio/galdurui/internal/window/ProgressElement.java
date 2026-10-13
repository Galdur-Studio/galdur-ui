package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurProgressBar;
import com.galdurstudio.galdurui.api.window.ProgressHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Élément de fenêtre qui affiche une barre de progression.
 *
 * <p>L'avancement et le texte vivent dans le {@link ProgressHandle} : le widget les relit à
 * chaque image.</p>
 *
 * @param handle   l'état de la barre, partagé avec le mod
 * @param centered {@code true} pour centrer la barre, {@code false} pour l'aligner à gauche
 */
public record ProgressElement(ProgressHandle handle, boolean centered) implements WindowElement {
    /** Largeur minimale d'une barre, en pixels, même sans texte ou avec un texte court. */
    private static final int MIN_WIDTH = 100;

    @Override
    public LayoutElement createWidget(boolean enabled) {
        int width = GaldurProgressBar.preferredWidth(handle.getLabel(), MIN_WIDTH);
        return GaldurProgressBar.of(width, handle::getProgress, handle::getLabel);
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
