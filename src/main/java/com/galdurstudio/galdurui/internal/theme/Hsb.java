package com.galdurstudio.galdurui.internal.theme;

/** Conversion between RGB and hue, saturation, brightness (each from 0 to 1), for the colour picker. */
final class Hsb {
    private Hsb() {
    }

    /** {@return the opaque colour, packed ARGB} */
    static int toRgb(float hue, float saturation, float brightness) {
        float h = (hue - (float) Math.floor(hue)) * 6;
        int sector = (int) h;
        float f = h - sector;
        float p = brightness * (1 - saturation);
        float q = brightness * (1 - saturation * f);
        float t = brightness * (1 - saturation * (1 - f));
        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = brightness; g = t; b = p; }
            case 1 -> { r = q; g = brightness; b = p; }
            case 2 -> { r = p; g = brightness; b = t; }
            case 3 -> { r = p; g = q; b = brightness; }
            case 4 -> { r = t; g = p; b = brightness; }
            default -> { r = brightness; g = p; b = q; }
        }
        return 0xFF000000 | channel(r) << 16 | channel(g) << 8 | channel(b);
    }

    /** {@return hue, saturation and brightness of {@code rgb} (alpha ignored)} */
    static float[] fromRgb(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255F;
        float g = ((rgb >> 8) & 0xFF) / 255F;
        float b = (rgb & 0xFF) / 255F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float hue;
        if (delta == 0) {
            hue = 0;
        } else if (max == r) {
            hue = ((g - b) / delta) / 6;
        } else if (max == g) {
            hue = ((b - r) / delta + 2) / 6;
        } else {
            hue = ((r - g) / delta + 4) / 6;
        }
        if (hue < 0) {
            hue += 1;
        }
        return new float[] {hue, max == 0 ? 0 : delta / max, max};
    }

    private static int channel(float value) {
        return Math.round(Math.max(0, Math.min(1, value)) * 255);
    }
}
