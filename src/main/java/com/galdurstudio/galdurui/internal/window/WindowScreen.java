package com.galdurstudio.galdurui.internal.window;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.window.GaldurWindow;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WindowScreen extends Screen {
    public WindowScreen(GaldurWindow window) {
        super(Component.literal("Galdur"));
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
