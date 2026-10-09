package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Écran Minecraft qui affiche le contenu d'un {@link WindowModel} dans un panneau centré.
 *
 * <p>Minecraft rappelle {@link #init()} à chaque redimensionnement : la mise en page et les widgets
 * sont donc recréés à chaque fois à partir des descriptions du modèle.</p>
 */
public class WindowScreen extends Screen {
    private final WindowModel model;
    private final Runnable onRemoved;

    /**
     * @param model     le contenu à afficher
     * @param onRemoved appelé quand l'écran disparaît, quelle qu'en soit la raison (Échap, bouton
     *                  de fermeture, autre écran qui le remplace). Permet de prévenir la fenêtre qui
     *                  l'a ouvert sans que ce package dépende de {@code api}.
     */
    public WindowScreen(WindowModel model, Runnable onRemoved) {
        super(Component.literal("Galdur"));
        this.model = model;
        this.onRemoved = onRemoved;
    }

    /**
     * Prévient la fenêtre que l'écran n'est plus affiché.
     *
     * <p>Minecraft appelle cette méthode à chaque changement d'écran, mais pas lors d'un simple
     * redimensionnement (qui ne fait que rappeler {@link #init()}).</p>
     */
    @Override
    public void removed() {
        super.removed();
        onRemoved.run();
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical();
        layout.spacing(GaldurTheme.gap());
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        for (WindowElement element : model.elements()) {
            LayoutElement widget = element.createWidget();
            layout.addChild(widget, element::adjustSettings);
        }
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
        layout.visitWidgets(this::addRenderableWidget);
    }
}
