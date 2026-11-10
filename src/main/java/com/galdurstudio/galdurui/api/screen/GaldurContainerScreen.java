package com.galdurstudio.galdurui.api.screen;

import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Themed screen for a slot menu: theme panel, a frame per slot, theme-coloured titles (the
 * screen title in the brand font). Drag and drop, Shift-click, number keys and item tooltips are
 * Minecraft's ({@link AbstractContainerScreen}).
 *
 * <p>Works as is for any menu, vanilla ones included. Register it with the helper of your loader
 * ({@code GaldurNeoForgeScreens}, {@code GaldurFabricScreens}, {@code GaldurForgeScreens}), or with
 * the loader's own registration and {@code GaldurContainerScreen<MyMenu>::new}:</p>
 *
 * <pre>{@code
 * // NeoForge, client side, on the mod bus
 * event.register(MY_MENUS.CHEST.get(), GaldurNeoForgeScreens.factory());
 * }</pre>
 *
 * <p>The panel size is derived from the slots: the margin left of the first slot is repeated on
 * the right and at the bottom. A menu without slots gets the size of a vanilla chest
 * (176 × 166). A subclass can force its size with
 * {@link #GaldurContainerScreen(AbstractContainerMenu, Inventory, Component, int, int)} and
 * override {@link #renderBg} or {@link #renderLabels} to add content.</p>
 *
 * <p>The "Inventory" label sits right above the player inventory when the menu has one, and is
 * hidden otherwise. The hovered slot gets a translucent {@code accent} veil and an
 * {@code accent_hover} outline, as does every slot targeted by a drag-spread. With JEI or EMI
 * installed, their item list avoids the panel and the {@link #getExclusionZones()}. The panel rises
 * slightly when the screen opens.</p>
 *
 * @param <M> the menu type
 */
public class GaldurContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    /** Size of a panel without slots: a vanilla chest. */
    private static final int DEFAULT_WIDTH = 176;
    private static final int DEFAULT_HEIGHT = 166;
    /** Slot frame: the 16-pixel item and one pixel on each side. */
    private static final int SLOT_FRAME = 18;
    private static final int ITEM_SIZE = 16;
    /** Gap between the top of the "Inventory" label and the first inventory row. */
    private static final int INVENTORY_LABEL_GAP = 12;
    private static final int TITLE_Y = 6;
    /** Opacity of the hovered slot veil (0 to 255), same as a Galdur item slot. */
    private static final int HOVER_ALPHA = 0x60;
    /** How far the panel rises while opening, in pixels. */
    private static final int OPEN_RISE = 8;

    private final boolean showInventoryLabel;
    /** Start of the opening animation; {@code -1} until the screen is first shown. */
    private long openStart = -1;

    /** Creates the screen, with a panel sized from the menu slots. */
    public GaldurContainerScreen(M menu, Inventory inventory, Component title) {
        this(menu, inventory, title, -1, -1);
    }

    /**
     * Creates the screen with a given panel size.
     *
     * @param width  panel width in pixels; negative to derive it from the slots
     * @param height panel height in pixels; negative to derive it from the slots
     */
    public GaldurContainerScreen(M menu, Inventory inventory, Component title, int width, int height) {
        this(menu, inventory, title, PanelSize.of(menu, width, height));
    }

    private GaldurContainerScreen(M menu, Inventory inventory, Component title, PanelSize size) {
        // Since 26.1 the panel size is final and given to the parent constructor.
        //? if >=26.1 {
        /*super(menu, inventory, title, size.width(), size.height());
        *///?} else {
        super(menu, inventory, title);
        this.imageWidth = size.width();
        this.imageHeight = size.height();
        //?}
        this.titleLabelX = size.margin();
        this.titleLabelY = TITLE_Y;
        this.showInventoryLabel = size.inventoryRow() != null;
        if (size.inventoryRow() != null) {
            this.inventoryLabelX = size.inventoryRow().x;
            this.inventoryLabelY = size.inventoryRow().y - INVENTORY_LABEL_GAP;
        }
    }

    /**
     * Panel size and title margin derived from the menu slots, before the screen exists.
     *
     * @param inventoryRow first slot of the player inventory, {@code null} if the menu has none
     */
    private record PanelSize(int width, int height, int margin, @Nullable Slot inventoryRow) {
        static PanelSize of(AbstractContainerMenu menu, int width, int height) {
            int margin = Integer.MAX_VALUE;
            int right = 0;
            int bottom = 0;
            Slot firstInventoryRow = null;
            for (Slot slot : menu.slots) {
                margin = Math.min(margin, slot.x);
                right = Math.max(right, slot.x + ITEM_SIZE);
                bottom = Math.max(bottom, slot.y + ITEM_SIZE);
                if (slot.container instanceof Inventory && slot.getContainerSlot() == 9 && firstInventoryRow == null) {
                    firstInventoryRow = slot;
                }
            }
            boolean hasSlots = margin != Integer.MAX_VALUE;
            return new PanelSize(width >= 0 ? width : hasSlots ? right + margin : DEFAULT_WIDTH,
                    height >= 0 ? height : hasSlots ? bottom + margin : DEFAULT_HEIGHT,
                    hasSlots ? margin : 8, firstInventoryRow);
        }
    }

    /**
     * {@return extra screen areas, in screen coordinates, that JEI and EMI must keep clear besides
     * the panel: side tabs, buttons sticking out...}; none by default. Read again whenever JEI or
     * EMI need it, so it may depend on the screen state.
     */
    public List<Rect2i> getExclusionZones() {
        return List.of();
    }

    /** Starts the opening animation, the first time the screen is shown only. */
    @Override
    public void added() {
        super.added();
        if (openStart < 0) {
            openStart = GaldurAnimations.now();
        }
    }

    private float openOffset() {
        if (openStart < 0) {
            return 0;
        }
        double t = GaldurAnimations.progress(openStart, GaldurAnimations.OPEN_MS);
        return (float) (OPEN_RISE * (1 - GaldurEasing.OUT_CUBIC.applyAsDouble(t)));
    }

    /** While opening, the panel and its contents rise slightly; tooltips stay under the cursor. */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Before 1.20.2 the screen draws its own background, and AbstractContainerScreen.render
        // calls renderBg (the panel) itself.
        //? if <1.20.2
        //renderBackground(guiGraphics);
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, offset,
                () -> super.render(guiGraphics, mouseX, mouseY - Math.round(offset), partialTick));
        // Since 26.1 AbstractContainerScreen draws the tooltip itself.
        //? if <26.1
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    //? if >=26.1 {
    /*/^*
     * Background dim, then the panel ({@link #renderBg}). Since 26.1 Minecraft asks for the
     * background apart from the rest: the dim covers the whole screen, the panel rises with the
     * contents.
     ^/
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, offset,
                () -> renderBg(guiGraphics, partialTick, mouseX, mouseY - Math.round(offset)));
    }
    *///?} elif >=1.20.2 {
    /** Background dim, then the panel ({@link #renderBg}). The dim covers the whole screen and does not rise. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, -offset, () -> renderTransparentBackground(guiGraphics));
        renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }
    //?}

    /**
     * Narrates the hovered slot item ("Iron Ingot, 32"), which Minecraft does not. Nested one
     * level below the widgets so it does not replace the focused one.
     */
    @Override
    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        super.updateNarratedWidget(narrationElementOutput);
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            narrationElementOutput.nest().nest().add(NarratedElementType.TITLE,
                    GaldurNarration.item(stack, stack.getCount()));
        }
    }

    /**
     * Draws the panel, then a frame for each active slot, outlined when hovered or targeted by a
     * drag-spread. Minecraft draws the items afterwards.
     */
    //? if <26.1
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.PANEL, leftPos, topPos, imageWidth, imageHeight,
                GaldurTheme.paint(GaldurTheme.PANEL));
        boolean spreading = isQuickCrafting && quickCraftSlots.size() > 1;
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            int frameX = leftPos + slot.x - 1;
            int frameY = topPos + slot.y - 1;
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, frameX, frameY, SLOT_FRAME, SLOT_FRAME,
                    GaldurTheme.paint(GaldurTheme.SLOT));
            boolean hovered = slot.isHighlightable() && isHovering(slot.x, slot.y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY);
            if (hovered || (spreading && quickCraftSlots.contains(slot))) {
                GaldurRender.drawOutline(guiGraphics, frameX, frameY, SLOT_FRAME, SLOT_FRAME, GaldurTheme.color(GaldurTheme.OUTLINE));
            }
        }
    }

    /**
     * {@return the hovered slot veil, drawn by Minecraft over the item: translucent {@code accent}}
     *
     * <p>No {@code @Override}: this hook only exists in the NeoForge and Forge patches of
     * Minecraft. On Fabric the vanilla white veil is drawn instead.</p>
     */
    public int getSlotColor(int index) {
        return (HOVER_ALPHA << 24) | (GaldurTheme.color(GaldurTheme.SLOT_HOVER) & 0xFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        GaldurRender.text(guiGraphics, title, titleLabelX, titleLabelY, GaldurTheme.color(GaldurTheme.TITLE), false);
        if (showInventoryLabel) {
            GaldurRender.text(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                    GaldurTheme.textMuted(), false);
        }
    }
}
