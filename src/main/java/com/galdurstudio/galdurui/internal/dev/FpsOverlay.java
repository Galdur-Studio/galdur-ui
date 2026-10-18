package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Compteur d'images par seconde affiché par-dessus n'importe quel écran ouvert, y compris les
 * écrans vanilla : l'écran de débogage (F3) est masqué dès qu'un écran est ouvert. Sert aux
 * mesures de performance (UI-38) : comparer un coffre vanilla et un écran Galdur.
 *
 * <p>S'active et se désactive avec {@code /galdurui fps}. Affiche les FPS de la dernière seconde
 * et leur moyenne sur les 5 dernières secondes, plus stable à relever. Développement
 * uniquement.</p>
 */
public final class FpsOverlay {
    /** Fenêtre de la moyenne glissante, en secondes. */
    private static final int WINDOW = 5;

    private static boolean enabled;
    private static final int[] samples = new int[WINDOW];
    private static int sampleCount;
    private static int next;
    private static long lastSample;

    private FpsOverlay() {
    }

    /** Branche l'affichage sur le bus de jeu. */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, FpsOverlay::onRender);
    }

    /**
     * Active le compteur s'il est éteint, l'éteint sinon. La moyenne repart de zéro.
     *
     * @return {@code true} si le compteur est affiché après l'appel
     */
    public static boolean toggle() {
        enabled = !enabled;
        sampleCount = 0;
        next = 0;
        return enabled;
    }

    private static void onRender(ScreenEvent.Render.Post event) {
        if (!enabled) {
            return;
        }
        int fps = Minecraft.getInstance().getFps();
        long now = Util.getMillis();
        if (now - lastSample >= 1000) {
            lastSample = now;
            samples[next] = fps;
            next = (next + 1) % WINDOW;
            sampleCount = Math.min(WINDOW, sampleCount + 1);
        }
        int sum = 0;
        for (int index = 0; index < sampleCount; index++) {
            sum += samples[index];
        }
        int average = sampleCount == 0 ? fps : Math.round(sum / (float) sampleCount);
        Component text = Component.literal("FPS " + fps + "  (moy. " + WINDOW + " s : " + average + ")");
        GaldurRender.drawBatched(event.getGuiGraphics(), () -> {
            GaldurRender.drawFill(event.getGuiGraphics(), 2, 2, GaldurRender.textWidth(text) + 6, GaldurRender.lineHeight() + 4, 0xA0000000);
            GaldurRender.text(event.getGuiGraphics(), text, 5, 4, 0xFFFFFF55, true);
        });
    }
}
