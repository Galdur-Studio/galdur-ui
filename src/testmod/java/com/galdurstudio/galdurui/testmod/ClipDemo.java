package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Clipping demo: a text much longer than its frame scrolls from right to left. It must be cut
 * cleanly at the frame edges, never drawn over the frame or the neighbouring widgets.
 */
class ClipDemo extends AbstractWidget {
    private static final int WIDTH = 140;
    private static final int PADDING = 2;
    private static final int MILLIS_PER_PIXEL = 30;
    private static final Component TEXT = Component.literal(
            "Clipping: this text is much longer than its frame and must be cut cleanly at both edges.");

    ClipDemo() {
        super(0, 0, WIDTH, GaldurRender.lineHeight() + 2 * PADDING, TEXT);
        this.active = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int innerX = getX() + PADDING;
        int innerY = getY() + PADDING;
        int innerWidth = getWidth() - 2 * PADDING;
        int innerHeight = getHeight() - 2 * PADDING;
        int travel = innerWidth + GaldurRender.textWidth(TEXT);
        int offset = (int) ((Util.getMillis() / MILLIS_PER_PIXEL) % travel);

        GaldurRender.drawFill(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.surface());
        GaldurRender.drawClipped(guiGraphics, innerX, innerY, innerWidth, innerHeight,
                () -> GaldurRender.text(guiGraphics, TEXT, innerX + innerWidth - offset, innerY, GaldurTheme.text(), false));
        GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.ember());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
