package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Frames-per-second counter drawn over any open screen ({@code /galdurui fps}). The F3 debug
 * screen is hidden as soon as a screen opens, so this is the way to compare a vanilla screen with
 * a Galdur one. Shows the last second and the average of the last {@value #WINDOW} seconds.
 */
public final class FpsOverlay {
    private static final int WINDOW = 5;

    private static boolean enabled;
    private static final int[] samples = new int[WINDOW];
    private static int sampleCount;
    private static int next;
    private static long lastSample;

    private FpsOverlay() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, FpsOverlay::onRender);
    }

    /** {@return {@code true} if the counter is shown after the call}; the average restarts from scratch */
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
        Component text = Component.literal("FPS " + fps + "  (" + WINDOW + " s avg: " + average + ")");
        GaldurRender.drawBatched(event.getGuiGraphics(), () -> {
            GaldurRender.drawFill(event.getGuiGraphics(), 2, 2, GaldurRender.textWidth(text) + 6,
                    GaldurRender.lineHeight() + 4, 0xA0000000);
            GaldurRender.text(event.getGuiGraphics(), text, 5, 4, 0xFFFFFF55, true);
        });
    }
}
