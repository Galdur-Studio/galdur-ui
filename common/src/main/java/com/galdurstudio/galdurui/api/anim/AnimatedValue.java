package com.galdurstudio.galdurui.api.anim;

import java.util.function.DoubleUnaryOperator;

/**
 * A number that moves from one value to another over a given time, along an easing curve
 * ({@link GaldurEasing}).
 *
 * <p>Set the target whenever you like, every frame for instance: a transition only starts when
 * the target changes, and it starts from the value shown at that moment, so changing course midway
 * never jumps.</p>
 *
 * <pre>{@code
 * private final AnimatedValue hover = new AnimatedValue(0, GaldurAnimations.HOVER_MS, GaldurEasing.OUT_QUAD);
 *
 * protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
 *     hover.setTarget(isHovered() ? 1 : 0);
 *     int color = mix(idle, hovered, hover.get());
 *     ...
 * }
 * }</pre>
 *
 * <p>When the player turned animations off, {@link #get()} returns the target directly. Client
 * thread only.</p>
 */
public final class AnimatedValue {
    private final long durationMs;
    private final DoubleUnaryOperator easing;
    private double from;
    private double to;
    private long start;

    /**
     * @param initial    starting value, shown without animation
     * @param durationMs duration of a transition, in milliseconds
     * @param easing     transition curve, for example {@link GaldurEasing#OUT_CUBIC}
     */
    public AnimatedValue(double initial, long durationMs, DoubleUnaryOperator easing) {
        this.durationMs = durationMs;
        this.easing = easing;
        this.from = initial;
        this.to = initial;
    }

    /** Changes the target; a transition starts from the current value unless it already is the target. */
    public void setTarget(double target) {
        if (target == to) {
            return;
        }
        from = get();
        to = target;
        start = GaldurAnimations.now();
    }

    /** Jumps to {@code value} without a transition. */
    public void snap(double value) {
        from = value;
        to = value;
    }

    /** {@return the value to show now} */
    public double get() {
        double t = GaldurAnimations.progress(start, durationMs);
        if (t >= 1) {
            return to;
        }
        return from + (to - from) * easing.applyAsDouble(t);
    }

    /** {@return the value being moved towards} */
    public double getTarget() {
        return to;
    }

    /** {@return {@code true} while the shown value has not reached the target} */
    public boolean isRunning() {
        return get() != to;
    }
}
