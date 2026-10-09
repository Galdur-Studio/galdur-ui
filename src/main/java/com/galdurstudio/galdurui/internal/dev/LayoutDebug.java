package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Mode debug des layouts : dessine le contour de chaque widget de l'écran ouvert.
 *
 * <p>S'active et se désactive avec {@code /galdurui debug}. Le contour est tracé après le rendu
 * de l'écran (événement {@link ScreenEvent.Render.Post}), donc par-dessus tout et sur n'importe
 * quel écran, y compris ceux d'autres mods. Comme {@link GaldurDevCommands}, il n'est branché
 * qu'en environnement de développement.</p>
 *
 * <p>Limite : un {@link Screen} ne conserve que ses widgets, pas les layouts qui les ont placés.
 * Seuls les widgets ont donc un contour, pas les blocs (colonnes, lignes, panneaux). Les widgets
 * imbriqués dans un autre conteneur d'événements ne sont pas parcourus non plus.</p>
 */
public final class LayoutDebug {
    private static final int OUTLINE_COLOR = 0xFFFF00FF;
    private static boolean isDebug = false;

    private LayoutDebug() {
    }

    /**
     * Active le mode debug s'il est éteint, l'éteint sinon.
     *
     * @return {@code true} si le mode est actif après l'appel
     */
    public static boolean toggle() {
        isDebug = !isDebug;
        return isDebug;
    }

    /**
     * Dessine le contour de chaque widget de l'écran, tant que le mode debug est actif.
     *
     * <p>Appelé à chaque image : ne fait rien d'autre qu'un test tant que le mode est éteint.</p>
     *
     * @param screenEvent événement de fin de rendu de l'écran
     */
    public static void debugListener(ScreenEvent.Render.Post screenEvent) {
        if (!isDebug) {
            return;
        }
        Screen screen = screenEvent.getScreen();
        GuiGraphics graphics = screenEvent.getGuiGraphics();
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget) {
                GaldurRender.drawOutline(graphics, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), OUTLINE_COLOR);
            }
        }
    }

    /** Branche l'écouteur de rendu sur le bus de jeu. */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, LayoutDebug::debugListener);
    }
}
