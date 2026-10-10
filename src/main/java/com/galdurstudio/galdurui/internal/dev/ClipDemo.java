package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Démonstration du découpage de {@link GaldurRender} pour l'écran de test.
 *
 * <p>Un texte bien plus long que le cadre défile en continu de droite à gauche. Il doit être
 * coupé net aux bords du cadre, sans jamais déborder dessus ni sur les widgets voisins : sinon,
 * le découpage est cassé.</p>
 *
 * <p>Réservé au développement : vit dans {@code internal.dev}, comme {@link TestScreen}.</p>
 */
class ClipDemo extends AbstractWidget {
    private static final int WIDTH = 140;
    private static final int PADDING = 2;
    /** Millisecondes pour avancer le texte d'un pixel. */
    private static final int MILLIS_PER_PIXEL = 30;
    private static final Component TEXT = Component.literal(
            "Découpage : ce texte est bien plus long que son cadre et doit être coupé net aux deux bords.");

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
