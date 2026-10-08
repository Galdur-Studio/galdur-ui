/**
 * Accès au thème de Galdur UI : couleurs et espacements lus dans
 * {@code assets/galdurui/theme.json}, rechargés avec les ressources du jeu.
 *
 * <p>API publique, stable dans une même version majeure. Ce package ne dépend d'aucune classe
 * vanilla, mais il fait partie d'une API dont les widgets et layouts héritent de classes de
 * Minecraft (voir la section « Compatibilité et versions » du README).</p>
 *
 * <p><strong>Règle pour nos mods</strong> : construire et dessiner les écrans uniquement avec
 * l'API Galdur ({@code com.galdurstudio.galdurui.api}), jamais en appelant {@code GuiGraphics}
 * directement, et ne jamais dépendre de {@code com.galdurstudio.galdurui.internal}.</p>
 */
package com.galdurstudio.galdurui.api.theme;
