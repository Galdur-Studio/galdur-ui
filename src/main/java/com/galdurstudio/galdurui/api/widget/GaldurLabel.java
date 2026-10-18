package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Themed line of text. The widget is exactly the size of its text (or of its maximum width, the
 * text being cut with "…"). It takes no click or focus and plays no sound, but is narrated.
 *
 * <pre>{@code
 * column.addChild(GaldurLabel.of(Component.literal("Title")));
 * GaldurLabel counter = column.addChild(GaldurLabel.muted(Component.literal("Clicks: 0")));
 * counter.setText(Component.literal("Clicks: 1"));  // also updates the width
 * }</pre>
 */
public class GaldurLabel extends AbstractWidget {
    private final IntSupplier color;
    private final int maxWidth;

    /** @param color supplier of the ARGB text colour, called every frame */
    protected GaldurLabel(Component text, IntSupplier color) {
        this(text, color, 0);
    }

    /** @param maxWidth maximum width in pixels, the text being cut with "…"; 0 or less for none */
    protected GaldurLabel(Component text, IntSupplier color, int maxWidth) {
        super(0, 0, fitWidth(text, maxWidth), GaldurRender.lineHeight(), text);
        this.color = color;
        this.maxWidth = maxWidth;
        // Like StringWidget: an inactive widget is neither clickable nor reachable by keyboard.
        this.active = false;
    }

    /** {@return a label in {@code text}, at most {@code maxWidth} pixels wide} */
    public static GaldurLabel of(Component text, int maxWidth) {
        return new GaldurLabel(text, GaldurTheme::text, maxWidth);
    }

    /** {@return a label in the {@code text} colour, sized to its text} */
    public static GaldurLabel of(Component text) {
        return GaldurLabel.of(text, 0);
    }

    /** {@return a secondary label in {@code text_muted}, at most {@code maxWidth} pixels wide} */
    public static GaldurLabel muted(Component text, int maxWidth) {
        return new GaldurLabel(text, GaldurTheme::textMuted, maxWidth);
    }

    /** {@return a label in the {@code text_muted} colour, sized to its text} */
    public static GaldurLabel muted(Component text) {
        return GaldurLabel.muted(text, 0);
    }

    private static int fitWidth(Component text, int maxWidth) {
        if (maxWidth <= 0) {
            return GaldurRender.textWidth(text);
        }
        return Math.min(maxWidth, GaldurRender.textWidth(text));
    }

    /** {@return the text shown} */
    public Component getText() {
        return getMessage();
    }

    /** Changes the text and resizes the widget; re-arrange the layout if the width changed. */
    public void setText(Component text) {
        setMessage(text);
        setWidth(fitWidth(text, maxWidth));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GaldurRender.text(guiGraphics, GaldurRender.truncateText(getMessage(), maxWidth), getX(), getY(), color.getAsInt(), true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
