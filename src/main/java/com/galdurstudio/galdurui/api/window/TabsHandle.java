package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.TabsModel;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;

/**
 * Poignée vers une rangée d'onglets d'une {@link GaldurWindow} : ses onglets et l'onglet actif.
 *
 * <p>On l'obtient avec {@code GaldurWindow.addTabs()}. Chaque {@link #addTab(Component)} renvoie
 * un {@link TabHandle}, qu'on remplit comme une fenêtre. Le premier onglet ajouté est actif
 * au départ.</p>
 *
 * <pre>{@code
 * TabsHandle onglets = window.addTabs(true);
 * TabHandle stockage = onglets.addTab(Component.literal("Stockage"));
 * TabHandle autocraft = onglets.addTab(Component.literal("Autocraft"));
 * onglets.onSelect(index -> System.out.println("Onglet : " + index));
 * autocraft.select();   // ou onglets.setSelected(1)
 * }</pre>
 *
 * <p>Comme pour {@link ListHandle}, l'état vit ici et non dans le widget affiché : l'onglet actif
 * survit aux redimensionnements, et le contenu de chaque onglet (texte d'une recherche,
 * défilement d'une liste...) est gardé quand on passe d'un onglet à l'autre.</p>
 *
 * <p>Comme toute {@link ElementHandle}, la rangée se désactive, se cache ou reçoit une
 * infobulle :</p>
 * <ul>
 *   <li><strong>désactivée</strong>, on ne change plus d'onglet, et le contenu affiché est
 *       désactivé lui aussi ;</li>
 *   <li><strong>cachée</strong>, elle disparaît avec tout son contenu et libère sa place ;</li>
 *   <li>son <strong>infobulle</strong> s'affiche au survol de la rangée d'onglets seulement, pas
 *       sur le contenu.</li>
 * </ul>
 *
 * <p>À appeler sur le thread du client.</p>
 */
public class TabsHandle extends ElementHandle {
    private final TabsModel model;
    private final List<TabHandle> tabs = new ArrayList<>();
    private final List<TabHandle> tabsView = Collections.unmodifiableList(tabs);
    private int selected;
    private IntConsumer action;

    /**
     * Non publique : seule {@link GaldurWindow} crée des onglets.
     *
     * @param model les titres et contenus des onglets, partagés avec l'élément affiché
     */
    TabsHandle(TabsModel model) {
        this.model = model;
    }

    /**
     * Ajoute un onglet à droite des autres et renvoie sa poignée, qui sert à remplir son contenu.
     *
     * @param title le titre affiché sur l'onglet
     * @return la poignée de l'onglet
     */
    public TabHandle addTab(Component title) {
        WindowModel content = new WindowModel();
        TabHandle tab = new TabHandle(this, content, tabs.size());
        model.add(title, content);
        tabs.add(tab);
        changed();
        return tab;
    }

    /** {@return les onglets, dans l'ordre, en lecture seule ; la vue suit les ajouts} */
    public List<TabHandle> getTabs() {
        return tabsView;
    }

    /** {@return l'index de l'onglet actif, ou {@code -1} s'il n'y a encore aucun onglet} */
    public int getSelected() {
        return tabs.isEmpty() ? -1 : selected;
    }

    /**
     * Rend un onglet actif. Si la valeur change réellement, son contenu remplace l'ancien à
     * l'image suivante et l'action donnée à {@link #onSelect(IntConsumer)} est appelée, que le
     * changement vienne du joueur ou de ce code.
     *
     * @param index l'index de l'onglet ; un index hors des onglets est ignoré
     */
    public void setSelected(int index) {
        if (index < 0 || index >= tabs.size() || index == selected) {
            return;
        }
        selected = index;
        changed();
        if (action != null) {
            action.accept(index);
        }
    }

    /**
     * Définit l'action appelée quand l'onglet actif change. Une seule action est conservée : un
     * nouvel appel remplace la précédente. Elle reçoit l'index du nouvel onglet actif.
     *
     * @param action appelée à chaque changement d'onglet
     */
    public void onSelect(IntConsumer action) {
        this.action = action;
    }
}
