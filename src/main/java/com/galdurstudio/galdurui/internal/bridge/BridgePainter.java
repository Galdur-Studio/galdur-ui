package com.galdurstudio.galdurui.internal.bridge;

import net.minecraft.client.gui.screens.Screen;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.mixin.AbstractContainerScreenAccessor;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/**
 * Draws the vanilla widgets of a restyled screen ({@link GaldurBridge}) with the Galdur sprites and
 * theme. The mixins of {@code internal.mixin} call it in place of the vanilla background sprites;
 * Minecraft still draws the text.
 */
public final class BridgePainter {
    /** Vanilla colour of the container labels (title, "Inventory"). */
    private static final int VANILLA_LABEL = 0x404040;
    private static final int SLOT_FRAME = 18;

    /** {@code true} while the labels of a container screen on a Galdur panel are drawn. */
    private static boolean themedLabels;
    /** Last container screen whose background was a Galdur panel, so its labels follow the theme. */
    private static AbstractContainerScreen<?> panelScreen;

    private BridgePainter() {
    }

    /** Answers for the screen shown, computed once per screen and per change of the rules. */
    private static Screen cachedScreen;
    private static int cachedVersion = -1;
    private static final Boolean[] CACHE = new Boolean[GaldurBridge.Element.values().length];

    /** {@return {@code true} if {@code element} of the Galdur look applies to the screen shown} */
    public static boolean active(GaldurBridge.Element element) {
        Screen screen = Client.screen();
        int version = GaldurBridge.rulesVersion();
        if (screen != cachedScreen || version != cachedVersion) {
            cachedScreen = screen;
            cachedVersion = version;
            java.util.Arrays.fill(CACHE, null);
        }
        Boolean value = CACHE[element.ordinal()];
        if (value == null) {
            value = GaldurBridge.applies(screen, element);
            CACHE[element.ordinal()] = value;
        }
        return value;
    }

    /** {@return {@code true} if the Galdur panel replaces the background of the screen shown} */
    public static boolean replaceBackground() {
        return active(GaldurBridge.Element.CONTAINER_BACKGROUND);
    }

    /** Button background: accent colour, lighter when hovered or focused, surface when disabled. */
    public static void button(GuiGraphics graphics, AbstractWidget button, int x, int y, int width, int height) {
        boolean highlighted = button.isHoveredOrFocused();
        if (!button.active) {
            GaldurRender.drawNineSlice(graphics, GaldurSprites.BUTTON_DISABLED, x, y, width, height, GaldurTheme.paint(GaldurTheme.BUTTON_DISABLED));
        } else {
            GaldurRender.drawNineSlice(graphics, highlighted ? GaldurSprites.BUTTON_HOVER : GaldurSprites.BUTTON, x, y,
                    width, height, highlighted ? GaldurTheme.paint(GaldurTheme.BUTTON_HOVER) : GaldurTheme.paint(GaldurTheme.BUTTON));
        }
    }

    /** Checkbox: a sunken box, filled with the accent colour when ticked. */
    public static void checkbox(GuiGraphics graphics, AbstractWidget box, boolean selected, int x, int y, int width, int height) {
        GaldurRender.drawNineSlice(graphics, GaldurSprites.FIELD, x, y, width, height, GaldurTheme.paint(GaldurTheme.CHECKBOX));
        if (box.active && box.isHoveredOrFocused()) {
            GaldurRender.drawOutline(graphics, x, y, width, height, GaldurTheme.color(GaldurTheme.OUTLINE));
        }
        if (selected) {
            int inset = Math.max(2, Math.min(width, height) / 4);
            GaldurRender.drawFill(graphics, x + inset, y + inset, width - 2 * inset, height - 2 * inset,
                    box.active ? GaldurTheme.color(GaldurTheme.CHECKBOX_MARK) : GaldurTheme.textMuted());
        }
    }

    /** Text field background: a sunken area, outlined when focused or hovered. */
    public static void field(GuiGraphics graphics, AbstractWidget field, int x, int y, int width, int height) {
        GaldurRender.drawNineSlice(graphics, GaldurSprites.FIELD, x, y, width, height, GaldurTheme.paint(GaldurTheme.FIELD));
        if (field.active && field.isHoveredOrFocused()) {
            GaldurRender.drawOutline(graphics, x, y, width, height, GaldurTheme.color(GaldurTheme.OUTLINE));
        }
    }

    /** Slider track: the same sunken area as a field. */
    public static void sliderTrack(GuiGraphics graphics, AbstractWidget slider, int x, int y, int width, int height) {
        GaldurRender.drawNineSlice(graphics, GaldurSprites.FIELD, x, y, width, height, GaldurTheme.paint(GaldurTheme.SLIDER));
    }

    /** Slider handle: a small button. */
    public static void sliderHandle(GuiGraphics graphics, AbstractWidget slider, int x, int y, int width, int height) {
        GaldurRender.drawNineSlice(graphics, slider.isHoveredOrFocused() ? GaldurSprites.BUTTON_HOVER : GaldurSprites.BUTTON,
                x, y, width, height, GaldurTheme.paint(GaldurTheme.SLIDER_HANDLE));
    }

    /** Galdur panel and a frame for each active slot, in place of a container texture. */
    public static void containerBackground(GuiGraphics graphics, AbstractContainerScreen<?> screen,
                                           AbstractContainerScreenAccessor layout) {
        panelScreen = screen;
        int left = layout.galdurui$leftPos();
        int top = layout.galdurui$topPos();
        GaldurRender.drawNineSlice(graphics, GaldurSprites.PANEL, left, top, layout.galdurui$imageWidth(),
                layout.galdurui$imageHeight(), GaldurTheme.paint(GaldurTheme.PANEL));
        for (Slot slot : screen.getMenu().slots) {
            if (slot.isActive()) {
                GaldurRender.drawNineSlice(graphics, GaldurSprites.SLOT, left + slot.x - 1, top + slot.y - 1,
                        SLOT_FRAME, SLOT_FRAME, GaldurTheme.paint(GaldurTheme.SLOT));
            }
        }
    }

    /**
     * {@return {@code true} if this screen's labels sit on a Galdur panel: restyled with
     * {@code Background.REPLACE}, or drawing {@code GaldurBridge.drawContainerBackground} itself}
     */
    public static boolean onGaldurPanel(AbstractContainerScreen<?> screen) {
        return active(GaldurBridge.Element.LABELS) || panelScreen == screen;
    }

    /** Runs {@code labels} with the vanilla label colour turned into the theme text colour. */
    public static void themedLabels(Runnable labels) {
        themedLabels = true;
        try {
            labels.run();
        } finally {
            themedLabels = false;
        }
    }

    /** {@return the colour to draw a text with: the theme text colour for vanilla container labels} */
    public static int labelColor(int argb) {
        return themedLabels && (argb & 0xFFFFFF) == VANILLA_LABEL ? GaldurTheme.color(GaldurTheme.TITLE) : argb;
    }
}
