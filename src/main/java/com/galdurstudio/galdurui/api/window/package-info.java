/**
 * Fenêtres de Galdur UI : décrire un écran en quelques lignes, sans coordonnées ni
 * {@code Screen} à écrire.
 *
 * <p>API publique, stable dans une même version majeure. On obtient une
 * {@link com.galdurstudio.galdurui.api.window.GaldurWindow} avec
 * {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}, on y ajoute des éléments,
 * puis on l'ouvre. La mise en page, le thème et le rendu sont pris en charge par la
 * bibliothèque.</p>
 *
 * <p><strong>Règle pour nos mods</strong> : ne jamais dépendre de
 * {@code com.galdurstudio.galdurui.internal}.</p>
 */
package com.galdurstudio.galdurui.api.window;
