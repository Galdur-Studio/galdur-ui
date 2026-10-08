/**
 * Briques de layout de Galdur UI, à combiner avec les layouts vanilla
 * ({@code LinearLayout}, {@code FrameLayout}, {@code GridLayout}).
 *
 * <p>API publique, stable dans une même version majeure. Ces classes héritent de classes
 * vanilla ({@code AbstractLayout}, {@code Renderable}) : un changement de ces classes chez Mojang
 * peut imposer une version majeure de Galdur UI (voir la section « Compatibilité et versions »
 * du README).</p>
 *
 * <p><strong>Règle pour nos mods</strong> : construire et dessiner les écrans uniquement avec
 * l'API Galdur ({@code com.galdurstudio.galdurui.api}), jamais en appelant {@code GuiGraphics}
 * directement, et ne jamais dépendre de {@code com.galdurstudio.galdurui.internal}.</p>
 */
package com.galdurstudio.galdurui.api.layout;
