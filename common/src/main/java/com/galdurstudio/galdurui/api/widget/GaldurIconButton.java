package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Square button without text that shows an icon: a GUI sprite or an item.
 *
 * <p>Same background, states and behaviour as {@link GaldurButton}; only the contents change. The
 * label is not drawn but is required: it is the <strong>tooltip</strong> and the narrated
 * text.</p>
 *
 * <pre>{@code
 * row.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER),
 *         Component.literal("Sort"), button -> sort()));
 * row.addChild(GaldurIconButton.of(ResourceLocation.fromNamespaceAndPath("mymod", "icon/sort"),
 *         Component.literal("Sort"), button -> sort()));
 * }</pre>
 *
 * <p>A sprite is drawn 16 × 16, tinted {@code text} ({@code text_muted} when disabled): provide it
 * in grayscale. An item is drawn as in an inventory and veiled when disabled. The side is
 * {@code spacing.button_height}, to line up with text buttons in the same row.</p>
 */
public class GaldurIconButton extends GaldurButton {
    private static final int ICON_SIZE = GaldurRender.ITEM_SIZE;
    private static final int DISABLED_VEIL_ALPHA = 0xA0;

    @Nullable
    private final ResourceLocation sprite;
    private final ItemStack item;

    /**
     * Prefer {@link #of} or {@link #ofItem}, which also set the label as tooltip.
     *
     * @param sprite sprite to show, or {@code null} to show {@code item}
     * @param item   item shown when there is no sprite; copied
     */
    protected GaldurIconButton(int size, @Nullable ResourceLocation sprite, ItemStack item, Component label,
                               OnPress onPress) {
        super(0, 0, size, size, label, onPress);
        this.sprite = sprite;
        this.item = item.copy();
    }

    private static GaldurIconButton withLabelTooltip(GaldurIconButton button) {
        button.setTooltip(Tooltip.create(button.getMessage()));
        return button;
    }

    /** {@return a button showing a grayscale GUI sprite, at {@code (0, 0)}} */
    public static GaldurIconButton of(ResourceLocation sprite, Component label, OnPress onPress) {
        return withLabelTooltip(new GaldurIconButton(GaldurTheme.buttonHeight(), sprite, ItemStack.EMPTY, label, onPress));
    }

    /** {@return a button showing an item (copied), at {@code (0, 0)}} */
    public static GaldurIconButton ofItem(ItemStack item, Component label, OnPress onPress) {
        return withLabelTooltip(new GaldurIconButton(GaldurTheme.buttonHeight(), null, item, label, onPress));
    }

    /** {@return the button side: an icon button does not widen to its label} */
    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics) {
        int x = getX() + (getWidth() - ICON_SIZE) / 2;
        int y = getY() + (getHeight() - ICON_SIZE) / 2;
        if (sprite != null) {
            GaldurRender.drawNineSlice(guiGraphics, sprite, x, y, ICON_SIZE, ICON_SIZE,
                    active ? GaldurTheme.text() : GaldurTheme.textMuted());
            return;
        }
        GaldurRender.drawItem(guiGraphics, item, x, y);
        if (!active) {
            GaldurRender.dimItem(guiGraphics, x, y, (DISABLED_VEIL_ALPHA << 24) | (GaldurTheme.surface() & 0xFFFFFF));
        }
    }
}
