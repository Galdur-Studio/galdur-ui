package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Themed scrolling item grid, to show thousands of items with their amounts: a storage network's
 * contents, a catalogue, a recipe list...
 *
 * <p>These are <strong>not</strong> real menu slots: the grid only shows a list of {@link Entry}
 * and reports clicks. The mod keeps the list up to date (from server packets, for instance),
 * filters and sorts it, and sends the server what the player wants to do with a click. The list
 * is <strong>read every frame</strong>: it can change at any time without rebuilding the
 * widget.</p>
 *
 * <pre>{@code
 * List<GaldurItemGrid.Entry> contents = ...;   // kept up to date by the mod
 * GaldurItemGrid grid = GaldurItemGrid.of(9, 6, () -> contents)
 *         .onClick((index, entry, button, shift) -> askServer(entry, button, shift));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Rendering</strong>: only the visible rows are drawn (columns × rows cells),
 *       whatever the list size. Each cell uses the {@code slot} sprite and the short amount of
 *       {@link GaldurItemSlot} ("64", "1,2k", "64k").</li>
 *   <li><strong>Scrolling</strong>: by whole rows, with the wheel (one row per notch) or the
 *       scrollbar, which can be grabbed and dragged. The scrollbar only shows when the items do
 *       not fit.</li>
 *   <li><strong>Hover</strong>: {@code accent_hover} outline, {@code accent} veil and the item
 *       tooltip, with the exact amount above 1,000.</li>
 *   <li><strong>Click</strong>: left or right, with the Shift state, on an occupied cell.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): muted amounts, no click or hover;
 *       scrolling still works for browsing.</li>
 *   <li><strong>JEI / EMI</strong>: when one is installed, hovering an item and pressing R or U
 *       shows its recipes, on any screen.</li>
 * </ul>
 *
 * <p>Sizes from the theme: {@code spacing.scrollbar_width}. A cell is 18 × 18 pixels, like a
 * vanilla slot.</p>
 */
public class GaldurItemGrid extends AbstractWidget {
    /** Side of a cell, frame included. */
    public static final int CELL_SIZE = GaldurItemSlot.SIZE;
    /** Gap between the last column and the scrollbar. */
    private static final int SCROLLBAR_GAP = 2;
    /** Minimum height of the scrollbar thumb. */
    private static final int MIN_THUMB_HEIGHT = 8;
    /** Opacity of the veil on the hovered cell (0 to 255). */
    private static final int HOVER_ALPHA = 0x60;

    /**
     * An item shown by the grid.
     *
     * @param stack  the item; its own count is ignored, {@code amount} is shown instead
     * @param amount the amount to show; {@code 1} shows nothing, as in vanilla, and neither does
     *               {@code 0} or less (known but absent item, craftable...)
     */
    public record Entry(ItemStack stack, long amount) {
    }

    /** Action triggered by a click on an occupied cell. */
    @FunctionalInterface
    public interface ClickHandler {
        /**
         * @param index  index of the item in the list
         * @param entry  the clicked item
         * @param button the mouse button ({@code 0} left, {@code 1} right)
         * @param shift  {@code true} if Shift was held
         */
        void onClick(int index, Entry entry, int button, boolean shift);
    }

    private final int columns;
    private final int rows;
    private final Supplier<List<Entry>> entries;
    private ClickHandler onClick = (index, entry, button, shift) -> {};
    private IntConsumer onScroll = row -> {};

    /** First visible row. */
    private int scrollRow;
    /** True while the player drags the scrollbar thumb. */
    private boolean draggingThumb;
    /** Mouse offset inside the thumb when it was grabbed. */
    private double grabOffset;
    /** Index of the item hovered on the last frame, or {@code -1}; read by narration. */
    private int hoveredIndex = -1;

    /**
     * Prefer {@link #of(int, int, Supplier)}.
     *
     * @param columns number of columns (at least 1)
     * @param rows    number of visible rows (at least 1)
     * @param entries supplier of the item list, called every frame
     */
    protected GaldurItemGrid(int columns, int rows, Supplier<List<Entry>> entries) {
        super(0, 0, Math.max(1, columns) * CELL_SIZE + SCROLLBAR_GAP + GaldurTheme.scrollbarWidth(),
                Math.max(1, rows) * CELL_SIZE, Component.empty());
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.entries = entries;
    }

    /**
     * {@return a grid of {@code columns} × {@code rows} visible cells, scrollbar included, at
     * {@code (0, 0)} for a layout to place}
     *
     * @param columns number of columns (at least 1)
     * @param rows    number of visible rows (at least 1)
     * @param entries supplier of the item list, called every frame
     */
    public static GaldurItemGrid of(int columns, int rows, Supplier<List<Entry>> entries) {
        return new GaldurItemGrid(columns, rows, entries);
    }

    /**
     * Sets the action triggered by a click on an occupied cell. Only one action is kept.
     *
     * @param onClick receives the index, the item, the button and the Shift state
     * @return this grid, for chaining
     */
    public GaldurItemGrid onClick(ClickHandler onClick) {
        this.onClick = onClick;
        return this;
    }

    /**
     * Sets the action called when the first visible row changes, to keep it when the widget is
     * rebuilt (resize). Only one action is kept.
     *
     * @param onScroll receives the new first visible row
     * @return this grid, for chaining
     */
    public GaldurItemGrid onScroll(IntConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    /** {@return the first visible row} */
    public int getScrollRow() {
        return scrollRow;
    }

    /**
     * Scrolls the grid, clamped between the first and the last possible row.
     *
     * @param row the first row to show
     */
    public void setScrollRow(int row) {
        int clamped = Mth.clamp(row, 0, maxScrollRow());
        if (clamped != scrollRow) {
            scrollRow = clamped;
            onScroll.accept(clamped);
        }
    }

    private int totalRows() {
        return Mth.positiveCeilDiv(entries.get().size(), columns);
    }

    private int maxScrollRow() {
        return Math.max(0, totalRows() - rows);
    }

    private boolean hasScrollbar() {
        return totalRows() > rows;
    }

    private int gridWidth() {
        return columns * CELL_SIZE;
    }

    private int scrollbarX() {
        return getX() + gridWidth() + SCROLLBAR_GAP;
    }

    private int thumbHeight() {
        return Mth.clamp(getHeight() * rows / Math.max(1, totalRows()), MIN_THUMB_HEIGHT, getHeight());
    }

    private int thumbY() {
        int travel = getHeight() - thumbHeight();
        int max = maxScrollRow();
        return getY() + (max == 0 ? 0 : scrollRow * travel / max);
    }

    /** {@return the list index of the item under this point, or {@code -1}} */
    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < getX() || mouseX >= getX() + gridWidth() || mouseY < getY() || mouseY >= getY() + getHeight()) {
            return -1;
        }
        int column = (int) ((mouseX - getX()) / CELL_SIZE);
        int row = (int) ((mouseY - getY()) / CELL_SIZE);
        int index = (scrollRow + row) * columns + column;
        return index < entries.get().size() ? index : -1;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return hasScrollbar() && mouseX >= scrollbarX() && mouseX < scrollbarX() + GaldurTheme.scrollbarWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setScrollRow(scrollRow); // the list may have shrunk since the previous frame
        List<Entry> list = entries.get();
        int hovered = active && isHovered() ? indexAt(mouseX, mouseY) : -1;
        hoveredIndex = hovered;
        int countColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        Entry hoveredEntry = null;

        // Three passes, to limit GPU submissions: every cell background at once, then the items,
        // then all amounts grouped on top.
        GaldurRender.drawNineSliceGrid(guiGraphics, GaldurSprites.SLOT, getX(), getY(), CELL_SIZE, CELL_SIZE,
                columns, rows, GaldurTheme.background());
        int first = scrollRow * columns;
        int last = Math.min(list.size(), first + rows * columns);
        int shown = Math.max(0, last - first);
        ItemStack[] stacks = new ItemStack[shown];
        int[] itemX = new int[shown];
        int[] itemY = new int[shown];
        for (int index = first; index < last; index++) {
            int cellX = getX() + (index - first) % columns * CELL_SIZE;
            int cellY = getY() + (index - first) / columns * CELL_SIZE;
            Entry entry = list.get(index);
            if (index == hovered) {
                hoveredEntry = entry;
                GaldurRender.drawOutline(guiGraphics, cellX, cellY, CELL_SIZE, CELL_SIZE, GaldurTheme.accentHover());
                GaldurRender.drawFill(guiGraphics, cellX + 1, cellY + 1, GaldurRender.ITEM_SIZE,
                        GaldurRender.ITEM_SIZE, (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
            }
            stacks[index - first] = entry.stack();
            itemX[index - first] = cellX + 1;
            itemY[index - first] = cellY + 1;
        }
        GaldurRender.drawItems(guiGraphics, stacks, itemX, itemY, shown);
        GaldurRender.drawBatched(guiGraphics, () -> {
            for (int index = first; index < last; index++) {
                Entry entry = list.get(index);
                if (entry.amount() > 1) {
                    int cellX = getX() + (index - first) % columns * CELL_SIZE;
                    int cellY = getY() + (index - first) / columns * CELL_SIZE;
                    GaldurRender.drawItemCount(guiGraphics, GaldurFormat.amount(entry.amount()), cellX + 1, cellY + 1,
                            countColor);
                }
            }
        });

        if (hasScrollbar()) {
            int barWidth = GaldurTheme.scrollbarWidth();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_TRACK, scrollbarX(), getY(), barWidth,
                    getHeight(), GaldurTheme.background());
            int thumbColor = draggingThumb ? GaldurTheme.accentHover() : GaldurTheme.textMuted();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_THUMB, scrollbarX(), thumbY(), barWidth,
                    thumbHeight(), thumbColor);
        }

        if (hoveredEntry != null && !hoveredEntry.stack().isEmpty()) {
            long amount = hoveredEntry.amount();
            GaldurRender.showItemTooltip(hoveredEntry.stack(), amount >= 1000
                    ? List.of(Component.literal(GaldurFormat.fullAmount(amount)).withStyle(ChatFormatting.GRAY))
                    : List.of());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY) || !hasScrollbar() || scrollY == 0) {
            return false;
        }
        setScrollRow(scrollRow - (int) Math.signum(scrollY));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isOverScrollbar(mouseX, mouseY)) {
            draggingThumb = true;
            int thumbTop = thumbY();
            if (mouseY < thumbTop || mouseY >= thumbTop + thumbHeight()) {
                grabOffset = thumbHeight() / 2.0;
                dragThumbTo(mouseY);
            } else {
                grabOffset = mouseY - thumbTop;
            }
            return true;
        }
        if (!active || (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            return false;
        }
        int index = indexAt(mouseX, mouseY);
        if (index >= 0) {
            onClick.onClick(index, entries.get().get(index), button, Screen.hasShiftDown());
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingThumb) {
            return false;
        }
        dragThumbTo(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingThumb;
        draggingThumb = false;
        return wasDragging;
    }

    /** Scrolls so that the thumb, grabbed at {@code grabOffset}, follows the mouse. */
    private void dragThumbTo(double mouseY) {
        int travel = getHeight() - thumbHeight();
        if (travel > 0) {
            double position = (mouseY - grabOffset - getY()) / travel;
            setScrollRow((int) Math.round(position * maxScrollRow()));
        }
    }

    /**
     * {@return the item under the mouse, or {@code null}} Useful for a screen that wants, for
     * instance, to show its recipe in JEI/EMI.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    @Nullable
    public Entry getEntryAt(double mouseX, double mouseY) {
        int index = indexAt(mouseX, mouseY);
        return index >= 0 ? entries.get().get(index) : null;
    }

    /**
     * {@return the screen-space bounds of the occupied cell under this point, or {@code null}}
     *
     * <p>Used by the JEI and EMI integrations, which need the hovered item's area.</p>
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    @Nullable
    public Rect2i getCellAreaAt(double mouseX, double mouseY) {
        if (indexAt(mouseX, mouseY) < 0) {
            return null;
        }
        int column = (int) ((mouseX - getX()) / CELL_SIZE);
        int row = (int) ((mouseY - getY()) / CELL_SIZE);
        return new Rect2i(getX() + column * CELL_SIZE, getY() + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Hovered cell: "Iron ingot, 1 234", then "Item 5 of 120".
        // Otherwise: "Item grid: 120 items".
        List<Entry> list = entries.get();
        Component title;
        if (hoveredIndex >= 0 && hoveredIndex < list.size()) {
            Entry entry = list.get(hoveredIndex);
            title = GaldurNarration.item(entry.stack(), entry.amount());
            narrationElementOutput.add(NarratedElementType.POSITION,
                    GaldurNarration.ours("grid.position", hoveredIndex + 1, list.size()));
        } else {
            title = list.isEmpty() ? GaldurNarration.ours("grid.empty") : GaldurNarration.ours("grid", list.size());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
    }
}
