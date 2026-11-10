package com.galdurstudio.galdurui.api.window;

import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;

/**
 * Handle to a progress bar of a {@link GaldurWindow}: its progress and the text shown on it.
 *
 * <p>Returned by {@code addProgress(...)}. The shown widget reads the progress and the text every
 * frame: change them as often as you like (every tick, for instance), even while the window is
 * open, without rebuilding the screen.</p>
 *
 * <pre>{@code
 * ProgressHandle craft = window.addProgress(Component.literal("Iron ingots: 0 / 64"));
 * craft.setProgress(16, 64);
 * craft.setLabel(Component.literal("Iron ingots: 16 / 64"));
 * }</pre>
 *
 * <p>The bar width is computed when the screen is built, from the text. A longer text given later
 * is cut with "…" until the next rebuild (resize, or an element added to the window). Like any
 * {@link ElementHandle}, the bar can be disabled (muted colours), hidden or given a tooltip.</p>
 *
 * <p>Call on the client thread.</p>
 */
public class ProgressHandle extends ElementHandle {
    private double progress;
    @Nullable
    private Component label;

    /**
     * Package-private: only {@link GaldurWindow} and tabs create bars.
     *
     * @param label the initial text, or {@code null} for none
     */
    ProgressHandle(@Nullable Component label) {
        this.label = label;
    }

    /** {@return the progress, from {@code 0} (empty) to {@code 1} (full)} */
    public double getProgress() {
        return progress;
    }

    /**
     * Changes the progress.
     *
     * @param progress from {@code 0} (empty) to {@code 1} (full); values outside are clamped, and
     *                 {@code NaN} counts as {@code 0}
     */
    public void setProgress(double progress) {
        this.progress = Double.isNaN(progress) ? 0 : Math.max(0, Math.min(1, progress));
    }

    /**
     * Changes the progress from a done count and a total, for instance 16 items crafted out of 64.
     *
     * @param done  what is done
     * @param total the total; when {@code 0} or less, the bar is empty
     */
    public void setProgress(long done, long total) {
        setProgress(total <= 0 ? 0 : (double) done / total);
    }

    /** {@return the text shown on the bar, or {@code null} if none} */
    @Nullable
    public Component getLabel() {
        return label;
    }

    /**
     * Changes the text shown on the bar.
     *
     * @param label the new text, or {@code null} for none
     */
    public void setLabel(@Nullable Component label) {
        this.label = label;
    }
}
