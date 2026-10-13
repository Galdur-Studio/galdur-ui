package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.ButtonElement;
import com.galdurstudio.galdurui.internal.window.CheckboxElement;
import com.galdurstudio.galdurui.internal.window.ListElement;
import com.galdurstudio.galdurui.internal.window.RowElement;
import com.galdurstudio.galdurui.internal.window.SearchElement;
import com.galdurstudio.galdurui.internal.window.TextElement;
import com.galdurstudio.galdurui.internal.window.TitleElement;
import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import net.minecraft.network.chat.Component;

/**
 * Ce qui reçoit des éléments empilés de haut en bas : une {@link GaldurWindow}, ou le contenu
 * d'un onglet ({@link TabHandle}).
 *
 * <p>Les éléments sont affichés dans l'ordre des appels. Un élément ajouté alors que la fenêtre
 * est affichée apparaît à l'image suivante. À appeler sur le thread du client.</p>
 *
 * <p>Les mods n'étendent pas cette classe : ils utilisent {@link GaldurWindow} et
 * {@link TabHandle}.</p>
 */
public abstract class ElementContainer {
    /** Non publique : seules {@link GaldurWindow} et {@link TabHandle} en héritent. */
    ElementContainer() {
    }

    /**
     * Ajoute un élément à la fin, branche sa poignée et prévient ce qui doit se mettre à jour.
     *
     * @return la poignée de l'élément, renvoyée telle quelle au mod
     */
    abstract <H extends ElementHandle> H add(WindowElement element, H handle);

    /**
     * Ajoute une ligne de texte à la fin, alignée à gauche ; voir
     * {@link #addText(Component, boolean)} pour la centrer.
     *
     * @param text le texte à afficher
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addText(Component text) {
        return addText(text, false);
    }

    /**
     * Ajoute une ligne de texte à la fin, centrée ou alignée à gauche.
     *
     * @param text     le texte à afficher
     * @param centered {@code true} pour la centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addText(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, centered, handle), handle);
    }

    /**
     * Ajoute un titre à la fin, aligné à gauche ; voir {@link #addTitle(Component, boolean)} pour
     * le centrer.
     *
     * <p>Un titre se distingue d'un simple texte par son style. Il se place en général en premier,
     * mais rien ne l'impose : les éléments restent dans l'ordre des appels.</p>
     *
     * @param text le titre à afficher
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addTitle(Component text) {
        return addTitle(text, false);
    }

    /**
     * Ajoute un titre à la fin, centré ou aligné à gauche.
     *
     * @param text     le titre à afficher
     * @param centered {@code true} pour le centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addTitle(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TitleElement(text, centered, handle), handle);
    }

    /**
     * Ajoute un bouton à la fin, aligné à gauche ; voir
     * {@link #addButton(Component, Runnable, boolean)} pour le centrer.
     *
     * <p>L'action est exécutée à chaque activation du bouton, à la souris ou au clavier.</p>
     *
     * @param label  le libellé du bouton
     * @param action exécutée quand on active le bouton
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addButton(Component label, Runnable action) {
        return addButton(label, action, false);
    }

    /**
     * Ajoute un bouton à la fin, centré ou aligné à gauche.
     *
     * @param label    le libellé du bouton
     * @param action   exécutée quand on active le bouton
     * @param centered {@code true} pour le centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addButton(Component label, Runnable action, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, centered, handle), handle);
    }

    /**
     * Ajoute une case à cocher à la fin, alignée à gauche, et renvoie sa poignée ; voir
     * {@link #addCheckbox(Component, boolean, boolean)} pour la centrer.
     *
     * <p>La poignée permet de lire et de changer l'état de la case, d'être prévenu des
     * changements et, comme toute {@link ElementHandle}, de la désactiver, la cacher ou lui donner
     * une infobulle.</p>
     *
     * @param label        le texte affiché à côté de la case
     * @param defaultState {@code true} si la case est cochée à l'ouverture
     * @return la poignée de la case
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState) {
        return addCheckbox(label, defaultState, false);
    }

    /**
     * Ajoute une case à cocher à la fin, centrée ou alignée à gauche.
     *
     * @param label        le texte affiché à côté de la case
     * @param defaultState {@code true} si la case est cochée à l'ouverture
     * @param centered     {@code true} pour la centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de la case
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState, boolean centered) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        return add(new CheckboxElement(label, handle, centered), handle);
    }

    /**
     * Ajoute une ligne à la fin, alignée à gauche, et renvoie sa poignée, pour y placer des
     * éléments côte à côte ; voir {@link #addRow(boolean)} pour la centrer.
     *
     * <pre>{@code
     * RowHandle actions = window.addRow();
     * actions.addButton(Component.literal("Valider"), this::valider);
     * actions.addButton(Component.literal("Annuler"), window::close);
     * }</pre>
     *
     * @return la poignée de la ligne, qui sert à y ajouter des éléments
     */
    public RowHandle addRow() {
        return addRow(false);
    }

    /**
     * Ajoute une ligne à la fin, centrée ou alignée à gauche, et renvoie sa poignée.
     *
     * @param centered {@code true} pour la centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de la ligne, qui sert à y ajouter des éléments
     */
    public RowHandle addRow(boolean centered) {
        WindowModel children = new WindowModel();
        RowHandle handle = new RowHandle(children);
        return add(new RowElement(children, centered, handle), handle);
    }

    /**
     * Ajoute une liste défilante à la fin, alignée à gauche, et renvoie sa poignée, qui porte ses
     * lignes et sa sélection ; voir {@link #addList(int, boolean)} pour la centrer.
     *
     * <pre>{@code
     * ListHandle minerais = window.addList(5);
     * minerais.addLine(Component.literal("Fer"));
     * minerais.onSelect(index -> choisir(index));
     * }</pre>
     *
     * @param visibleRows nombre de lignes visibles à la fois (au moins 1) ; fixe la hauteur
     * @return la poignée de la liste
     */
    public ListHandle addList(int visibleRows) {
        return addList(visibleRows, false);
    }

    /**
     * Ajoute une liste défilante à la fin, centrée ou alignée à gauche, et renvoie sa poignée.
     *
     * @param visibleRows nombre de lignes visibles à la fois (au moins 1) ; fixe la hauteur
     * @param centered    {@code true} pour la centrer, {@code false} pour l'aligner à gauche
     * @return la poignée de la liste
     */
    public ListHandle addList(int visibleRows, boolean centered) {
        ListHandle handle = new ListHandle();
        return add(new ListElement(handle, visibleRows, centered), handle);
    }

    /**
     * Ajoute un champ de recherche à la fin, aligné à gauche, et renvoie sa poignée, qui porte le
     * texte saisi ; voir {@link #addSearch(Component, boolean)} pour le centrer.
     *
     * <pre>{@code
     * SearchHandle recherche = window.addSearch(Component.literal("Rechercher..."));
     * recherche.onChange(texte -> filtrer(texte));
     * }</pre>
     *
     * @param placeholder texte d'indication, affiché tant que le champ est vide
     * @return la poignée du champ
     */
    public SearchHandle addSearch(Component placeholder) {
        return addSearch(placeholder, false);
    }

    /**
     * Ajoute un champ de recherche à la fin, centré ou aligné à gauche, et renvoie sa poignée.
     *
     * @param placeholder texte d'indication, affiché tant que le champ est vide
     * @param centered    {@code true} pour le centrer, {@code false} pour l'aligner à gauche
     * @return la poignée du champ
     */
    public SearchHandle addSearch(Component placeholder, boolean centered) {
        SearchHandle handle = new SearchHandle();
        return add(new SearchElement(placeholder, handle, centered), handle);
    }
}
