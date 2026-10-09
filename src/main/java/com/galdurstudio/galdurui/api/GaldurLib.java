package com.galdurstudio.galdurui.api;

import com.galdurstudio.galdurui.api.window.GaldurWindow;

/**
 * Point d'entrée de Galdur UI : la classe à connaître pour créer des écrans.
 *
 * <p>Ne sert qu'à fabriquer les objets de la bibliothèque ; elle ne se construit pas.</p>
 *
 * <pre>{@code
 * GaldurWindow window = GaldurLib.createWindow();
 * window.open();
 * }</pre>
 */
public final class GaldurLib {
    private GaldurLib() {}

    /**
     * Crée une fenêtre vide, prête à recevoir des éléments.
     *
     * @return une nouvelle fenêtre, fermée
     */
    public static GaldurWindow createWindow() {
        return new GaldurWindow();
    }
}
