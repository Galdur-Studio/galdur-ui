package com.galdurstudio.galdurui.internal.window;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Contenu d'une rangée d'onglets : le titre et les éléments de chaque onglet, dans l'ordre.
 *
 * <p>Partagé entre la poignée publique
 * ({@link com.galdurstudio.galdurui.api.window.TabsHandle}), qui y ajoute les onglets, et
 * {@link TabsElement}, que {@link WindowScreen} lit pour construire l'affichage.</p>
 */
public class TabsModel {
    private final List<Component> titles = new ArrayList<>();
    private final List<WindowModel> contents = new ArrayList<>();

    /**
     * Ajoute un onglet à la fin.
     *
     * @param title   le titre de l'onglet
     * @param content les éléments de l'onglet
     */
    public void add(Component title, WindowModel content) {
        titles.add(title);
        contents.add(content);
    }

    /** {@return les titres des onglets, dans l'ordre ; copie non modifiable} */
    public List<Component> titles() {
        return List.copyOf(titles);
    }

    /** {@return le contenu de chaque onglet, dans l'ordre ; copie non modifiable} */
    public List<WindowModel> contents() {
        return List.copyOf(contents);
    }
}
