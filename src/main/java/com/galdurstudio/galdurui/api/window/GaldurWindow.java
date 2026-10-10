package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Un écran décrit par le code d'un mod : titre, textes, boutons, cases à cocher.
 *
 * <p>On la crée avec {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}. Elle ne
 * contient que la description de l'écran ; la mise en page, le thème et le rendu sont
 * assurés par Galdur UI, et aucune coordonnée n'est jamais à calculer.</p>
 */
public class GaldurWindow {
    private final WindowModel model = new WindowModel();

    /** Écran affiché en ce moment, ou {@code null} si la fenêtre n'est pas à l'écran. */
    private WindowScreen screen;

    /**
     * Écran créé par {@link #open()} mais pas encore affiché : {@code open()} passe par
     * {@code Minecraft.tell}, qui ne s'exécute qu'à la prochaine boucle du client.
     */
    private WindowScreen pending;

    /** Vrai quand une reconstruction de l'écran est déjà prévue (voir {@link #requestRebuild()}). */
    private boolean rebuildScheduled;

    /**
     * Crée une fenêtre vide. Préférer {@code GaldurLib.createWindow()}.
     */
    public GaldurWindow() {
    }

    /**
     * Affiche la fenêtre à l'écran. Sans effet si elle est déjà ouverte ou en cours d'ouverture.
     *
     * <p>L'écran affiché jusque-là est retenu : {@link #close()} et Échap y reviennent.</p>
     */
    public void open() {
        if (screen != null || pending != null) {
            return;
        }
        WindowScreen created = new WindowScreen(model, this::shown, this::hidden);
        pending = created;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.tell(() -> {
            if (pending != created) {
                return;
            }
            pending = null;
            created.setPrevious(minecraft.screen);
            minecraft.setScreen(created);
        });
    }

    /**
     * Ferme la fenêtre et revient à l'écran précédent, exactement comme Échap. Sans effet si elle
     * est fermée.
     */
    public void close() {
        if (pending != null) {
            pending = null;
            return;
        }
        WindowScreen current = screen;
        if (current != null && Minecraft.getInstance().screen == current) {
            current.onClose();
        }
    }

    /** {@return {@code true} si la fenêtre est à l'écran ou en cours d'ouverture} */
    public boolean isOpen() {
        return screen != null || pending != null;
    }

    /** L'écran devient l'écran courant (ouverture, ou retour depuis un écran ouvert par-dessus). */
    private void shown(WindowScreen shownScreen) {
        screen = shownScreen;
    }

    /** L'écran n'est plus l'écran courant, qu'importe la raison (Échap, close(), autre écran...). */
    private void hidden(WindowScreen hiddenScreen) {
        if (screen == hiddenScreen) {
            screen = null;
        }
    }

    /**
     * Ajoute un élément au modèle, branche sa poignée et, si la fenêtre est affichée, la
     * reconstruit.
     *
     * @return la poignée de l'élément, renvoyée telle quelle au mod
     */
    private <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(this::requestRebuild);
        model.add(element);
        requestRebuild();
        return handle;
    }

    /**
     * Reconstruit l'écran à la prochaine boucle du client, s'il est affiché.
     *
     * <p>Différé avec {@code Minecraft.tell} : un mod change souvent un état depuis l'action d'un
     * bouton, donc pendant que l'écran parcourt ses widgets ; les remplacer à ce moment-là serait
     * risqué. Plusieurs changements dans la même image ne donnent qu'une reconstruction.</p>
     */
    private void requestRebuild() {
        if (screen == null || rebuildScheduled) {
            return;
        }
        rebuildScheduled = true;
        Minecraft.getInstance().tell(() -> {
            rebuildScheduled = false;
            if (screen != null) {
                screen.rebuild();
            }
        });
    }

    /**
     * Ajoute une ligne de texte à la fin de la fenêtre.
     *
     * <p>Les éléments sont affichés de haut en bas, dans l'ordre des appels. Un élément ajouté
     * alors que la fenêtre est affichée apparaît à l'image suivante (à appeler sur le thread du client).
     * Le texte est aligné à gauche ; voir {@link #addText(Component, boolean)} pour le centrer.</p>
     *
     * @param text le texte à afficher
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addText(Component text) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, false, handle), handle);
    }

    /**
     * Ajoute une ligne de texte à la fin de la fenêtre, centrée ou alignée à gauche.
     *
     * @param text     le texte à afficher
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addText(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, centered, handle), handle);
    }

    /**
     * Ajoute un titre à la fin de la fenêtre.
     *
     * <p>Un titre se distingue d'un simple texte par son style. Il se place en général en premier,
     * mais rien ne l'impose : les éléments restent dans l'ordre des appels. Il est aligné à gauche ;
     * voir {@link #addTitle(Component, boolean)} pour le centrer.</p>
     *
     * @param text le titre à afficher
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addTitle(Component text) {
        ElementHandle handle = new ElementHandle();
        return add(new TitleElement(text, false, handle), handle);
    }

    /**
     * Ajoute un titre à la fin de la fenêtre, centré ou aligné à gauche.
     *
     * @param text     le titre à afficher
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addTitle(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TitleElement(text, centered, handle), handle);
    }

    /**
     * Ajoute un bouton à la fin de la fenêtre.
     *
     * <p>L'action est exécutée à chaque activation du bouton, à la souris ou au clavier. Le bouton
     * est aligné à gauche ; voir {@link #addButton(Component, Runnable, boolean)} pour le centrer.</p>
     *
     * @param label  le libellé du bouton
     * @param action exécutée quand on active le bouton
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addButton(Component label, Runnable action) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, false, handle), handle);
    }

    /**
     * Ajoute un bouton à la fin de la fenêtre, centré ou aligné à gauche.
     *
     * @param label    le libellé du bouton
     * @param action   exécutée quand on active le bouton
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addButton(Component label, Runnable action, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, centered, handle), handle);
    }

    /**
     * Ajoute une case à cocher à la fin de la fenêtre et renvoie sa poignée.
     *
     * <p>La poignée permet de lire et de changer l'état de la case, d'être prévenu des
     * changements et, comme toute {@link ElementHandle}, de la désactiver, la cacher ou lui donner
     * une infobulle. La case est alignée à gauche ; voir
     * {@link #addCheckbox(Component, boolean, boolean)} pour la centrer.</p>
     *
     * @param label        le texte affiché à côté de la case
     * @param defaultState {@code true} si la case est cochée à l'ouverture
     * @return la poignée de la case
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        return add(new CheckboxElement(label, handle, false), handle);
    }

    /**
     * Ajoute une case à cocher à la fin de la fenêtre, centrée ou alignée à gauche.
     *
     * @param label        le texte affiché à côté de la case
     * @param defaultState {@code true} si la case est cochée à l'ouverture
     * @param centered     {@code true} pour la centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de la case
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState, boolean centered) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        return add(new CheckboxElement(label, handle, centered), handle);
    }

    /**
     * Ajoute une ligne à la fin de la fenêtre et renvoie sa poignée, pour y placer des éléments
     * côte à côte. La ligne est alignée à gauche ; voir {@link #addRow(boolean)} pour la centrer.
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
     * Ajoute une ligne à la fin de la fenêtre, centrée ou alignée à gauche, et renvoie sa poignée.
     *
     * @param centered {@code true} pour la centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de la ligne, qui sert à y ajouter des éléments
     */
    public RowHandle addRow(boolean centered) {
        WindowModel children = new WindowModel();
        RowHandle handle = new RowHandle(children);
        return add(new RowElement(children, centered, handle), handle);
    }

    /**
     * Ajoute une liste défilante à la fin de la fenêtre et renvoie sa poignée, qui porte ses
     * lignes et sa sélection. La liste est alignée à gauche ; voir
     * {@link #addList(int, boolean)} pour la centrer.
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
     * Ajoute une liste défilante à la fin de la fenêtre, centrée ou alignée à gauche, et renvoie
     * sa poignée.
     *
     * @param visibleRows nombre de lignes visibles à la fois (au moins 1) ; fixe la hauteur
     * @param centered    {@code true} pour la centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée de la liste
     */
    public ListHandle addList(int visibleRows, boolean centered) {
        ListHandle handle = new ListHandle();
        return add(new ListElement(handle, visibleRows, centered), handle);
    }

    /**
     * Ajoute un champ de recherche à la fin de la fenêtre et renvoie sa poignée, qui porte le texte
     * saisi. Le champ est aligné à gauche ; voir {@link #addSearch(Component, boolean)} pour le
     * centrer.
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
     * Ajoute un champ de recherche à la fin de la fenêtre, centré ou aligné à gauche, et renvoie
     * sa poignée.
     *
     * @param placeholder texte d'indication, affiché tant que le champ est vide
     * @param centered    {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée du champ
     */
    public SearchHandle addSearch(Component placeholder, boolean centered) {
        SearchHandle handle = new SearchHandle();
        return add(new SearchElement(placeholder, handle, centered), handle);
    }
}
