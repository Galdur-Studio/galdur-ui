package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.ButtonElement;
import com.galdurstudio.galdurui.internal.window.CheckboxElement;
import com.galdurstudio.galdurui.internal.window.TextElement;
import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import net.minecraft.network.chat.Component;

/**
 * Poignée vers une ligne d'une {@link GaldurWindow} : des éléments placés côte à côte, de gauche
 * à droite, au lieu d'être empilés.
 *
 * <p>On l'obtient avec {@code GaldurWindow.addRow()}. On y ajoute des éléments comme dans la
 * fenêtre, et chacun renvoie sa propre poignée. Les éléments d'une ligne sont centrés
 * verticalement entre eux et séparés par l'écart du thème ({@code spacing.gap}).</p>
 *
 * <pre>{@code
 * RowHandle actions = window.addRow(true);
 * actions.addButton(Component.literal("Valider"), this::valider);
 * actions.addButton(Component.literal("Annuler"), window::close);
 * }</pre>
 *
 * <p>Comme toute {@link ElementHandle}, la ligne se désactive, se cache ou reçoit une infobulle
 * d'un seul coup :</p>
 * <ul>
 *   <li><strong>désactivée</strong>, elle désactive tous ses éléments ;</li>
 *   <li><strong>cachée</strong>, elle disparaît avec ses éléments et libère sa place ; une ligne
 *       dont tous les éléments sont cachés libère aussi sa place ;</li>
 *   <li>son <strong>infobulle</strong> s'affiche sur les éléments qui n'en ont pas une à eux.</li>
 * </ul>
 *
 * <p>Un élément ajouté alors que la fenêtre est affichée apparaît à l'image suivante. À appeler
 * sur le thread du client. Une ligne ne contient pas d'autre ligne.</p>
 */
public class RowHandle extends ElementHandle {
    private final WindowModel children;

    /**
     * Non publique : seule {@link GaldurWindow} crée des lignes.
     *
     * @param children la liste des éléments de la ligne, partagée avec l'élément affiché
     */
    RowHandle(WindowModel children) {
        this.children = children;
    }

    /**
     * Ajoute un texte à la fin de la ligne.
     *
     * @param text le texte à afficher
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addText(Component text) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, false, handle), handle);
    }

    /**
     * Ajoute un bouton à la fin de la ligne.
     *
     * @param label  le libellé du bouton
     * @param action exécutée quand on active le bouton, à la souris ou au clavier
     * @return la poignée de l'élément (désactiver, cacher, infobulle)
     */
    public ElementHandle addButton(Component label, Runnable action) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, false, handle), handle);
    }

    /**
     * Ajoute une case à cocher à la fin de la ligne et renvoie sa poignée.
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
     * Ajoute un élément à la ligne et branche sa poignée sur celle de la ligne : un changement
     * de l'élément remonte ainsi jusqu'à la fenêtre.
     *
     * @return la poignée de l'élément, renvoyée telle quelle au mod
     */
    private <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(this::changed);
        children.add(element);
        changed();
        return handle;
    }
}
