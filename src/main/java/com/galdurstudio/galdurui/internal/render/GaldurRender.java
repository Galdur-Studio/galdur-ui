package com.galdurstudio.galdurui.internal.render;

import com.galdurstudio.galdurui.internal.client.Client;
//? if >=26.1 {
/*import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2fStack;
*///?} else {
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
//? if >=1.21
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix4f;
import org.joml.Vector3f;
//?}
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Galdur UI drawing layer.
 *
 * <p>This is the <strong>only</strong> class of the library that calls {@link GuiGraphics}. The
 * widgets of {@code api.widget} describe what they want to see (a stretched sprite here, a line of
 * text there) and let this class deal with the game version's rendering API. When that rendering
 * changes (it did in 1.21.6), only this file needs rewriting.</p>
 *
 * <p>Written for Minecraft 1.21.1, with the differences of the other versions in Stonecutter
 * comments ({@code //? if}). Since 26.1, Minecraft records what to draw and batches it itself
 * ({@code GuiGraphics}): each method then simply calls its vanilla counterpart. Where GUI
 * sprites come from before 26.1 is in {@link GuiSprites}.</p>
 */
public final class GaldurRender {
    /**
     * Side of an item icon, in GUI pixels.
     */
    public static final int ITEM_SIZE = 16;

    private GaldurRender() {
    }

    /** Bands a gradient is drawn in: enough to look smooth, few enough to stay cheap. */
    private static final int GRADIENT_BANDS = 12;

    /**
     * Draws a 9-slice sprite tinted with a theme paint: a colour, or a gradient. A gradient is
     * drawn as thin bands, each clipped and tinted with the colour at its middle, so it works with
     * every sprite and every Minecraft version.
     *
     * @param paint colour or gradient, usually {@code GaldurTheme.paint(key)}
     */
    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height,
                                     GaldurPaint paint) {
        if (paint.isSolid()) {
            drawNineSlice(graphics, sprite, x, y, width, height, paint.first());
            return;
        }
        //? if <26.1 {
        // Each vertex takes the colour of the gradient where it is.
        drawNineSliceGrid(graphics, sprite, x, y, width, height, 1, 1, paint);
        //?} else {
        /*boolean vertical = paint.direction() == GaldurPaint.Direction.VERTICAL;
        int length = vertical ? height : width;
        int bands = Math.max(1, Math.min(GRADIENT_BANDS, length));
        for (int band = 0; band < bands; band++) {
            int start = band * length / bands;
            int end = (band + 1) * length / bands;
            int color = paint.at((start + end) / 2F / Math.max(1, length - 1));
            if (vertical) {
                drawClipped(graphics, x, y + start, width, end - start,
                        () -> drawNineSlice(graphics, sprite, x, y, width, height, color));
            } else {
                drawClipped(graphics, x + start, y, end - start, height,
                        () -> drawNineSlice(graphics, sprite, x, y, width, height, color));
            }
        }
        *///?}
    }

    /** Fills a rectangle with a theme paint: a colour, or a gradient in its direction. */
    public static void drawFill(GuiGraphics graphics, int x, int y, int width, int height, GaldurPaint paint) {
        if (paint.isSolid()) {
            drawFill(graphics, x, y, width, height, paint.first());
            return;
        }
        boolean vertical = paint.direction() == GaldurPaint.Direction.VERTICAL;
        int length = vertical ? height : width;
        int steps = paint.colors().size() - 1;
        for (int step = 0; step < steps; step++) {
            int start = step * length / steps;
            int end = (step + 1) * length / steps;
            int from = paint.colors().get(step);
            int to = paint.colors().get(step + 1);
            if (vertical) {
                drawGradient(graphics, x, y + start, width, end - start, from, to);
            } else {
                // Horizontal: one column per pixel, like the colour picker.
                for (int column = start; column < end; column++) {
                    drawFill(graphics, x + column, y, 1, height, paint.at(column / (float) Math.max(1, length - 1)));
                }
            }
        }
    }

    /**
     * Draws a tinted 9-slice sprite.
     *
     * <p>Each sprite pixel is multiplied by {@code argb}: a white pixel takes exactly the requested
     * colour, a gray pixel a darker version. Galdur UI sprites are therefore grayscale, and their
     * colour comes from the theme every frame. The alpha of {@code argb} makes the sprite
     * translucent.</p>
     *
     * <p>The slicing comes from the sprite's {@code .png.mcmeta}: corners keep their size, edges
     * and centre are repeated (tiled) then cropped, never stretched, exactly like
     * {@code GuiGraphics.blitNineSlicedSprite}. If the rectangle is smaller than twice the border,
     * the border is reduced to half the width (or height), as in vanilla.</p>
     *
     * <p>1.21.1 implementation, chosen after measuring with Spark (UI-38): Minecraft draws a
     * 9-slice with one draw call per tile, and changing the tint through
     * {@code setColor} also flushes the buffer on every sprite. Here all tiles go into
     * <strong>a single</strong> buffer, the tint being carried by each vertex
     * ({@code position_tex_color} shader): one draw call per sprite, and no flush. Like
     * {@code blitSprite}, drawing is immediate; blending and depth test stay enabled. A sprite that
     * is not 9-slice ({@code stretch}, {@code tile}) takes the vanilla path.</p>
     *
     * @param graphics render context given by the screen
     * @param sprite   sprite id in the GUI atlas
     * @param x        x of the top-left corner
     * @param y        y of the top-left corner
     * @param width    wanted width, in GUI pixels
     * @param height   wanted height, in GUI pixels
     * @param argb     tint, packed ARGB ({@code 0xFFFFFFFF} = no tint)
     */
    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        drawNineSliceGrid(graphics, sprite, x, y, width, height, 1, 1, argb);
    }

    /**
     * Draws the same tinted 9-slice sprite {@code columns} × {@code rows} times, side by side, in a
     * single draw call: the cell backgrounds of a grid, for instance. Same result as
     * {@code columns × rows} calls to {@link #drawNineSlice}, much cheaper.
     *
     * @param graphics render context given by the screen
     * @param sprite   sprite id in the GUI atlas
     * @param x        x of the top-left corner of the first copy
     * @param y        y of the top-left corner of the first copy
     * @param width    width of one copy, which is also the horizontal step
     * @param height   height of one copy, which is also the vertical step
     * @param columns  copies per row
     * @param rows     number of rows
     * @param argb     tint, packed ARGB
     */
    public static void drawNineSliceGrid(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height,
                                         int columns, int rows, int argb) {
        //? if >=26.1 {
        /*if (width <= 0 || height <= 0 || columns <= 0 || rows <= 0) {
            return;
        }
        *///?} else {
        drawNineSliceGrid(graphics, sprite, x, y, width, height, columns, rows, GaldurPaint.solid(argb));
    }

    /** Before 26.1: the 9-slice grid, each vertex tinted with the paint at its place in its copy. */
    private static void drawNineSliceGrid(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height,
                                          int columns, int rows, GaldurPaint paint) {
        if (width <= 0 || height <= 0 || columns <= 0 || rows <= 0) {
            return;
        }
        int argb = paint.first();
        //?}
        //? if >=26.1 {
        /*// Minecraft tiles the 9-slice, tints each vertex and batches every sprite of the frame.
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + column * width, y + row * height,
                        width, height, argb);
            }
        }
        *///?} else {
        GuiSprites.Sprite resolved = GuiSprites.get(sprite);
        GuiSprites.NineSlice nineSlice = resolved.nineSlice();
        if (nineSlice == null) {
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    drawSpriteVanilla(graphics, sprite, resolved, x + column * width, y + row * height, width, height, argb);
                }
            }
            return;
        }
        int sliceWidth = nineSlice.width();
        int sliceHeight = nineSlice.height();
        int left = Math.min(nineSlice.left(), width / 2);
        int right = Math.min(nineSlice.right(), width / 2);
        int top = Math.min(nineSlice.top(), height / 2);
        int bottom = Math.min(nineSlice.bottom(), height / 2);

        // 9-slice columns and rows: area on screen (start, size) and source area in the sprite.
        int[] areaX = {0, left, width - right};
        int[] areaW = {left, width - left - right, right};
        int[] srcX = {0, left, sliceWidth - right};
        int[] srcW = {left, sliceWidth - left - right, right};
        int[] areaY = {0, top, height - bottom};
        int[] areaH = {top, height - top - bottom, bottom};
        int[] srcY = {0, top, sliceHeight - bottom};
        int[] srcH = {top, sliceHeight - top - bottom, bottom};

        Matrix4f pose = graphics.pose().last().pose();
        BufferBuilder buffer = beginQuads();
        for (int copyRow = 0; copyRow < rows; copyRow++) {
            for (int copyColumn = 0; copyColumn < columns; copyColumn++) {
                int originX = x + copyColumn * width;
                int originY = y + copyRow * height;
                for (int row = 0; row < 3; row++) {
                    for (int column = 0; column < 3; column++) {
                        addTiled(buffer, pose, resolved, paint, originX, originY, width, height,
                                originX + areaX[column], originY + areaY[row], areaW[column], areaH[row],
                                srcX[column], srcY[row], srcW[column], srcH[row], sliceWidth, sliceHeight);
                    }
                }
            }
        }
        RenderSystem.setShaderTexture(0, resolved.texture());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        draw(buffer);
        //?}
    }

    //? if <26.1 {
    /** {@return a buffer of textured, tinted quads ({@code position_tex_color})} */
    private static BufferBuilder beginQuads() {
        //? if >=1.21 {
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        //?} else {
        /*BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        return buffer;
        *///?}
    }

    /** Adds one vertex to a {@link #beginQuads()} buffer. */
    private static void vertex(BufferBuilder buffer, Matrix4f pose, float x, float y, float u, float v, int argb) {
        //? if >=1.21 {
        buffer.addVertex(pose, x, y, 0).setUv(u, v).setColor(argb);
        //?} else {
        /*buffer.vertex(pose, x, y, 0).uv(u, v).color(argb).endVertex();
        *///?}
    }

    /** Sends a {@link #beginQuads()} buffer to the GPU; the shader and texture must already be set. */
    private static void draw(BufferBuilder buffer) {
        //? if >=1.21 {
        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        //?} else {
        /*BufferUploader.drawWithShader(buffer.end());
        *///?}
    }
    //?}

    /**
     * Runs {@code draw} while batching text and fills ({@link #text}, {@link #drawFill},
     * {@link #drawItemCount}...): they reach the GPU in a single submission at the end, instead of
     * one per call. Useful for many small texts, like the amounts of a grid.
     *
     * <p>What is drawn in {@code draw} therefore ends up <strong>above</strong> the sprites and
     * items drawn meanwhile: only put there elements that sit on top of everything else. Up to 1.21.1:
     * {@code GuiGraphics.drawManaged(Runnable)}. Since 26.1 Minecraft batches everything itself and
     * {@code draw} simply runs.</p>
     *
     * @param graphics render context given by the screen
     * @param draw     the drawing to batch
     */
    @SuppressWarnings("deprecation")
    public static void drawBatched(GuiGraphics graphics, Runnable draw) {
        //? if >=26.1 {
        /*draw.run();
        *///?} else {
        graphics.drawManaged(draw);
        //?}
    }

    //? if <26.1 {
    /**
     * Adds to the buffer the tiles covering a screen area by repeating a sprite area; the last
     * tile of each row and column is cropped.
     *
     * @param areaX        x of the area to cover
     * @param areaY        y of the area to cover
     * @param areaW        width of the area to cover; nothing is added when zero
     * @param areaH        height of the area to cover
     * @param srcX         x of the source area, in sprite pixels
     * @param srcY         y of the source area
     * @param srcW         width of the source area; nothing is added when zero
     * @param srcH         height of the source area
     * @param spriteWidth  sprite width in pixels (from the {@code .mcmeta})
     * @param spriteHeight sprite height in pixels
     */
    /** {@return the colour of the paint at a point of a sprite of the given size} */
    private static int colorAt(GaldurPaint paint, float x, float y, int width, int height) {
        if (paint.isSolid()) {
            return paint.first();
        }
        return paint.direction() == GaldurPaint.Direction.VERTICAL
                ? paint.at(y / Math.max(1, height)) : paint.at(x / Math.max(1, width));
    }

    private static void addTiled(BufferBuilder buffer, Matrix4f pose, GuiSprites.Sprite sprite, GaldurPaint paint,
                                 int originX, int originY, int fullWidth, int fullHeight,
                                 int areaX, int areaY, int areaW, int areaH,
                                 int srcX, int srcY, int srcW, int srcH, int spriteWidth, int spriteHeight) {
        if (areaW <= 0 || areaH <= 0 || srcW <= 0 || srcH <= 0) {
            return;
        }
        for (int dy = 0; dy < areaH; dy += srcH) {
            int tileH = Math.min(srcH, areaH - dy);
            float v0 = sprite.v((float) srcY / spriteHeight);
            float v1 = sprite.v((float) (srcY + tileH) / spriteHeight);
            for (int dx = 0; dx < areaW; dx += srcW) {
                int tileW = Math.min(srcW, areaW - dx);
                float u0 = sprite.u((float) srcX / spriteWidth);
                float u1 = sprite.u((float) (srcX + tileW) / spriteWidth);
                float x0 = areaX + dx;
                float y0 = areaY + dy;
                float x1 = x0 + tileW;
                float y1 = y0 + tileH;
                vertex(buffer, pose, x0, y0, u0, v0, colorAt(paint, x0 - originX, y0 - originY, fullWidth, fullHeight));
                vertex(buffer, pose, x0, y1, u0, v1, colorAt(paint, x0 - originX, y1 - originY, fullWidth, fullHeight));
                vertex(buffer, pose, x1, y1, u1, v1, colorAt(paint, x1 - originX, y1 - originY, fullWidth, fullHeight));
                vertex(buffer, pose, x1, y0, u1, v0, colorAt(paint, x1 - originX, y0 - originY, fullWidth, fullHeight));
            }
        }
    }

    /**
     * Vanilla path, for a sprite that is not 9-slice: {@code GuiGraphics.setColor} (which flushes
     * the pending buffer), the same state as {@code AbstractButton.renderWidget}, then
     * {@code blitSprite}. The colour is reset to white afterwards.
     */
    private static void drawSpriteVanilla(GuiGraphics graphics, ResourceLocation sprite, GuiSprites.Sprite resolved,
                                          int x, int y, int width, int height, int argb) {
        graphics.setColor(channel(argb, 16), channel(argb, 8), channel(argb, 0), channel(argb, 24));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        //? if >=1.20.2 {
        graphics.blitSprite(sprite, x, y, width, height);
        //?} else {
        /*// No GUI atlas: the sprite is a whole texture, stretched over the area.
        graphics.blit(resolved.texture(), x, y, width, height, 0, 0, 1, 1, 1, 1);
        *///?}
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    //?}

    /**
     * Draws the {@code width} x {@code height} part of a whole texture at {@code u, v}, with transparency.
     *
     * @param textureWidth  width of the texture file, in pixels
     * @param textureHeight height of the texture file, in pixels
     */
    public static void drawTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width,
                                   int height, int textureWidth, int textureHeight) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?} else {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //?}
    }

    /**
     * Draws the outline of a rectangle, without filling it.
     *
     * <p>The line is 1 GUI pixel wide and stays inside the given area: the outline never spills
     * onto neighbouring pixels. The alpha of {@code argb} is respected, allowing a translucent line
     * that does not hide what it surrounds. No render state is changed.</p>
     *
     * <p>Mostly used by the layout debug mode, but usable for any frame.</p>
     *
     * @param graphics render context given by the screen
     * @param x        x of the top-left corner
     * @param y        y of the top-left corner
     * @param width    width, in GUI pixels
     * @param height   height, in GUI pixels
     * @param argb     line colour, packed ARGB
     */
    public static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int argb) {
        //? if >=26.1 {
        /*graphics.outline(x, y, width, height, argb);
        *///?} else {
        graphics.renderOutline(x, y, width, height, argb);
        //?}
    }

    /**
     * Fills a rectangle with a flat colour.
     *
     * <p>The alpha of {@code argb} is respected: a translucent colour shows what is below. No
     * render state is changed.</p>
     *
     * @param graphics render context given by the screen
     * @param x        x of the top-left corner
     * @param y        y of the top-left corner
     * @param width    width, in GUI pixels
     * @param height   height, in GUI pixels
     * @param argb     fill colour, packed ARGB
     */
    public static void drawFill(GuiGraphics graphics, int x, int y, int width, int height, int argb) {
        graphics.fill(x, y, x + width, y + height, argb);
    }

    /**
     * Fills a rectangle with a vertical gradient: {@code topArgb} on the first row, {@code bottomArgb}
     * on the last. No render state is changed.
     *
     * @param graphics   render context given by the screen
     * @param x          x of the top-left corner
     * @param y          y of the top-left corner
     * @param width      width, in GUI pixels
     * @param height     height, in GUI pixels
     * @param topArgb    colour at the top, packed ARGB
     * @param bottomArgb colour at the bottom, packed ARGB
     */
    public static void drawGradient(GuiGraphics graphics, int x, int y, int width, int height, int topArgb, int bottomArgb) {
        graphics.fillGradient(x, y, x + width, y + height, topArgb, bottomArgb);
    }

    /**
     * Starts clipping: until the matching {@link #popClip(GuiGraphics)}, only pixels inside this
     * rectangle are drawn, the rest is cut.
     *
     * <p>Clips stack: a clip opened inside another keeps only the intersection of both (a list in
     * a tab in a panel overflows none of the three). Every {@code pushClip} must be followed by its
     * {@code popClip}, even when drawing throws, or the rest of the screen would stay clipped:
     * {@link #drawClipped(GuiGraphics, int, int, int, int, Runnable)} takes care of it.</p>
     *
     * <p>1.21.1 implementation: {@code enableScissor}, which
     * manages the stack and flushes the pending buffer. The rectangle is in screen coordinates (GUI
     * pixels) and Minecraft 1.21.1 does not apply the {@code graphics} pose to it: it is done here,
     * so a clipped area follows its content when it is moved (by {@link #drawOffset} during an
     * animation, for instance). A negative width or height is clamped to zero, which lets nothing
     * through.</p>
     *
     * @param graphics render context given by the screen
     * @param x        x of the top-left corner of the visible area
     * @param y        y of the top-left corner of the visible area
     * @param width    width of the visible area, in GUI pixels
     * @param height   height of the visible area, in GUI pixels
     */
    public static void pushClip(GuiGraphics graphics, int x, int y, int width, int height) {
        //? if >=26.1 {
        /*// Since 1.21.6 Minecraft applies the pose to the clip itself.
        graphics.enableScissor(x, y, x + Math.max(0, width), y + Math.max(0, height));
        *///?} else {
        Matrix4f pose = graphics.pose().last().pose();
        Vector3f a = pose.transformPosition(x, y, 0, new Vector3f());
        Vector3f b = pose.transformPosition(x + Math.max(0, width), y + Math.max(0, height), 0, new Vector3f());
        graphics.enableScissor(Math.round(Math.min(a.x, b.x)), Math.round(Math.min(a.y, b.y)),
                Math.round(Math.max(a.x, b.x)), Math.round(Math.max(a.y, b.y)));
        //?}
    }

    /**
     * Runs {@code draw} offset by {@code (dx, dy)} GUI pixels, then restores the position, even if
     * drawing throws. Used by movement animations: everything {@code draw} draws (sprites, text,
     * items, clipped areas) moves as one block.
     *
     * <p>Only the drawing moves: widget click areas stay where they are. 1.21.1 implementation:
     * {@code pose().pushPose()}, {@code translate}, {@code popPose()}.</p>
     *
     * @param graphics render context given by the screen
     * @param dx       horizontal offset, in GUI pixels
     * @param dy       vertical offset, in GUI pixels
     * @param draw     the drawing to offset
     */
    public static void drawOffset(GuiGraphics graphics, float dx, float dy, Runnable draw) {
        if (dx == 0 && dy == 0) {
            draw.run();
            return;
        }
        //? if >=26.1 {
        /*Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        try {
            pose.translate(dx, dy);
            draw.run();
        } finally {
            pose.popMatrix();
        }
        *///?} else {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(dx, dy, 0);
            draw.run();
        } finally {
            pose.popPose();
        }
        //?}
    }

    /**
     * Ends the last clip opened by {@link #pushClip(GuiGraphics, int, int, int, int)} and restores
     * the previous one (or no clip at all if there was only one).
     *
     * <p>Implementation: {@code disableScissor()}. Calling this without an open
     * clip is a programming error: Minecraft then throws an {@link IllegalStateException}.</p>
     *
     * @param graphics render context given by the screen
     */
    public static void popClip(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    /**
     * Runs {@code draw} with an active clip, then ends it in every case, even if drawing throws.
     * Prefer this to a hand-written {@code pushClip} / {@code popClip}.
     *
     * <pre>{@code
     * GaldurRender.drawClipped(graphics, getX(), getY(), getWidth(), getHeight(), () -> {
     *     for (Row row : visibleRows) {
     *         row.render(graphics, ...);
     *     }
     * });
     * }</pre>
     *
     * @param graphics render context given by the screen
     * @param x        x of the top-left corner of the visible area
     * @param y        y of the top-left corner of the visible area
     * @param width    width of the visible area, in GUI pixels
     * @param height   height of the visible area, in GUI pixels
     * @param draw     the drawing to clip
     */
    public static void drawClipped(GuiGraphics graphics, int x, int y, int width, int height, Runnable draw) {
        pushClip(graphics, x, y, width, height);
        try {
            draw.run();
        } finally {
            popClip(graphics);
        }
    }

    /**
     * Draws an item icon, with its durability bar if any, but without its amount: see
     * {@link #drawItemCount(GuiGraphics, String, int, int, int)}.
     *
     * <p>Implementation: {@code renderItem} then {@code renderItemDecorations} with an empty text
     * ({@code item} and {@code itemDecorations} since 26.1), which draws durability and cooldown
     * without the vanilla count.</p>
     *
     * @param graphics render context given by the screen
     * @param stack    the item; nothing is drawn when empty
     * @param x        x of the icon's top-left corner (16 × 16)
     * @param y        y of the icon's top-left corner
     */
    public static void drawItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        //? if >=26.1 {
        /*graphics.item(stack, x, y);
        graphics.itemDecorations(font(), stack, x, y, "");
        *///?} else {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font(), stack, x, y, "");
        //?}
    }

    /**
     * Draws several items at once, each like {@link #drawItem} (icon, durability, cooldown, no
     * amount): the cells of a grid, for instance.
     *
     * <p>Minecraft flushes its buffer after <strong>every</strong> item, because lighting differs
     * between flat items (ingots, tools) and 3D items (blocks). Here items are drawn in two passes,
     * all 3D items then all flat items, with one flush per pass; durability bars and cooldowns are
     * then batched into a single submission. Measured with Spark (UI-38): item drawing was the
     * terminal's top cost.</p>
     *
     * <p>1.21.1 implementation: follows {@code GuiGraphics.renderItem} (model, offset, scale 16,
     * flat or 3D lighting, {@code ItemRenderer.render}), then {@code renderItemDecorations} inside
     * {@link #drawBatched}.</p>
     *
     * @param graphics render context given by the screen
     * @param stacks   the items; empty stacks are skipped
     * @param xs       x of each item's top-left corner
     * @param ys       y of each item's top-left corner
     * @param count    number of items to take from the three arrays
     */
    public static void drawItems(GuiGraphics graphics, ItemStack[] stacks, int[] xs, int[] ys, int count) {
        //? if >=26.1 {
        /*// Since 1.21.6 Minecraft renders GUI items into an atlas and batches them itself.
        for (int index = 0; index < count; index++) {
            drawItem(graphics, stacks[index], xs[index], ys[index]);
        }
        *///?} else {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel[] models = new BakedModel[count];
        boolean anyFlat = false;
        boolean anyBlockLit = false;
        for (int index = 0; index < count; index++) {
            if (!stacks[index].isEmpty()) {
                models[index] = itemRenderer.getModel(stacks[index], minecraft.level, minecraft.player, 0);
                if (models[index].usesBlockLight()) {
                    anyBlockLit = true;
                } else {
                    anyFlat = true;
                }
            }
        }
        if (anyBlockLit) {
            drawItemPass(graphics, itemRenderer, stacks, xs, ys, models, count, true);
        }
        if (anyFlat) {
            Lighting.setupForFlatItems();
            try {
                drawItemPass(graphics, itemRenderer, stacks, xs, ys, models, count, false);
            } finally {
                Lighting.setupFor3DItems();
            }
        }
        drawBatched(graphics, () -> {
            for (int index = 0; index < count; index++) {
                if (models[index] != null) {
                    graphics.renderItemDecorations(font(), stacks[index], xs[index], ys[index], "");
                }
            }
        });
        //?}
    }

    //? if <26.1 {
    /** Draws the items of a single lighting type, then flushes the buffer once. */
    private static void drawItemPass(GuiGraphics graphics, ItemRenderer itemRenderer, ItemStack[] stacks, int[] xs,
                                     int[] ys, BakedModel[] models, int count, boolean blockLight) {
        PoseStack pose = graphics.pose();
        for (int index = 0; index < count; index++) {
            BakedModel model = models[index];
            if (model == null || model.usesBlockLight() != blockLight) {
                continue;
            }
            pose.pushPose();
            try {
                pose.translate(xs[index] + 8, ys[index] + 8, 150);
                pose.scale(16.0F, -16.0F, 16.0F);
                itemRenderer.render(stacks[index], ItemDisplayContext.GUI, false, pose, graphics.bufferSource(),
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            } finally {
                pose.popPose();
            }
        }
        graphics.flush();
    }
    //?}

    /**
     * Veils an already drawn item icon, for instance to show it disabled.
     *
     * <p>1.21.1 implementation: items are drawn in front of the GUI ({@code z} + 150); a plain fill
     * would end up behind them. The {@code graphics} pose is therefore moved forward ({@code z} +
     * 200, like the amount) for the fill, then restored.</p>
     *
     * @param graphics render context given by the screen
     * @param x        x of the icon's top-left corner (16 × 16)
     * @param y        y of the icon's top-left corner
     * @param argb     veil colour, packed ARGB; its alpha sets the opacity
     */
    public static void dimItem(GuiGraphics graphics, int x, int y, int argb) {
        //? if >=26.1 {
        /*// Since 1.21.6 what is drawn later goes on top: no depth to set.
        graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, argb);
        *///?} else {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 200.0F);
        graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, argb);
        pose.popPose();
        //?}
    }

    /**
     * Draws an amount in the bottom-right corner of an item icon, on top of the icon.
     *
     * <p>A text that fits in the icon's 16 pixels ("64") is drawn at normal size, where the vanilla
     * count goes. A wider text ("1,2k") is drawn at half size to stay inside the cell.</p>
     *
     * <p>1.21.1 implementation: the {@code graphics} pose is moved forward ({@code z} + 200, as in
     * vanilla) to pass in front of the item, then restored.</p>
     *
     * @param graphics render context given by the screen
     * @param text     the text to draw, usually {@code GaldurFormat.amount(...)}
     * @param x        x of the icon's top-left corner
     * @param y        y of the icon's top-left corner
     * @param argb     text colour, packed ARGB
     */
    public static void drawItemCount(GuiGraphics graphics, String text, int x, int y, int argb) {
        Font font = font();
        int width = font.width(text);
        //? if >=26.1 {
        /*Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        if (width <= ITEM_SIZE) {
            graphics.text(font, text, x + ITEM_SIZE + 1 - width, y + 9, argb, true);
        } else {
            pose.translate(x + ITEM_SIZE, y + ITEM_SIZE);
            pose.scale(0.5F, 0.5F);
            graphics.text(font, text, -width, -font.lineHeight + 1, argb, true);
        }
        pose.popMatrix();
        *///?} else {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        if (width <= ITEM_SIZE) {
            pose.translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, text, x + ITEM_SIZE + 1 - width, y + 9, argb, true);
        } else {
            pose.translate(x + ITEM_SIZE, y + ITEM_SIZE, 200.0F);
            pose.scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(font, text, -width, -font.lineHeight + 1, argb, true);
        }
        pose.popPose();
        //?}
    }

    /**
     * Asks the current screen to show an item's tooltip at the end of its render, above every
     * widget: name, description, enchantments... as in an inventory.
     *
     * <p>Call during rendering, on every frame where the mouse hovers the item. The {@code extra}
     * lines are appended (the exact amount, for instance).</p>
     *
     * <p>Implementation: the item's lines ({@code Screen.getTooltipFromItem}), then
     * {@code setTooltipForNextRenderPass} on the screen, or {@code setTooltipForNextFrame} on
     * {@code graphics} since 26.1. Image tooltips (bundle contents) are not shown.</p>
     *
     * @param graphics render context given by the screen
     * @param stack    the item; no effect when empty or when no screen is open
     * @param extra    lines added after the item's own
     * @param mouseX   mouse x, where the tooltip goes
     * @param mouseY   mouse y
     */
    public static void showItemTooltip(GuiGraphics graphics, ItemStack stack, List<Component> extra,
                                       int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (stack.isEmpty() || Client.screen() == null) {
            return;
        }
        List<Component> lines = new ArrayList<>(net.minecraft.client.gui.screens.Screen.getTooltipFromItem(minecraft, stack));
        lines.addAll(extra);
        //? if >=26.1 {
        /*graphics.setTooltipForNextFrame(font(), lines, Optional.<TooltipComponent>empty(), mouseX, mouseY);
        *///?} else {
        List<FormattedCharSequence> sequences = new ArrayList<>(lines.size());
        for (Component line : lines) {
            sequences.add(line.getVisualOrderText());
        }
        Client.screen().setTooltipForNextRenderPass(sequences);
        //?}
    }

    /**
     * {@return the component of {@code argb} shifted by {@code shift} bits, between 0 and 1}
     */
    private static float channel(int argb, int shift) {
        return ((argb >>> shift) & 0xFF) / 255.0F;
    }

    /**
     * Draws a left-aligned line of text.
     *
     * <p>The library's only call to {@code drawString} ({@code text} since 26.1): the other text
     * methods go through here.</p>
     *
     * @param graphics render context
     * @param text     text to draw
     * @param x        x of the first character
     * @param y        y of the top of the line
     * @param argb     text colour, packed ARGB
     * @param shadow   {@code true} to draw the drop shadow
     */
    public static void text(GuiGraphics graphics, Component text, int x, int y, int argb, boolean shadow) {
        //? if >=26.1 {
        /*graphics.text(font(), text, x, y, argb, shadow);
        *///?} else {
        graphics.drawString(font(), text, x, y, argb, shadow);
        //?}
    }

    public static void text(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int argb, boolean shadow) {
        //? if >=26.1 {
        /*graphics.text(font(), text, x, y, argb, shadow);
        *///?} else {
        graphics.drawString(font(), text, x, y, argb, shadow);
        //?}
    }

    /**
     * Draws a line of text with drop shadow, centred horizontally.
     *
     * @param graphics render context
     * @param text     text to draw
     * @param centerX  x of the line centre
     * @param y        y of the top of the line
     * @param argb     text colour, packed ARGB
     */
    public static void drawCenteredText(GuiGraphics graphics, Component text, int centerX, int y, int argb) {
        text(graphics, text, centerX - textWidth(text) / 2, y, argb, true);
    }

    public static FormattedCharSequence truncateText(Component text, int maxWidth) {
        int textWidth = font().width(text);
        if (maxWidth <= 0 || textWidth <= maxWidth) {
            return Language.getInstance().getVisualOrder(text);
        }
        int truncatedWidth = maxWidth - font().width(CommonComponents.ELLIPSIS);
        FormattedText truncatedText = font().substrByWidth(text, truncatedWidth);
        return Language.getInstance().getVisualOrder(FormattedText.composite(truncatedText, CommonComponents.ELLIPSIS));
    }

    /**
     * {@return the width of {@code text} in GUI pixels}
     *
     * <p>Lets widgets size themselves without touching {@link Font}.</p>
     *
     * @param text text to measure
     */
    public static int textWidth(Component text) {
        return font().width(text);
    }

    /**
     * {@return the line height of the current font, in GUI pixels}
     */
    public static int lineHeight() {
        return font().lineHeight;
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }
}
