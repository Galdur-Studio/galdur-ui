package com.galdurstudio.galdurui.api.layout;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.widget.GaldurPanel;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.resources.ResourceLocation;

/**
 * Layout that wraps a child in a themed background panel. Its size is the child's plus the
 * padding on every side, and the 9-slice background follows its final position and size. Combined
 * with the vanilla layouts, screens need no coordinate maths:
 *
 * <pre>{@code
 * LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
 * // ... column.addChild(...)
 * GaldurPanelLayout panel = new GaldurPanelLayout(column);
 * addRenderableOnly(panel);                     // first, so the background is drawn behind
 * panel.visitWidgets(this::addRenderableWidget);
 * panel.arrangeElements();
 * FrameLayout.centerInRectangle(panel, getRectangle());
 * }</pre>
 *
 * <p>Unless {@link #padding(int)} is set, the padding is read from the theme
 * ({@code spacing.panel_padding}) on every {@link #arrangeElements()}.</p>
 */
public class GaldurPanelLayout extends AbstractLayout implements Renderable {
    private final LayoutElement child;
    private final GaldurPanel background = new GaldurPanel(0, 0, 0, 0);

    /** Fixed padding; empty = follow the theme. */
    private OptionalInt padding = OptionalInt.empty();

    /** @param child panel contents, usually a {@code LinearLayout} */
    public GaldurPanelLayout(LayoutElement child) {
        super(0, 0, 0, 0);
        this.child = child;
    }

    /**
     * Fixes the padding instead of following the theme.
     *
     * @param padding space between the panel edge and the child, in GUI pixels
     * @return this layout
     * @throws IllegalArgumentException if {@code padding} is negative
     */
    public GaldurPanelLayout padding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("GaldurPanelLayout padding must be zero or positive, got "
                    + padding + ". To follow the theme, use useThemePadding().");
        }
        this.padding = OptionalInt.of(padding);
        return this;
    }

    /**
     * Goes back to the theme padding ({@code spacing.panel_padding}).
     *
     * @return this layout
     */
    public GaldurPanelLayout useThemePadding() {
        this.padding = OptionalInt.empty();
        return this;
    }

    /**
     * Replaces the background sprite.
     *
     * @param sprite GUI sprite id, ideally a grayscale 9-slice (tinted by the theme)
     * @return this layout
     */
    public GaldurPanelLayout withSprite(ResourceLocation sprite) {
        background.withSprite(sprite);
        return this;
    }

    /** {@return the effective padding, in GUI pixels} */
    public int getPadding() {
        return padding.orElseGet(GaldurTheme::panelPadding);
    }

    /** {@return the element inside the panel} */
    public LayoutElement getChild() {
        return child;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(child);
    }

    @Override
    public void arrangeElements() {
        // Arrange the child first (if it is a layout) to know its size.
        super.arrangeElements();
        int pad = getPadding();
        child.setPosition(getX() + pad, getY() + pad);
        width = child.getWidth() + 2 * pad;
        height = child.getHeight() + 2 * pad;
        syncBackground();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        syncBackground();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        syncBackground();
    }

    private void syncBackground() {
        background.setBounds(getX(), getY(), width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        background.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
