package com.galdurstudio.galdurui.internal.window;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Description d'un élément de fenêtre (texte, bouton, case à cocher...).
 *
 * <p>Un élément ne contient <strong>pas</strong> de widget : il décrit seulement ce qu'il faut
 * afficher. Minecraft rappelle {@code Screen.init()} à chaque redimensionnement de la fenêtre du
 * jeu et les widgets sont alors recréés. {@link WindowScreen} reconstruit donc les widgets à
 * partir de ces descriptions, qui, elles, survivent.</p>
 *
 * <p>Interne à Galdur UI : les mods n'utilisent pas ce type, ils passent par
 * {@link com.galdurstudio.galdurui.api.window.GaldurWindow}.</p>
 */
public interface WindowElement {
    /**
     * Crée un widget neuf à partir de cette description.
     *
     * <p>Appelée à chaque {@code init()} de l'écran : elle doit renvoyer une nouvelle instance
     * à chaque appel, sans réutiliser un widget déjà donné à l'écran.</p>
     *
     * @return le widget prêt à être placé dans une mise en page
     */
    LayoutElement createWidget();

    /**
     * Règle la façon dont l'élément est placé dans la mise en page (alignement, marges...).
     *
     * <p>{@link WindowScreen} appelle cette méthode pour chaque élément, au moment de l'ajouter
     * au layout, en lui donnant ses réglages. Par défaut elle n'en change aucun : l'élément garde
     * les réglages de Minecraft. Un élément ne redéfinit que les réglages qui le concernent.</p>
     *
     * @param settings les réglages de cet élément dans le layout, à modifier directement
     */
    default void adjustSettings(LayoutSettings settings) {}
}
