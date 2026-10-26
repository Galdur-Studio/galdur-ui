package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Themed progress bar, with an optional text centred on it, usable in vanilla layouts.
 *
 * <p>Like {@link GaldurScrollList}, the widget keeps neither the progress nor the text: it
 * <strong>reads them every frame</strong> from suppliers, so the bar can move every tick without
 * being rebuilt.</p>
 *
 * <pre>{@code
 * double[] progress = {0.25};
 * column.addChild(GaldurProgressBar.of(120, () -> progress[0],
 *         () -> Component.literal("16 / 64")));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Fill</strong>: left to right, a gradient from {@code accent} (redstone) to
 *       {@code ember} over the full width, so the head warms up as it moves. Sunken track
 *       ({@code field} sprite tinted {@code background}); the head column is lightened to mark
 *       the front.</li>
 *   <li><strong>Animation</strong>: the bar slides to its new value instead of jumping (it goes
 *       back at once, so a reset is clean), and a shine runs along the filled part while it is
 *       not full.</li>
 *   <li><strong>Text</strong>: centred, in {@code text} with shadow; cut with "…" when too long.
 *       Without text ({@code null}) the bar is alone.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): flat fill and {@code text_muted}
 *       text, no animation.</li>
 * </ul>
 *
 * <p>The bar is not interactive: it takes neither focus nor clicks. Height from the theme:
 * {@code spacing.progress_height}.</p>
 */
public class GaldurProgressBar extends AbstractWidget {
    /** Frame thickness, in pixels. */
    private static final int BORDER = 1;
    /** Gap between the edge and a cut text, in pixels. */
    private static final int TEXT_PADDING = 2;
    /** Time constant of the slide to the new value, in milliseconds. */
    private static final double SMOOTHING_MS = 90;
    /** Width of the shine, in pixels. */
    private static final int SHINE_WIDTH = 18;
    /** Duration of one shine pass, left to right, in milliseconds. */
    private static final long SHINE_PERIOD_MS = 1600;
    /** Maximum shine strength, from 0 (invisible) to 1 (white). */
    private static final double SHINE_STRENGTH = 0.45;
    /** Lightening of the head column, from 0 to 1. */
    private static final double HEAD_STRENGTH = 0.35;

    private final DoubleSupplier progress;
    private final Supplier<Component> label;

    /** Shown progress, which catches up with the real one; {@code NaN} before the first frame. */
    private double shown = Double.NaN;
    /** Time of the previous frame, in milliseconds ({@code Util.getMillis()}). */
    private long lastFrame;

    /**
     * Prefer {@link #of(int, DoubleSupplier, Supplier)}.
     *
     * @param width    total width, frame included, in pixels
     * @param progress supplier of the progress, from {@code 0} (empty) to {@code 1} (full), called
     *                 every frame; values outside are clamped
     * @param label    supplier of the text shown on the bar, or of {@code null} for none
     */
    protected GaldurProgressBar(int width, DoubleSupplier progress, Supplier<Component> label) {
        super(0, 0, width, GaldurTheme.progressHeight(), Component.empty());
        this.progress = progress;
        this.label = label;
    }

    /**
     * {@return a progress bar without text, at {@code (0, 0)} for a layout to place}
     *
     * @param width    total width, frame included, in pixels
     * @param progress supplier of the progress, from {@code 0} to {@code 1}, called every frame
     */
    public static GaldurProgressBar of(int width, DoubleSupplier progress) {
        return new GaldurProgressBar(width, progress, () -> null);
    }

    /**
     * {@return a progress bar with a centred text, at {@code (0, 0)}}
     *
     * @param width    total width, frame included, in pixels
     * @param progress supplier of the progress, from {@code 0} to {@code 1}, called every frame
     * @param label    supplier of the text, called every frame; {@code null} for none
     */
    public static GaldurProgressBar of(int width, DoubleSupplier progress, Supplier<Component> label) {
        return new GaldurProgressBar(width, progress, label);
    }

    /**
     * {@return the width that shows {@code label} in full on the bar, or {@code minWidth} if larger}
     *
     * @param label    the expected text, or {@code null} for none
     * @param minWidth minimum width, in pixels
     */
    public static int preferredWidth(@Nullable Component label, int minWidth) {
        int text = label == null ? 0 : GaldurRender.textWidth(label) + 2 * (TEXT_PADDING + BORDER);
        return Math.max(minWidth, text);
    }

    /** {@return the progress read, clamped to {@code 0}..{@code 1}; {@code NaN} counts as {@code 0}} */
    private double clampedProgress() {
        double value = progress.getAsDouble();
        if (Double.isNaN(value)) {
            return 0;
        }
        return Math.max(0, Math.min(1, value));
    }

    /**
     * Moves the shown progress towards the real one and returns it.
     *
     * <p>Exponential smoothing, independent of the frame rate. A decrease applies at once; a
     * disabled bar is not animated.</p>
     */
    private double updateShown() {
        double target = clampedProgress();
        long now = Util.getMillis();
        if (Double.isNaN(shown) || target < shown || !active || !GaldurAnimations.enabled()) {
            shown = target;
        } else {
            double elapsed = Math.max(0, now - lastFrame);
            shown += (target - shown) * (1 - Math.exp(-elapsed / SMOOTHING_MS));
            if (target - shown < 0.001) {
                shown = target;
            }
        }
        lastFrame = now;
        return shown;
    }

    /** {@return the colour at {@code t} (0 to 1) between {@code from} and {@code to}, per channel} */
    private static int lerpColor(int from, int to, double t) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            result |= ((int) Math.round(a + (b - a) * t) & 0xFF) << shift;
        }
        return result;
    }

    /**
     * {@return the shine strength in column {@code column} of the filled part, from 0 to 1}
     *
     * <p>The shine starts just before the left edge, leaves after the head, then starts over; its
     * strength falls off linearly from its centre.</p>
     */
    private static double shineAt(int column, int filled, long now) {
        double travel = filled + SHINE_WIDTH;
        double center = (now % SHINE_PERIOD_MS) / (double) SHINE_PERIOD_MS * travel - SHINE_WIDTH / 2.0;
        double distance = Math.abs(column + 0.5 - center);
        return Math.max(0, 1 - distance / (SHINE_WIDTH / 2.0));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int innerX = getX() + BORDER;
        int innerY = getY() + BORDER;
        int innerWidth = getWidth() - 2 * BORDER;
        int innerHeight = getHeight() - 2 * BORDER;
        double value = updateShown();
        int filled = (int) Math.round(innerWidth * value);

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), getY(), getWidth(), getHeight(),
                GaldurTheme.background());
        if (filled > 0 && !active) {
            GaldurRender.drawFill(guiGraphics, innerX, innerY, filled, innerHeight, GaldurTheme.textMuted());
        } else if (filled > 0) {
            int from = GaldurTheme.accent();
            int to = GaldurTheme.ember();
            boolean shining = clampedProgress() < 1 && GaldurAnimations.enabled();
            long now = Util.getMillis();
            int white = 0xFFFFFFFF;
            for (int column = 0; column < filled; column++) {
                int color = lerpColor(from, to, innerWidth <= 1 ? 0 : column / (double) (innerWidth - 1));
                double light = shining ? shineAt(column, filled, now) * SHINE_STRENGTH : 0;
                if (column == filled - 1 && filled < innerWidth) {
                    light = Math.max(light, HEAD_STRENGTH);
                }
                if (light > 0) {
                    color = lerpColor(color, white, light);
                }
                GaldurRender.drawFill(guiGraphics, innerX + column, innerY, 1, innerHeight, color);
            }
        }

        Component text = label.get();
        if (text == null) {
            return;
        }
        int textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        int textY = innerY + (innerHeight - GaldurRender.lineHeight()) / 2 + 1;
        int maxWidth = innerWidth - 2 * TEXT_PADDING;
        if (GaldurRender.textWidth(text) <= maxWidth) {
            GaldurRender.text(guiGraphics, text, getX() + (getWidth() - GaldurRender.textWidth(text)) / 2,
                    textY, textColor, true);
        } else {
            GaldurRender.text(guiGraphics, GaldurRender.truncateText(text, maxWidth), innerX + TEXT_PADDING,
                    textY, textColor, true);
        }
    }

    /** A progress bar is not clickable: no sound, no mouse focus. */
    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }

    /** A progress bar is skipped by keyboard navigation (Tab, arrows). */
    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // "Progress bar: Iron ingots: 16 / 64", then "25 percent".
        Component text = label.get();
        Component percent = GaldurNarration.ours("progress.percent", Math.round(clampedProgress() * 100));
        narrationElementOutput.add(NarratedElementType.TITLE,
                GaldurNarration.withState(GaldurNarration.ours("progress", text != null ? text : percent), active));
        if (text != null) {
            narrationElementOutput.add(NarratedElementType.POSITION, percent);
        }
    }
}
