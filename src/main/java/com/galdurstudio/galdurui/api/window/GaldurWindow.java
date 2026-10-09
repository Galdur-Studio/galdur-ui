package com.galdurstudio.galdurui.api.window;

/**
 * Un écran décrit par le code d'un mod : titre, textes, boutons, cases à cocher.
 *
 * <p>On la crée avec {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}. Elle ne
 * contient que la description de l'écran ; la mise en page, le thème et le rendu sont
 * assurés par Galdur UI, et aucune coordonnée n'est jamais à calculer.</p>
 *
 * <p><strong>Squelette</strong> : {@link #open()} et {@link #close()} ne font encore rien ;
 * leur comportement et les éléments arrivent aux étapes suivantes.</p>
 */
public class GaldurWindow {
    /** Crée une fenêtre vide. Préférer {@code GaldurLib.createWindow()}. */
    public GaldurWindow() {}

    /** Affiche la fenêtre à l'écran. Sans effet si elle est déjà ouverte. */
    public void open() {}

    /** Ferme la fenêtre et revient à l'écran précédent. Sans effet si elle est fermée. */
    public void close() {}
}
