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

    private InventoryPainter() {
    }

    /** {@return {@code true} for the screens whose textures are replaced one by one, not their whole background} */
    public static boolean paintsItself(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen || screen instanceof InventoryScreen;
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
        int color = GaldurTheme.textMuted();
        GaldurRender.drawFill(graphics, left + 132, top + 31, 11, 2, color);
        for (int column = 0; column < 4; column++) {
            GaldurRender.drawFill(graphics, left + 142 + column, top + 28 + column, 1, 8 - 2 * column, color);
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
