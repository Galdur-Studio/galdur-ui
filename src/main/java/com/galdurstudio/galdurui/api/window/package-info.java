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
 * <p>Chaque {@code add...(...)} renvoie une poignée qui garde l'état de l'élément, avant comme
 * après l'ouverture : {@link com.galdurstudio.galdurui.api.window.ElementHandle} pour le
 * désactiver, le cacher ou lui donner une infobulle, et
 * {@link com.galdurstudio.galdurui.api.window.CheckboxHandle} qui y ajoute l'état coché.
 * {@code addRow()} renvoie une {@link com.galdurstudio.galdurui.api.window.RowHandle}, qui place
 * ses propres éléments côte à côte, et {@code addList(...)} une
 * {@link com.galdurstudio.galdurui.api.window.ListHandle}, qui porte les lignes et la sélection
 * d'une liste défilante. {@code addSearch(...)} renvoie une
 * {@link com.galdurstudio.galdurui.api.window.SearchHandle}, qui porte le texte d'un champ de
 * recherche.</p>
 *
 * <p><strong>Règle pour nos mods</strong> : ne jamais dépendre de
 * {@code com.galdurstudio.galdurui.internal}.</p>
 */
package com.galdurstudio.galdurui.api.window;
