package com.galdurstudio.galdurui.api.theme;

import java.util.List;

/**
 * What a theme colour is painted with: one colour, or a gradient through several colours.
 *
 * <p>In {@code theme.json}, a solid colour is {@code "#B8341F"}; a gradient is
 * {@code {"gradient": ["#B8341F", "#D98A1F"], "direction": "horizontal"}}, or a plain list of
 * colours for a vertical one. The colours are spread evenly, the first at the top (or left).</p>
 *
 * @param colors    ARGB colours, at least one; one colour means a solid paint
 * @param direction direction of the gradient, ignored for a solid paint
 */
public record GaldurPaint(List<Integer> colors, Direction direction) {
    /** Direction of a gradient. */
    public enum Direction {
        /** First colour at the top, last at the bottom. */
        VERTICAL,
        /** First colour on the left, last on the right. */
        HORIZONTAL
    }

    public GaldurPaint {
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("a paint needs at least one colour");
        }
        colors = List.copyOf(colors);
    }

    /** {@return a solid paint} */
    public static GaldurPaint solid(int argb) {
        return new GaldurPaint(List.of(argb), Direction.VERTICAL);
    }

    /** {@return a gradient through {@code colors}, spread evenly} */
    public static GaldurPaint gradient(Direction direction, Integer... colors) {
        return new GaldurPaint(List.of(colors), direction);
    }

    /** {@return {@code true} if the paint is a single colour} */
    public boolean isSolid() {
        return colors.size() == 1;
    }

    /** {@return the first colour: the whole paint when solid, the start of a gradient} */
    public int first() {
        return colors.get(0);
    }

    /**
     * {@return the colour at {@code position} along the gradient, from 0 (first colour) to 1 (last)}
     */
    public int at(float position) {
        if (isSolid()) {
            return first();
        }
        float scaled = Math.max(0, Math.min(1, position)) * (colors.size() - 1);
        int index = Math.min((int) scaled, colors.size() - 2);
        return mix(colors.get(index), colors.get(index + 1), scaled - index);
    }

    /** {@return the same paint with {@code argb} as colour number {@code index}} */
    public GaldurPaint withColor(int index, int argb) {
        Integer[] changed = colors.toArray(new Integer[0]);
        changed[index] = argb;
        return new GaldurPaint(List.of(changed), direction);
    }

    private static int mix(int from, int to, float amount) {
        int result = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            result |= Math.round(a + (b - a) * amount) << shift;
        }
        return result;
    }
}
