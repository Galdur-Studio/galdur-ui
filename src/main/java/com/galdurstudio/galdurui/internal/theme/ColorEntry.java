package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurWidget;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** One line of the theme editor's colour list: a swatch and the colour's name; a click selects it. */
final class ColorEntry extends GaldurWidget {
    static final int WIDTH = 120;
    private static final int HEIGHT = 14;
    private static final int SWATCH = 10;

    private final Supplier<GaldurPaint> paint;
    private final BooleanSupplier changed;
    private final BooleanSupplier selected;
    private final Runnable onSelect;

    /**
     * @param changed {@code true} when the player changed this colour, marked with a dot; otherwise
     *                it comes from the resource packs or falls back on a base colour
     */
    ColorEntry(Component name, Supplier<GaldurPaint> paint, BooleanSupplier changed, BooleanSupplier selected,
               Runnable onSelect) {
        super(0, 0, WIDTH, HEIGHT, name);
        this.paint = paint;
        this.changed = changed;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean chosen = selected.getAsBoolean();
        if (chosen) {
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), getY(), getWidth(), getHeight(), GaldurTheme.surface());
        }
        if (chosen || isHoveredOrFocused()) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                    chosen ? GaldurTheme.accent() : GaldurTheme.accentHover());
        }
        int swatchY = getY() + (getHeight() - SWATCH) / 2;
        GaldurRender.drawFill(guiGraphics, getX() + 2, swatchY, SWATCH, SWATCH, paint.get());
        GaldurRender.drawOutline(guiGraphics, getX() + 2, swatchY, SWATCH, SWATCH, GaldurTheme.textMuted());
        GaldurRender.text(guiGraphics, getMessage(), getX() + SWATCH + 6, getY() + (getHeight() - GaldurRender.lineHeight()) / 2 + 1,
                GaldurTheme.text(), false);
        if (changed.getAsBoolean()) {
            GaldurRender.drawFill(guiGraphics, getX() + getWidth() - 6, getY() + getHeight() / 2 - 1, 3, 3, GaldurTheme.accent());
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onSelect.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE,
                Component.translatable("galdurui.theme.entry.narration", getMessage(), PlayerTheme.hex(paint.get().first())));
    }
}
