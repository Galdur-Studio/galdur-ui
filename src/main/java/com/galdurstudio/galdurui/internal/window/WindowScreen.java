package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
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
        layout.spacing(GaldurTheme.gap());
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        for (WindowElement element : model.elements()) {
            LayoutElement widget = element.createWidget();
            layout.addChild(widget);
        }
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
        layout.visitWidgets(this::addRenderableWidget);
    }
}
