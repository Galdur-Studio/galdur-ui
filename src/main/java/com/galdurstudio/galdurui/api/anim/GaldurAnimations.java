package com.galdurstudio.galdurui.api.anim;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import net.minecraft.Util;

/**
 * Global switch and durations of the Galdur UI animations.
 *
 * <p>The player can turn every animation off (Mods → Galdur UI → Config, or
 * {@code config/galdurui-client.toml}); changes are then instant. An animation written by a mod
 * should check {@link #enabled()}; {@link AnimatedValue} already does.</p>
 */
public final class GaldurAnimations {
    /** Hover fade (buttons, tabs), in milliseconds. */
    public static final long HOVER_MS = 90;
    /** Checkbox mark growing or shrinking, in milliseconds. */
    public static final long TOGGLE_MS = 110;
    /** Screen opening, in milliseconds. */
    public static final long OPEN_MS = 180;
    /** Tab content sliding in, in milliseconds. */
    public static final long PAGE_MS = 160;
    /** Time constant of smooth scrolling, in milliseconds. */
    public static final double SCROLL_SMOOTHING_MS = 45;

    private GaldurAnimations() {
    }

    /** {@return {@code true} unless the player turned animations off} */
    public static boolean enabled() {
        return GaldurConfig.animations();
    }

    /** {@return the animation clock, in milliseconds} */
    public static long now() {
        return Util.getMillis();
    }

    /**
     * {@return the progress of an animation started at {@code start}, from {@code 0} to
     * {@code 1}}; {@code 1} if animations are off or {@code durationMs} is not positive
     *
     * @param start      start time, read from {@link #now()}
     * @param durationMs duration, in milliseconds
     */
    public static double progress(long start, long durationMs) {
        if (!enabled() || durationMs <= 0) {
            return 1;
        }
        return Math.max(0, Math.min(1, (now() - start) / (double) durationMs));
    }

    /**
     * {@return {@code current} moved towards {@code target} with frame-rate independent
     * exponential smoothing}
     *
     * <p>For targets that keep changing (scrolling, progress bars). Returns {@code target} directly
     * when animations are off, when {@code current} is {@code NaN}, or once the remaining gap is
     * below {@code epsilon}.</p>
     *
     * @param current     the displayed value
     * @param target      the value to reach
     * @param elapsedMs   time since the previous frame
     * @param timeConstMs time constant: the gap shrinks by a factor of e (~2.7) in that time
     * @param epsilon     gap below which the target is reached
     */
    public static double approach(double current, double target, double elapsedMs, double timeConstMs, double epsilon) {
        if (!enabled() || Double.isNaN(current)) {
            return target;
        }
        double next = current + (target - current) * (1 - Math.exp(-Math.max(0, elapsedMs) / timeConstMs));
        return Math.abs(target - next) < epsilon ? target : next;
    }
}
