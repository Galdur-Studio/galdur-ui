package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;

/**
 * Poignée vers un onglet d'une rangée d'onglets ({@link TabsHandle}) : son contenu, rempli comme
 * une fenêtre avec les méthodes {@code add...(...)} héritées de {@link ElementContainer}.
 *
 * <pre>{@code
 * TabHandle stockage = onglets.addTab(Component.literal("Stockage"));
 * SearchHandle recherche = stockage.addSearch(Component.literal("Rechercher..."));
 * ListHandle objets = stockage.addList(6);
 * }</pre>
 *
 * <p>Le contenu n'est affiché que lorsque l'onglet est actif, mais ses poignées restent
 * utilisables à tout moment : un changement dans un onglet caché est visible quand on y revient.
 * Un onglet ne contient pas d'autre rangée d'onglets.</p>
 *
 * <p>À appeler sur le thread du client.</p>
 */
public class TabHandle extends ElementContainer {
    private final TabsHandle owner;
    private final WindowModel content;
    private final int index;

    /**
     * Non publique : seule {@link TabsHandle} crée des onglets.
     *
     * @param owner   la rangée d'onglets, prévenue de chaque changement du contenu
     * @param content les éléments de l'onglet, partagés avec l'élément affiché
     * @param index   la position de l'onglet dans la rangée
     */
    TabHandle(TabsHandle owner, WindowModel content, int index) {
        this.owner = owner;
        this.content = content;
        this.index = index;
    }

    /**
     * Ajoute un élément au contenu et branche sa poignée sur la rangée d'onglets : un changement
     * de l'élément remonte ainsi jusqu'à la fenêtre.
     *
     * @return la poignée de l'élément, renvoyée telle quelle au mod
     */
    @Override
    <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(owner::changed);
        content.add(element);
        owner.changed();
        return handle;
    }

    /** {@return la position de l'onglet dans sa rangée, à partir de 0} */
    public int getIndex() {
        return index;
    }

    /** {@return {@code true} si cet onglet est l'onglet actif} */
    public boolean isSelected() {
        return owner.getSelected() == index;
    }

    /** Rend cet onglet actif ; équivaut à {@code setSelected(getIndex())} sur la rangée. */
    public void select() {
        owner.setSelected(index);
    }
}
