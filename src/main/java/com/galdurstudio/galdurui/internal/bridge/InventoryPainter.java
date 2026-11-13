package com.galdurstudio.galdurui.internal.bridge;

import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.internal.mixin.AbstractContainerScreenAccessor;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

/**
 * The player inventory and the creative inventory in the Galdur look. Their background is a
 * texture with tabs, a scroll bar, a search field and the player drawn around it, so the Galdur
 * panel cannot simply replace the whole background: Minecraft keeps drawing everything, and each
 * texture or sprite it asks for is answered with a Galdur one ({@code GuiGraphicsMixin}).
 */
public final class InventoryPainter {
    private static final String INVENTORY_TEXTURE = "textures/gui/container/inventory.png";
    private static final String CREATIVE_TEXTURES = "textures/gui/container/creative_inventory/";
    private static final String CREATIVE_SPRITES = "container/creative_inventory/";
    private static final String CRAFTING_TEXTURE = "textures/gui/container/crafting_table.png";
    /** Furnace, blast furnace and smoker: the same layout. */
    private static final java.util.Set<String> FURNACE_TEXTURES = java.util.Set.of(
            "textures/gui/container/furnace.png", "textures/gui/container/blast_furnace.png",
            "textures/gui/container/smoker.png");

    private InventoryPainter() {
    }

    /** {@return {@code true} for the screens whose textures are replaced one by one, not their whole background} */
    public static boolean paintsItself(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen || screen instanceof InventoryScreen
                || screen instanceof AbstractFurnaceScreen<?> || screen instanceof CraftingScreen;
    }

    private static AbstractContainerScreen<?> ownPanelScreen() {
        Screen screen = Client.screen();
        return screen != null && paintsItself(screen) && BridgePainter.replaceBackground()
                ? (AbstractContainerScreen<?>) screen : null;
    }

    /**
     * Answers a texture that Minecraft is about to draw.
     *
     * @return {@code true} if a Galdur version was drawn instead
     */
    public static boolean texture(GuiGraphics graphics, ResourceLocation id, int x, int y, int u, int v, int width, int height) {
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }
        AbstractContainerScreen<?> screen = ownPanelScreen();
        if (screen == null) {
            return false;
        }
        String path = id.getPath();
        // The background is drawn from the top-left corner of its texture. Before 1.20.2, the flame and
        // the arrow come from the same texture further right: Minecraft keeps drawing them.
        boolean background = u == 0 && v == 0;
        if (screen instanceof AbstractFurnaceScreen<?> && FURNACE_TEXTURES.contains(path)) {
            if (background) {
                furnacePanel(graphics, screen);
                return true;
            }
            // Before 1.20.2: the burning flame (v 0) and the progress arrow (v 14) come from u 176.
            if (u == 176) {
                return furnaceProgress(graphics, screen, v < 14, x, y, width, height);
            }
        }
        if (background && screen instanceof CraftingScreen && path.equals(CRAFTING_TEXTURE)) {
            craftingPanel(graphics, screen);
            return true;
        }
        if (screen instanceof InventoryScreen && path.equals(INVENTORY_TEXTURE)) {
            inventoryPanel(graphics, screen);
            return true;
        }
        if (screen instanceof CreativeModeInventoryScreen && path.startsWith(CREATIVE_TEXTURES)) {
            String name = path.substring(CREATIVE_TEXTURES.length());
            if (name.equals("tabs.png")) {
                // Before 1.20.2, the tabs and the scroller come from one texture.
                if (u >= 232) {
                    scroller(graphics, screen, x, y, width, height, u >= 244);
                } else {
                    tab(graphics, x, y, width, height, v % 64 >= 32, v < 64);
                }
                return true;
            }
            if (name.startsWith("tab_")) {
                creativePanel(graphics, screen, name);
                return true;
            }
        }
        return false;
    }

    /**
     * Answers a GUI sprite that Minecraft is about to draw.
     *
     * @return {@code true} if a Galdur version was drawn instead
     */
    public static boolean sprite(GuiGraphics graphics, ResourceLocation id, int x, int y, int width, int height) {
        if (!"minecraft".equals(id.getNamespace()) || !id.getPath().startsWith(CREATIVE_SPRITES)) {
            return false;
        }
        AbstractContainerScreen<?> screen = ownPanelScreen();
        if (!(screen instanceof CreativeModeInventoryScreen)) {
            return false;
        }
        String name = id.getPath().substring(CREATIVE_SPRITES.length());
        if (name.startsWith("tab_")) {
            tab(graphics, x, y, width, height, name.contains("selected") && !name.contains("unselected"),
                    name.contains("top"));
            return true;
        }
        if (name.startsWith("scroller")) {
            scroller(graphics, screen, x, y, width, height, name.contains("disabled"));
            return true;
        }
        return false;
    }

    private static void creativePanel(GuiGraphics graphics, AbstractContainerScreen<?> screen, String name) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        BridgePainter.containerBackground(graphics, screen, layout);
        int left = layout.galdurui$leftPos();
        int top = layout.galdurui$topPos();
        if (name.contains("search")) {
            field(graphics, left + 80, top + 4, 90, 13);
        } else if (name.contains("inventory")) {
            field(graphics, left + 72, top + 5, 34, 45);
            cross(graphics, left + 173, top + 112);
        }
    }

    private static void inventoryPanel(GuiGraphics graphics, AbstractContainerScreen<?> screen) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        BridgePainter.containerBackground(graphics, screen, layout);
        int left = layout.galdurui$leftPos();
        int top = layout.galdurui$topPos();
        field(graphics, left + 25, top + 7, 51, 72);
        // The arrow from the crafting grid to its result.
        arrow(graphics, left + 135, top + 35, 16, 9, 13, GaldurTheme.textMuted());
    }

    /** Furnace, blast furnace, smoker: Minecraft then draws the burning flame and the progress arrow over these. */
    private static void furnacePanel(GuiGraphics graphics, AbstractContainerScreen<?> screen) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        BridgePainter.containerBackground(graphics, screen, layout);
        int left = layout.galdurui$leftPos();
        int top = layout.galdurui$topPos();
        flame(graphics, left + 56, top + 36);
        arrow(graphics, left + 80, top + 42, 22, 14, 15, GaldurTheme.textMuted());
        resultSlot(graphics, screen, left, top, 2);
    }

    private static void craftingPanel(GuiGraphics graphics, AbstractContainerScreen<?> screen) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        BridgePainter.containerBackground(graphics, screen, layout);
        arrow(graphics, layout.galdurui$leftPos() + 90, layout.galdurui$topPos() + 42, 22, 14, 15, GaldurTheme.textMuted());
        resultSlot(graphics, screen, layout.galdurui$leftPos(), layout.galdurui$topPos(), 0);
    }

    /**
     * Answers the part of a GUI sprite Minecraft draws to show a progress: the burning flame and the
     * cooking arrow of a furnace, redrawn in the theme colours and cut to the same part.
     *
     * @return {@code true} if a Galdur version was drawn instead
     */
    public static boolean spritePart(GuiGraphics graphics, ResourceLocation id, int x, int y, int width, int height) {
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }
        AbstractContainerScreen<?> screen = ownPanelScreen();
        if (!(screen instanceof AbstractFurnaceScreen<?>)) {
            return false;
        }
        String path = id.getPath();
        if (!path.matches("container/(furnace|blast_furnace|smoker)/(lit|burn)_progress")) {
            return false;
        }
        return furnaceProgress(graphics, screen, path.endsWith("lit_progress"), x, y, width, height);
    }

    /** The flame or the arrow, whole, in the theme colour, but only inside the part asked for. */
    private static boolean furnaceProgress(GuiGraphics graphics, AbstractContainerScreen<?> screen, boolean flame,
                                           int x, int y, int width, int height) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        int left = layout.galdurui$leftPos();
        int top = layout.galdurui$topPos();
        if (width <= 0 || height <= 0) {
            return true;
        }
        GaldurRender.drawClipped(graphics, x, y, width, height, () -> {
            if (flame) {
                flame(graphics, left + 56, top + 36, GaldurTheme.ember());
            } else {
                arrow(graphics, left + 80, top + 42, 22, 14, 15, GaldurTheme.accent());
            }
        });
        return true;
    }

    /** The result slot of a furnace or crafting table: a larger frame, 26 x 26, as on the vanilla texture. */
    private static void resultSlot(GuiGraphics graphics, AbstractContainerScreen<?> screen, int left, int top, int index) {
        var slots = screen.getMenu().slots;
        if (index < slots.size()) {
            var slot = slots.get(index);
            GaldurRender.drawNineSlice(graphics, GaldurSprites.SLOT, left + slot.x - 5, top + slot.y - 5, 26, 26,
                    GaldurTheme.paint(GaldurTheme.SLOT));
        }
    }

    /**
     * An arrow pointing right, drawn like Minecraft's: a shaft 3 pixels thick centred on {@code y}, {@code length}
     * pixels long from {@code x}, and a triangular head {@code headHeight} pixels high starting {@code headOffset}
     * pixels after {@code x}.
     */
    private static void arrow(GuiGraphics graphics, int x, int y, int length, int headOffset, int headHeight, int color) {
        GaldurRender.drawFill(graphics, x, y - 1, length - 1, 3, color);
        GaldurRender.drawFill(graphics, x + length - 1, y, 1, 1, color);
        for (int column = 0; column <= headHeight / 2; column++) {
            int height = headHeight - 2 * column;
            GaldurRender.drawFill(graphics, x + headOffset + column, y - height / 2, 1, height, color);
        }
    }

    /** The furnace flame, pixel for pixel the vanilla one, in a 14 x 14 square. */
    private static final String[] FLAME = {
        "..............", "..#.........#.", "..#.........#.", "...#...#...#..", "...#...#...#..",
        "..##....#..##.", "####..###.###.", "####.####.####", "###..####..###", "###..####.####",
        "####.###..####", ".###.###..###.", "####.####.###.", ".###..###..###",
    };

    /** A muted flame, for the furnace while it is not burning. */
    private static void flame(GuiGraphics graphics, int x, int y) {
        flame(graphics, x, y, GaldurTheme.textMuted());
    }

    private static void flame(GuiGraphics graphics, int x, int y, int color) {
        for (int row = 0; row < FLAME.length; row++) {
            String line = FLAME[row];
            int column = 0;
            while (column < line.length()) {
                if (line.charAt(column) != '#') {
                    column++;
                    continue;
                }
                int run = column;
                while (run < line.length() && line.charAt(run) == '#') {
                    run++;
                }
                GaldurRender.drawFill(graphics, x + column, y + row, run - column, 1, color);
                column = run;
            }
        }
    }

    private static void field(GuiGraphics graphics, int x, int y, int width, int height) {
        GaldurRender.drawNineSlice(graphics, GaldurSprites.FIELD, x, y, width, height, GaldurTheme.paint(GaldurTheme.FIELD));
    }

    /** The red cross of the creative inventory's delete slot, which was part of the texture. */
    private static void cross(GuiGraphics graphics, int x, int y) {
        int color = 0xFFB8341F;
        for (int step = 2; step < 14; step++) {
            GaldurRender.drawFill(graphics, x + step, y + step, 2, 2, color);
            GaldurRender.drawFill(graphics, x + 15 - step, y + step, 2, 2, color);
        }
    }

    /** A creative tab: the selected one has the colour of the panel it opens. */
    private static void tab(GuiGraphics graphics, int x, int y, int width, int height, boolean selected, boolean top) {
        // Smaller than Minecraft's 26x32 sprite: its icon sits 4 pixels from each edge of the visible part,
        // and the last 4 pixels hide under the panel.
        int visible = 24;
        GaldurRender.drawNineSlice(graphics, selected ? GaldurSprites.PANEL : GaldurSprites.TAB, x + 2,
                top ? y + 5 : y + 3, width - 4, visible,
                GaldurTheme.paint(selected ? GaldurTheme.PANEL : GaldurTheme.TAB));
    }

    /** The scroll bar of the creative inventory: its groove, drawn with the handle, and the handle. */
    private static void scroller(GuiGraphics graphics, AbstractContainerScreen<?> screen, int x, int y, int width,
                                 int height, boolean disabled) {
        AbstractContainerScreenAccessor layout = (AbstractContainerScreenAccessor) screen;
        GaldurRender.drawNineSlice(graphics, GaldurSprites.SCROLLBAR_TRACK, x - 1, layout.galdurui$topPos() + 17,
                width + 2, 114, GaldurTheme.paint(GaldurTheme.FIELD));
        GaldurPaint handle = GaldurTheme.paint(disabled ? GaldurTheme.BUTTON_DISABLED : GaldurTheme.SCROLLBAR);
        GaldurRender.drawNineSlice(graphics, GaldurSprites.SCROLLBAR_THUMB, x, y, width, height, handle);
    }
}
