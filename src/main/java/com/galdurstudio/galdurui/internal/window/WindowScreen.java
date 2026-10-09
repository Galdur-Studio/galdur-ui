package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Écran Minecraft qui affiche le contenu d'un {@link WindowModel} dans un panneau centré.
 *
 * <p>Minecraft rappelle {@link #init()} à chaque redimensionnement : la mise en page et les widgets
 * sont donc recréés à chaque fois à partir des descriptions du modèle.</p>
 */
public class WindowScreen extends Screen {
    private final WindowModel model;

    /**
     * @param model le contenu à afficher
     */
    public WindowScreen(WindowModel model) {
        super(Component.literal("Galdur"));
        this.model = model;
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical();
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }
}
