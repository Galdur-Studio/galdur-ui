package com.galdurstudio.galdurui.api.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.Test;

class GaldurEasingTest {
    private static final Map<String, DoubleUnaryOperator> CURVES = Map.of(
            "LINEAR", GaldurEasing.LINEAR,
            "IN_QUAD", GaldurEasing.IN_QUAD,
            "OUT_QUAD", GaldurEasing.OUT_QUAD,
            "IN_OUT_QUAD", GaldurEasing.IN_OUT_QUAD,
            "OUT_CUBIC", GaldurEasing.OUT_CUBIC,
            "IN_OUT_CUBIC", GaldurEasing.IN_OUT_CUBIC,
            "OUT_BACK", GaldurEasing.OUT_BACK);

    @Test
    void everyCurveStartsAtZeroAndEndsAtOne() {
        CURVES.forEach((name, curve) -> {
            assertEquals(0, curve.applyAsDouble(0), 1e-9, name);
            assertEquals(1, curve.applyAsDouble(1), 1e-9, name);
        });
    }

    @Test
    void curvesWithoutOvershootStayBetweenZeroAndOne() {
        CURVES.forEach((name, curve) -> {
            if (curve == GaldurEasing.OUT_BACK) {
                return;
            }
            for (int step = 0; step <= 100; step++) {
                double value = curve.applyAsDouble(step / 100.0);
                assertTrue(value >= -1e-9 && value <= 1 + 1e-9, name + " at " + step + "%: " + value);
            }
        });
    }

    @Test
    void outCurvesAreAheadOfLinearHalfway() {
        assertTrue(GaldurEasing.OUT_QUAD.applyAsDouble(0.5) > 0.5);
        assertTrue(GaldurEasing.OUT_CUBIC.applyAsDouble(0.5) > 0.5);
        assertTrue(GaldurEasing.IN_QUAD.applyAsDouble(0.5) < 0.5);
    }
}
