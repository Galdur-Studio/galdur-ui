package com.galdurstudio.galdurui.internal.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.resources.ResourceLocation;

/**
 * Themed background panel, drawn with a 9-slice sprite.
 *
 * <p>The sprite is grayscale and tinted at render time with the theme {@code background} colour,
 * read every frame.</p>
 *
 * <p><strong>Internal</strong>: only {@link com.galdurstudio.galdurui.api.layout.GaldurPanelLayout}
 * uses it. It exposes coordinates, which the public API avoids: do not depend on it from another
 * mod.</p>
 *
 * <p>Purely decorative: it takes neither clicks nor focus and sits behind the widgets placed on
 * it. Since it implements {@link Renderable}, add it to a screen with
 * {@code addRenderableOnly(...)}, first, so it is drawn behind the rest.</p>
 *
 * <pre>{@code
 * GaldurPanel background = new GaldurPanel(x, y, 220, 140);
 * addRenderableOnly(background);
 * }</pre>
 *
 * <p>To wrap a layout, use {@link com.galdurstudio.galdurui.api.layout.GaldurPanelLayout}, which
 * sizes and places this background by itself.</p>
 *
 * <p>Sprite corners are not stretched. Below twice the border per side (8 px for the shipped
 * sprite), Minecraft shrinks the border to half the size: corners do not overlap, but they are
 * cropped.</p>
 */
public class GaldurPanel implements Renderable {
    private int x;
    private int y;
    private int width;
    private int height;
    private ResourceLocation sprite = GaldurSprites.PANEL;

    /** Whether the panel is drawn. */
    public boolean visible = true;

    /**
     * Creates a panel.
     *
     * @param x      x of the top-left corner
     * @param y      y of the top-left corner
     * @param width  width, in GUI pixels
     * @param height height, in GUI pixels
     */
    public GaldurPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Replaces the background sprite, for instance so a mod can use its own.
     *
     * <p>The sprite is tinted with {@code background} too: provide it in grayscale (white takes
     * exactly the theme colour).</p>
     *
     * @param sprite id of a GUI sprite, ideally 9-slice
     * @return this panel, for chaining
     */
    public GaldurPanel withSprite(ResourceLocation sprite) {
        this.sprite = sprite;
        return this;
    }

    /**
     * Moves and resizes the panel.
     *
     * <p>Useful from {@code Screen#init()}, which is called again on every window size or GUI
     * scale change.</p>
     *
     * @param x      x of the top-left corner
     * @param y      y of the top-left corner
     * @param width  width, in GUI pixels
     * @param height height, in GUI pixels
     */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** {@return the x of the top-left corner} */
    public int getX() {
        return x;
    }

    /** {@return the y of the top-left corner} */
    public int getY() {
        return y;
    }

    /** {@return the panel width, in GUI pixels} */
    public int getWidth() {
        return width;
    }

    /** {@return the panel height, in GUI pixels} */
    public int getHeight() {
        return height;
    }

    /** {@return the x of the panel centre} */
    public int getCenterX() {
        return x + width / 2;
    }

    /** {@return the x of the right edge, exclusive} */
    public int getRight() {
        return x + width;
    }

    /** {@return the y of the bottom edge, exclusive} */
    public int getBottom() {
        return y + height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            GaldurRender.drawNineSlice(guiGraphics, sprite, x, y, width, height, GaldurTheme.background());
        }
    }
}
