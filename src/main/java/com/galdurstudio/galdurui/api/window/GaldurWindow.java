package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
    private WindowScreen currentScreen;
    private Screen lastScreen;

    /**
     * Crée une fenêtre vide. Préférer {@code GaldurLib.createWindow()}.
     */
    public GaldurWindow() {
    }

    /**
     * Affiche la fenêtre à l'écran. Sans effet si elle est déjà ouverte.
     */
    public void open() {
        if (currentScreen != null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.tell(() -> {
            lastScreen = minecraft.screen;
            currentScreen = new WindowScreen(model, this::reset);
            minecraft.setScreen(currentScreen);
        });
    }

    /**
     * Ferme la fenêtre et revient à l'écran précédent. Sans effet si elle est fermée.
     */
    public void close() {
        if (currentScreen == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(lastScreen);
        currentScreen = null;
        lastScreen = null;
    }

    /** Oublie les écrans : appelée quand l'écran disparaît, par {@link #close()} ou autrement (Échap...). */
    private void reset() {
        currentScreen = null;
        lastScreen = null;
    }

    /**
     * Ajoute une ligne de texte à la fin de la fenêtre.
     *
     * <p>Les éléments sont affichés de haut en bas, dans l'ordre des appels. À appeler avant
     * {@link #open()}. Le texte est aligné à gauche ; voir {@link #addText(Component, boolean)}
     * pour le centrer.</p>
     *
     * @param text le texte à afficher
     */
    public void addText(Component text) {
        TextElement element = new TextElement(text, false);
        model.add(element);
    }

    /**
     * Ajoute une ligne de texte à la fin de la fenêtre, centrée ou alignée à gauche.
     *
     * @param text     le texte à afficher
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     */
    public void addText(Component text, boolean centered) {
        TextElement element = new TextElement(text, centered);
        model.add(element);
    }

    /**
     * Ajoute un titre à la fin de la fenêtre.
     *
     * <p>Un titre se distingue d'un simple texte par son style. Il se place en général en premier,
     * mais rien ne l'impose : les éléments restent dans l'ordre des appels. Il est aligné à gauche ;
     * voir {@link #addTitle(Component, boolean)} pour le centrer.</p>
     *
     * @param text le titre à afficher
     */
    public void addTitle(Component text) {
        TitleElement element = new TitleElement(text, false);
        model.add(element);
    }

    /**
     * Ajoute un titre à la fin de la fenêtre, centré ou aligné à gauche.
     *
     * @param text     le titre à afficher
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     */
    public void addTitle(Component text, boolean centered) {
        TitleElement element = new TitleElement(text, centered);
        model.add(element);
    }

    /**
     * Ajoute un bouton à la fin de la fenêtre.
     *
     * <p>L'action est exécutée à chaque activation du bouton, à la souris ou au clavier. Le bouton
     * est aligné à gauche ; voir {@link #addButton(Component, Runnable, boolean)} pour le centrer.</p>
     *
     * @param label  le libellé du bouton
     * @param action exécutée quand on active le bouton
     */
    public void addButton(Component label, Runnable action) {
        ButtonElement element = new ButtonElement(label, action, false);
        model.add(element);
    }

    /**
     * Ajoute un bouton à la fin de la fenêtre, centré ou aligné à gauche.
     *
     * @param label    le libellé du bouton
     * @param action   exécutée quand on active le bouton
     * @param centered {@code true} pour le centrer dans la fenêtre, {@code false} pour l'aligner à gauche
     */
    public void addButton(Component label, Runnable action, boolean centered) {
        ButtonElement element = new ButtonElement(label, action, centered);
        model.add(element);
    }

    /**
     * Ajoute une case à cocher à la fin de la fenêtre et renvoie sa poignée.
     *
     * <p>La poignée permet de lire et de changer l'état de la case, et d'être prévenu des
     * changements. La case est alignée à gauche ; voir
     * {@link #addCheckbox(Component, boolean, boolean)} pour la centrer.</p>
     *
     * @param label        le texte affiché à côté de la case
     * @param defaultState {@code true} si la case est cochée à l'ouverture
     * @return la poignée de la case
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        CheckboxElement element = new CheckboxElement(label, handle, false);
        model.add(element);
        return handle;
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
        CheckboxElement element = new CheckboxElement(label, handle, centered);
        model.add(element);
        return handle;
    }
}
