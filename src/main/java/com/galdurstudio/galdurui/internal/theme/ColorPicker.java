package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurWidget;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Colour picker of the theme editor: a square for saturation (rising left to right) and
 * brightness (falling top to bottom), next to a hue bar. Click or drag in either; every change is reported at
 * once, so the theme follows the mouse.
 */
final class ColorPicker extends GaldurWidget {
    /** Side of the saturation and brightness square, in GUI pixels. */
    private static final int SQUARE = 80;
    private static final int BAR_GAP = 6;
    private static final int BAR_WIDTH = 10;

    private final IntConsumer onChange;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha = 0xFF;
    /** What the current drag moves: the square, the bar, or nothing. */
    private Area dragging = Area.NONE;

    private enum Area { NONE, SQUARE, BAR }

    ColorPicker(IntConsumer onChange) {
        super(0, 0, SQUARE + BAR_GAP + BAR_WIDTH, SQUARE, Component.translatable("galdurui.theme.picker"));
        this.onChange = onChange;
    }

    /** Shows {@code argb} without reporting it; its alpha is kept for the colours chosen next. */
    void setColor(int argb) {
        alpha = argb >>> 24;
        float[] hsb = Hsb.fromRgb(argb);
        // A grey has no hue: keep the current one, so the bar does not jump to red.
        if (hsb[1] > 0 && hsb[2] > 0) {
            hue = hsb[0];
        }
        saturation = hsb[1];
        brightness = hsb[2];
    }

    /** {@return the colour shown, packed ARGB} */
    int color() {
        return (alpha << 24) | (Hsb.toRgb(hue, saturation, brightness) & 0xFFFFFF);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        // Square: one column per pixel, from white (top) to black (bottom) through the column's colour.
        for (int column = 0; column < SQUARE; column++) {
            int top = Hsb.toRgb(hue, column / (float) (SQUARE - 1), 1);
            GaldurRender.drawGradient(guiGraphics, x + column, y, 1, SQUARE, top, 0xFF000000);
        }
        GaldurRender.drawOutline(guiGraphics, x - 1, y - 1, SQUARE + 2, SQUARE + 2, GaldurTheme.surface());
        int markX = x + Math.round(saturation * (SQUARE - 1));
        int markY = y + Math.round((1 - brightness) * (SQUARE - 1));
        GaldurRender.drawOutline(guiGraphics, markX - 2, markY - 2, 5, 5, brightness > 0.5F ? 0xFF000000 : 0xFFFFFFFF);

        // Hue bar, from red at the top through every hue back to red.
        int barX = x + SQUARE + BAR_GAP;
        for (int row = 0; row < SQUARE; row++) {
            GaldurRender.drawFill(guiGraphics, barX, y + row, BAR_WIDTH, 1,
                    Hsb.toRgb(row / (float) (SQUARE - 1), 1, 1));
        }
        GaldurRender.drawOutline(guiGraphics, barX - 1, y - 1, BAR_WIDTH + 2, SQUARE + 2, GaldurTheme.surface());
        int hueY = y + Math.round(hue * (SQUARE - 1));
        GaldurRender.drawOutline(guiGraphics, barX - 2, hueY - 1, BAR_WIDTH + 4, 3, GaldurTheme.text());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragging = mouseX < getX() + SQUARE ? Area.SQUARE : mouseX >= getX() + SQUARE + BAR_GAP ? Area.BAR : Area.NONE;
        pick(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        pick(mouseX, mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragging = Area.NONE;
    }

    private void pick(double mouseX, double mouseY) {
        float down = Mth.clamp((float) (mouseY - getY()) / (SQUARE - 1), 0, 1);
        switch (dragging) {
            case SQUARE -> {
                saturation = Mth.clamp((float) (mouseX - getX()) / (SQUARE - 1), 0, 1);
                brightness = 1 - down;
            }
            case BAR -> hue = down;
            case NONE -> {
                return;
            }
        }
        onChange.accept(color());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE,
                Component.translatable("galdurui.theme.picker.narration", PlayerTheme.hex(color())));
    }
}
