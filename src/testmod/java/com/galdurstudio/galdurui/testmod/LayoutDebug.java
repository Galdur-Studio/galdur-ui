package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Layout debug mode ({@code /galdurui debug}): outlines every widget of the open screen, on any
 * screen, including other mods' ones.
 *
 * <p>Limit: a {@link Screen} only keeps its widgets, not the layouts that placed them, so blocks
 * (columns, rows, panels) get no outline. Widgets nested in another event container are not
 * visited either.</p>
 */
public final class LayoutDebug {
    private static final int OUTLINE_COLOR = 0xFFFF00FF;
    private static boolean enabled;

    private LayoutDebug() {
    }

    /** {@return {@code true} if the debug mode is on after the call} */
    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, LayoutDebug::onRender);
    }

    private static void onRender(ScreenEvent.Render.Post event) {
        if (!enabled) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        for (GuiEventListener child : event.getScreen().children()) {
            if (child instanceof AbstractWidget widget) {
                GaldurRender.drawOutline(graphics, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(),
                        OUTLINE_COLOR);
            }
        }
    }
}
