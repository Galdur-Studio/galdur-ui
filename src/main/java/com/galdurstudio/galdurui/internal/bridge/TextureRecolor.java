package com.galdurstudio.galdurui.internal.bridge;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.inventory.Slot;

/**
 * Vanilla screen textures turned into decorations for a Galdur panel. The grey background, the panel
 * bevels and the slots vanish; light greys (arrows, tubes, the anvil...) take the theme's muted text
 * colour, dark greys a faint version of it, and coloured pixels stay. The shapes are the texture's
 * own, so they stay exact, resource packs included. Built once, and again when the theme changes or
 * the resources reload.
 */
public final class TextureRecolor {
    // The colours of Minecraft's container textures, ARGB.
    private static final int BACKGROUND = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int OUTLINE = 0xFF000000;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int SLOT_EDGE = 0xFF373737;
    /** The panel's own bevel, around the edge of the background. */
    private static final int BORDER = 4;
    /** Alpha of the dark greys (outlines, shading). */
    private static final int FAINT = 0x60;
    /** Under the anvil's text field: never seen in game. */
    private static final int HIDDEN = 0xFFFF0000;
    /** Smallest side of a well (a scroll bar groove, the stonecutter's recipe list). */
    private static final int WELL_SIZE = 10;

    /**
     * A recoloured texture: where it is, its size, the slots drawn larger (result slots), and the wells
     * to draw as Galdur fields ({@code x, y, width, height} in the texture).
     */
    record Result(ResourceLocation id, int textureWidth, int textureHeight, List<Integer> largeSlots,
                  List<int[]> wells) {
    }

    private static final Map<String, Optional<Result>> CACHE = new HashMap<>();
    private static long cachedColors;
    private static int counter;

    private TextureRecolor() {
    }

    /**
     * {@return the decorations of a screen's background texture; empty if it cannot be read} The part
     * outside the background (before 1.20.2: progress bars, buttons) is recoloured like a sprite.
     */
    static Optional<Result> background(ResourceLocation source, AbstractContainerScreen<?> screen, int width, int height) {
        String key = source + "#" + screen.getClass().getName() + "#" + width + "x" + height;
        return cached(key, () -> build(source, screen, width, height));
    }

    /** {@return a GUI sprite of a screen (progress bars, bubbles...) recoloured; empty if it cannot be read} */
    static Optional<Result> sprite(ResourceLocation sprite) {
        ResourceLocation file = GaldurUI.id(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
        return cached(file.toString(), () -> build(file, null, 0, 0));
    }

    private static Optional<Result> cached(String key, java.util.function.Supplier<Optional<Result>> build) {
        long colors = ((long) GaldurTheme.textMuted() << 32) | (GaldurTheme.accent() & 0xFFFFFFFFL);
        if (colors != cachedColors) {
            clear();
            cachedColors = colors;
        }
        Optional<Result> result = CACHE.get(key);
        if (result == null) {
            result = build.get();
            CACHE.put(key, result);
        }
        return result;
    }

    /** Forgets every texture, after a resource reload or a theme change. */
    public static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        for (Optional<Result> result : CACHE.values()) {
            result.ifPresent(r -> minecraft.getTextureManager().release(r.id()));
        }
        CACHE.clear();
    }

    private static Optional<Result> build(ResourceLocation source, AbstractContainerScreen<?> screen, int width,
                                          int height) {
        Minecraft minecraft = Minecraft.getInstance();
        Optional<Resource> resource = minecraft.getResourceManager().getResource(source);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        int muted = GaldurTheme.textMuted();
        int accent = GaldurTheme.accent();
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            int textureWidth = image.getWidth();
            int textureHeight = image.getHeight();
            boolean[][] slotArea = new boolean[Math.max(width, 0)][Math.max(height, 0)];
            List<Integer> largeSlots = new ArrayList<>();
            List<Slot> slots = screen == null ? List.of() : screen.getMenu().slots;
            for (int index = 0; index < slots.size(); index++) {
                Slot slot = slots.get(index);
                // A result slot: a 26 x 26 frame around the item, its corner in the slot edge colour.
                boolean large = inside(slot.x - 5, slot.y - 5, width, height)
                        && argb(image, slot.x - 5, slot.y - 5) == SLOT_EDGE
                        && argb(image, slot.x - 1, slot.y - 1) != SLOT_EDGE;
                if (large) {
                    largeSlots.add(index);
                }
                int margin = large ? 5 : 1;
                int size = large ? 26 : 18;
                for (int x = slot.x - margin; x < slot.x - margin + size; x++) {
                    for (int y = slot.y - margin; y < slot.y - margin + size; y++) {
                        if (inside(x, y, width, height)) {
                            slotArea[x][y] = true;
                        }
                    }
                }
            }
            List<int[]> wells = wells(image, slotArea, width, height);
            boolean[][] wellArea = new boolean[Math.max(width, 0)][Math.max(height, 0)];
            for (int[] well : wells) {
                for (int x = well[0]; x < well[0] + well[2]; x++) {
                    for (int y = well[1]; y < well[1] + well[3]; y++) {
                        wellArea[x][y] = true;
                    }
                }
            }
            NativeImage out = new NativeImage(textureWidth, textureHeight, true);
            for (int x = 0; x < textureWidth; x++) {
                for (int y = 0; y < textureHeight; y++) {
                    int pixel = argb(image, x, y);
                    int color;
                    if (!inside(x, y, width, height)) {
                        color = spriteColor(pixel, muted, accent);
                    } else if (x < BORDER || y < BORDER || x >= width - BORDER || y >= height - BORDER
                            || wellArea[x][y]) {
                        color = 0;
                    } else if (slotArea[x][y]) {
                        // Inside a slot, only the faded item shapes stay (the brewing stand's bottles...).
                        boolean bevel = grey(pixel) && ((pixel & 0xFF) >= 0x80 || pixel == SHADOW
                                || pixel == OUTLINE || pixel == SLOT_EDGE);
                        color = bevel ? 0 : backgroundColor(pixel, muted);
                    } else {
                        color = backgroundColor(pixel, muted);
                    }
                    setArgb(out, x, y, color);
                }
            }
            ResourceLocation id = GaldurUI.id("recolored/" + (counter++));
            //? if >=26.1 {
            /*minecraft.getTextureManager().register(id, new DynamicTexture(id::toString, out));
            *///?} else {
            minecraft.getTextureManager().register(id, new DynamicTexture(out));
            //?}
            return Optional.of(new Result(id, textureWidth, textureHeight, List.copyOf(largeSlots), wells));
        } catch (IOException | RuntimeException exception) {
            GaldurUI.LOGGER.warn("Could not restyle the texture {}", source, exception);
            return Optional.empty();
        }
    }

    /**
     * The wells of a background: areas of one colour at least {@value #WELL_SIZE} pixels on each side
     * that fill their bounding box, with their 1-pixel bevel around.
     */
    private static List<int[]> wells(NativeImage image, boolean[][] slotArea, int width, int height) {
        List<int[]> wells = new ArrayList<>();
        boolean[][] seen = new boolean[Math.max(width, 0)][Math.max(height, 0)];
        int[] queue = new int[Math.max(width * height, 1)];
        for (int startX = BORDER; startX < width - BORDER; startX++) {
            for (int startY = BORDER; startY < height - BORDER; startY++) {
                int color = argb(image, startX, startY);
                if (seen[startX][startY] || slotArea[startX][startY] || color == BACKGROUND || (color >>> 24) == 0) {
                    continue;
                }
                int head = 0;
                int tail = 0;
                queue[tail++] = startX * height + startY;
                seen[startX][startY] = true;
                int minX = startX, maxX = startX, minY = startY, maxY = startY, count = 0;
                while (head < tail) {
                    int cell = queue[head++];
                    int x = cell / height;
                    int y = cell % height;
                    count++;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    int[][] around = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
                    for (int[] next : around) {
                        int nx = next[0];
                        int ny = next[1];
                        if (nx >= BORDER && ny >= BORDER && nx < width - BORDER && ny < height - BORDER
                                && !seen[nx][ny] && !slotArea[nx][ny] && argb(image, nx, ny) == color) {
                            seen[nx][ny] = true;
                            queue[tail++] = nx * height + ny;
                        }
                    }
                }
                int wellWidth = maxX - minX + 1;
                int wellHeight = maxY - minY + 1;
                if (wellWidth >= WELL_SIZE && wellHeight >= WELL_SIZE && count * 10 >= wellWidth * wellHeight * 9) {
                    int x = Math.max(minX - 1, 0);
                    int y = Math.max(minY - 1, 0);
                    wells.add(new int[] {x, y, Math.min(maxX + 2, width) - x, Math.min(maxY + 2, height) - y});
                }
            }
        }
        return List.copyOf(wells);
    }

    /** A pixel of the background: grey shapes in the muted colour, faint when dark. */
    private static int backgroundColor(int argb, int muted) {
        if ((argb >>> 24) == 0 || argb == BACKGROUND || argb == HIDDEN) {
            return 0;
        }
        if (!grey(argb)) {
            return argb;
        }
        return (argb & 0xFF) >= 0x80 ? muted : (FAINT << 24) | (muted & 0xFFFFFF);
    }

    /** A pixel of a progress sprite: what fills up (white) in the accent colour, the empty part muted. */
    private static int spriteColor(int argb, int muted, int accent) {
        if ((argb >>> 24) == 0 || argb == BACKGROUND) {
            return 0;
        }
        if (argb == LIGHT) {
            return accent;
        }
        return backgroundColor(argb, muted);
    }

    private static boolean grey(int argb) {
        int red = (argb >> 16) & 0xFF;
        return red == ((argb >> 8) & 0xFF) && red == (argb & 0xFF);
    }

    private static boolean inside(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static int argb(NativeImage image, int x, int y) {
        //? if >=26.1 {
        /*return image.getPixel(x, y);
        *///?} else {
        int abgr = image.getPixelRGBA(x, y);
        return (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >> 16) & 0xFF);
        //?}
    }

    private static void setArgb(NativeImage image, int x, int y, int argb) {
        //? if >=26.1 {
        /*image.setPixel(x, y, argb);
        *///?} else {
        image.setPixelRGBA(x, y, (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >> 16) & 0xFF));
        //?}
    }
}
