/**
 * Widgets de Galdur UI : boutons, libellés, cases à cocher, listes défilantes, champs de
 * recherche, onglets, barres de progression et cases d'objet aux couleurs de la charte.
 *
 * <p>API publique, stable dans une même version majeure. Ces classes héritent de classes
 * vanilla ({@code AbstractWidget}) pour s'intégrer aux écrans et aux layouts
 * du jeu : un changement de ces classes chez Mojang peut imposer une version majeure de
 * Galdur UI (voir la section « Compatibilité et versions » du README).</p>
 *
 * <p><strong>Règle pour nos mods</strong> : construire et dessiner les écrans uniquement avec
 * l'API Galdur ({@code com.galdurstudio.galdurui.api}), jamais en appelant {@code GuiGraphics}
 * directement, et ne jamais dépendre de {@code com.galdurstudio.galdurui.internal}.</p>
 */
package com.galdurstudio.galdurui.api.widget;
