package com.galdurstudio.galdurui.api.anim;

import java.util.function.DoubleUnaryOperator;

/**
 * Easing curves: they map a linear progress {@code t} from {@code 0} to {@code 1} to a progress
 * that speeds up or slows down. Every curve gives {@code 0} at {@code 0} and {@code 1} at
 * {@code 1}. Use them with {@link AnimatedValue}, or directly:
 *
 * <pre>{@code
 * double t = Math.min(1, elapsed / (double) durationMs);
 * int y = (int) Math.round(from + (to - from) * GaldurEasing.OUT_CUBIC.applyAsDouble(t));
 * }</pre>
 *
 * <p>{@link #OUT_CUBIC} is the default choice for UI: it starts fast, so it reacts at once, and
 * lands softly.</p>
 */
public final class GaldurEasing {
    /** Constant speed. */
    public static final DoubleUnaryOperator LINEAR = t -> t;
    /** Starts slow, ends fast. */
    public static final DoubleUnaryOperator IN_QUAD = t -> t * t;
    /** Starts fast, ends slow; subtler than {@link #OUT_CUBIC}. */
    public static final DoubleUnaryOperator OUT_QUAD = t -> 1 - (1 - t) * (1 - t);
    /** Slow at both ends. */
    public static final DoubleUnaryOperator IN_OUT_QUAD = t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    /** Starts fast, lands softly: the default choice. */
    public static final DoubleUnaryOperator OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);
    /** Slow at both ends, stronger than {@link #IN_OUT_QUAD}. */
    public static final DoubleUnaryOperator IN_OUT_CUBIC = t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    /** Slightly overshoots the target before settling: a small bounce. */
    public static final DoubleUnaryOperator OUT_BACK = t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };

    private GaldurEasing() {
    }
}
