package com.galdurstudio.galdurui.api.screen;

import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.text.GaldurFonts;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Themed screen for a slot menu: theme panel, a frame per slot, theme-coloured titles (the
 * screen title in the brand font). Drag and drop, Shift-click, number keys and item tooltips are
 * Minecraft's ({@link AbstractContainerScreen}).
 *
 * <p>Works as is for any menu, vanilla ones included, through {@link #factory()}:</p>
 *
 * <pre>{@code
 * // client side, on the mod bus
 * event.register(MY_MENUS.CHEST.get(), GaldurContainerScreen.factory());
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
        super(menu, inventory, title);
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
        this.imageWidth = width >= 0 ? width : hasSlots ? right + margin : DEFAULT_WIDTH;
        this.imageHeight = height >= 0 ? height : hasSlots ? bottom + margin : DEFAULT_HEIGHT;
        this.titleLabelX = hasSlots ? margin : 8;
        this.titleLabelY = TITLE_Y;
        this.showInventoryLabel = firstInventoryRow != null;
        if (firstInventoryRow != null) {
            this.inventoryLabelX = firstInventoryRow.x;
            this.inventoryLabelY = firstInventoryRow.y - INVENTORY_LABEL_GAP;
        }
    }

    /**
     * {@return a screen constructor for {@code RegisterMenuScreensEvent.register}, without a
     * subclass}; a plain {@code GaldurContainerScreen::new} does not work there because Java cannot
     * infer the screen type
     *
     * @param <M> the menu type
     */
    public static <M extends AbstractContainerMenu> MenuScreens.ScreenConstructor<M, GaldurContainerScreen<M>> factory() {
        return GaldurContainerScreen::new;
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
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, offset,
                () -> super.render(guiGraphics, mouseX, mouseY - Math.round(offset), partialTick));
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /** Background dim, then the panel ({@link #renderBg}). The dim covers the whole screen and does not rise. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, -offset, () -> renderTransparentBackground(guiGraphics));
        renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

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
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.PANEL, leftPos, topPos, imageWidth, imageHeight,
                GaldurTheme.background());
        boolean spreading = isQuickCrafting && quickCraftSlots.size() > 1;
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            int frameX = leftPos + slot.x - 1;
            int frameY = topPos + slot.y - 1;
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, frameX, frameY, SLOT_FRAME, SLOT_FRAME,
                    GaldurTheme.background());
            boolean hovered = slot.isHighlightable() && isHovering(slot.x, slot.y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY);
            if (hovered || (spreading && quickCraftSlots.contains(slot))) {
                GaldurRender.drawOutline(guiGraphics, frameX, frameY, SLOT_FRAME, SLOT_FRAME, GaldurTheme.accentHover());
            }
        }
    }

    /** {@return the hovered slot veil, drawn by Minecraft over the item: translucent {@code accent}} */
    @Override
    public int getSlotColor(int index) {
        return (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        GaldurRender.text(guiGraphics, GaldurFonts.title(title), titleLabelX, titleLabelY, GaldurTheme.text(), false);
        if (showInventoryLabel) {
            GaldurRender.text(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                    GaldurTheme.textMuted(), false);
        }
    }
}
