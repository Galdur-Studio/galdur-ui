package com.galdurstudio.galdurui.internal.window;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenu d'une fenêtre : la liste ordonnée des {@link WindowElement} ajoutés par le mod.
 *
 * <p>{@link com.galdurstudio.galdurui.api.window.GaldurWindow} le garde en champ privé et le
 * transmet à {@link WindowScreen} à l'ouverture. Cela permet à l'écran de lire le contenu sans que
 * {@code GaldurWindow} n'ait à exposer de getter public.</p>
 */
public class WindowModel {
    private final List<WindowElement> elements = new ArrayList<>();

    /**
     * Ajoute un élément à la fin de la fenêtre.
     *
     * @param element l'élément à ajouter
     */
    public void add(WindowElement element) {
        elements.add(element);
    }

    /**
     * Donne les éléments dans l'ordre d'ajout, de haut en bas à l'affichage.
     *
     * @return une copie non modifiable de la liste ; la modifier ne change pas la fenêtre
     */
    public List<WindowElement> elements() {
        return List.copyOf(elements);
    }
}
