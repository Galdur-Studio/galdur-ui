package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.*;
import net.minecraft.client.Minecraft;

/**
 * Un écran décrit par le code d'un mod : titre, textes, boutons, cases à cocher, listes, onglets...
 *
 * <p>On la crée avec {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}. Elle ne
 * contient que la description de l'écran ; la mise en page, le thème et le rendu sont
 * assurés par Galdur UI, et aucune coordonnée n'est jamais à calculer.</p>
 *
 * <p>Les méthodes {@code add...(...)} héritées de {@link ElementContainer} ajoutent les éléments
 * de haut en bas, dans l'ordre des appels. Un élément ajouté alors que la fenêtre est affichée
 * apparaît à l'image suivante (à appeler sur le thread du client).</p>
 */
public class GaldurWindow extends ElementContainer {
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
    @Override
    <H extends ElementHandle> H add(WindowElement element, H handle) {
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
     * Ajoute une rangée d'onglets à la fin de la fenêtre, alignée à gauche, et renvoie sa poignée ;
     * voir {@link #addTabs(boolean)} pour la centrer.
     *
     * <p>Chaque onglet a son propre contenu, qu'on remplit comme une fenêtre. Seul le contenu de
     * l'onglet actif est affiché, sous la rangée ; la fenêtre garde la taille du plus grand
     * contenu, pour ne pas changer de taille quand on change d'onglet.</p>
     *
     * <pre>{@code
     * TabsHandle onglets = window.addTabs();
     * TabHandle stockage = onglets.addTab(Component.literal("Stockage"));
     * stockage.addSearch(Component.literal("Rechercher..."));
     * TabHandle artisanat = onglets.addTab(Component.literal("Artisanat"));
     * artisanat.addText(Component.literal("Table 3 × 3"));
     * }</pre>
     *
     * @return la poignée des onglets, qui sert à en ajouter et à choisir l'onglet actif
     */
    public TabsHandle addTabs() {
        return addTabs(false);
    }

    /**
     * Ajoute une rangée d'onglets à la fin de la fenêtre, centrée ou alignée à gauche, et renvoie
     * sa poignée.
     *
     * @param centered {@code true} pour la centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     * @return la poignée des onglets
     */
    public TabsHandle addTabs(boolean centered) {
        TabsModel tabs = new TabsModel();
        TabsHandle handle = new TabsHandle(tabs);
        return add(new TabsElement(tabs, handle, centered), handle);
    }
}
