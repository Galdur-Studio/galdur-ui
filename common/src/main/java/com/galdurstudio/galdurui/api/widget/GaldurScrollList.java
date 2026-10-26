package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Themed scrolling list of text lines, usable in vanilla layouts. The player selects a line with
 * a click, or with the up and down arrows.
 *
 * <p>Like {@link GaldurCheckbox}, the widget keeps neither the lines nor the selection: it
 * <strong>reads them every frame</strong> from suppliers and reports the changes the player asks
 * for. The lines can change at any time without rebuilding the widget. Only the scroll position
 * lives in the widget; {@link #onScroll(DoubleConsumer)} lets you keep it elsewhere when the
 * widget is rebuilt (resize).</p>
 *
 * <pre>{@code
 * List<Component> ores = List.of(Component.literal("Iron"), Component.literal("Gold"));
 * int[] choice = {-1};
 * column.addChild(GaldurScrollList.of(120, 5, () -> ores,
 *         () -> choice[0], index -> choice[0] = index));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Mouse wheel</strong>: scrolls one line per notch.</li>
 *   <li><strong>Scrollbar</strong>: shown only when the lines do not fit; it can be grabbed and
 *       dragged.</li>
 *   <li><strong>Keyboard</strong>: up and down arrows change the selection; the list scrolls to
 *       keep the selected line visible.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): muted text, no click, selection or
 *       scrolling.</li>
 * </ul>
 *
 * <p>A line that is too long is clipped at the list edge (see {@code GaldurRender.drawClipped}).
 * Sizes from the theme: {@code spacing.list_row_height} and {@code spacing.scrollbar_width}.</p>
 */
public class GaldurScrollList extends AbstractWidget {
    /** Frame thickness, in pixels. */
    private static final int BORDER = 1;
    /** Gap between the left edge of a line and its text, in pixels. */
    private static final int TEXT_PADDING = 3;
    /** Minimum height of the scrollbar thumb, in pixels. */
    private static final int MIN_THUMB_HEIGHT = 8;
    /** Opacity of the hovered line highlight (0 to 255). */
    private static final int HOVER_ALPHA = 0x40;

    private final int visibleRows;
    private final Supplier<List<Component>> lines;
    private final IntSupplier selected;
    private final IntConsumer onSelect;
    private DoubleConsumer onScroll = amount -> {};

    /** Scroll offset, in pixels from the top of the content. */
    private double scroll;
    /**
     * Shown scroll offset, which smoothly catches up with {@link #scroll} (wheel, arrows);
     * {@code NaN} before the first frame. Clicks use it: you click what you see.
     */
    private double shownScroll = Double.NaN;
    /** Time of the previous frame, for smooth scrolling. */
    private long lastFrame;
    /** True while the player drags the scrollbar thumb. */
    private boolean draggingThumb;

    /**
     * Prefer {@link #of(int, int, Supplier, IntSupplier, IntConsumer)}.
     *
     * @param width       total width, frame and scrollbar included, in pixels
     * @param visibleRows number of lines visible at once (at least 1)
     * @param lines       supplier of the lines, called every frame
     * @param selected    supplier of the selected line index, or {@code -1} for none
     * @param onSelect    called with the index of the line the player selects
     */
    protected GaldurScrollList(int width, int visibleRows, Supplier<List<Component>> lines,
                               IntSupplier selected, IntConsumer onSelect) {
        super(0, 0, width, Math.max(1, visibleRows) * GaldurTheme.listRowHeight() + 2 * BORDER,
                Component.empty());
        this.visibleRows = Math.max(1, visibleRows);
        this.lines = lines;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    /**
     * {@return a scrolling list at {@code (0, 0)}, for a layout to place}
     *
     * @param width       total width, frame and scrollbar included, in pixels
     * @param visibleRows number of lines visible at once (at least 1); sets the height
     * @param lines       supplier of the lines, called every frame
     * @param selected    supplier of the selected line index, or {@code -1} for none
     * @param onSelect    called with the index of the line the player selects
     */
    public static GaldurScrollList of(int width, int visibleRows, Supplier<List<Component>> lines,
                                      IntSupplier selected, IntConsumer onSelect) {
        return new GaldurScrollList(width, visibleRows, lines, selected, onSelect);
    }

    /**
     * {@return the width that shows the longest given line in full, scrollbar included, or
     * {@code minWidth} if larger}
     *
     * @param lines    the lines to show
     * @param minWidth minimum width, in pixels
     */
    public static int preferredWidth(List<Component> lines, int minWidth) {
        int widest = 0;
        for (Component line : lines) {
            widest = Math.max(widest, GaldurRender.textWidth(line));
        }
        return Math.max(minWidth, widest + 2 * TEXT_PADDING + 2 * BORDER + GaldurTheme.scrollbarWidth());
    }

    /**
     * Sets the action called when the scroll offset changes, by the player or by code. Only one
     * action is kept.
     *
     * @param onScroll receives the new offset, in pixels from the top of the content
     * @return this list, for chaining
     */
    public GaldurScrollList onScroll(DoubleConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    /** {@return the scroll offset, in pixels from the top of the content} */
    public double getScroll() {
        return scroll;
    }

    /**
     * Scrolls the list, clamped between the top and the bottom of the content.
     *
     * @param scroll wanted offset, in pixels from the top of the content
     */
    public void setScroll(double scroll) {
        double clamped = Mth.clamp(scroll, 0, maxScroll());
        if (clamped != this.scroll) {
            this.scroll = clamped;
            onScroll.accept(clamped);
        }
    }

    /**
     * Scrolls just enough for line {@code index} to be fully visible.
     *
     * @param index the line index; no effect when outside the list
     */
    public void scrollToLine(int index) {
        if (index < 0 || index >= lines.get().size()) {
            return;
        }
        int rowHeight = GaldurTheme.listRowHeight();
        int top = index * rowHeight;
        int bottom = top + rowHeight;
        if (top < scroll) {
            setScroll(top);
        } else if (bottom > scroll + innerHeight()) {
            setScroll(bottom - innerHeight());
        }
    }

    private int innerX() {
        return getX() + BORDER;
    }

    private int innerY() {
        return getY() + BORDER;
    }

    private int innerHeight() {
        return getHeight() - 2 * BORDER;
    }

    private int contentHeight() {
        return lines.get().size() * GaldurTheme.listRowHeight();
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - innerHeight());
    }

    private boolean hasScrollbar() {
        return contentHeight() > innerHeight();
    }

    /** {@return the width of the lines: the whole inside, minus the scrollbar when shown} */
    private int rowsWidth() {
        int inner = getWidth() - 2 * BORDER;
        return hasScrollbar() ? inner - GaldurTheme.scrollbarWidth() : inner;
    }

    private int scrollbarX() {
        return innerX() + rowsWidth();
    }

    private int thumbHeight() {
        int track = innerHeight();
        return Mth.clamp(track * track / Math.max(1, contentHeight()), MIN_THUMB_HEIGHT, track);
    }

    private int thumbY() {
        int travel = innerHeight() - thumbHeight();
        int max = maxScroll();
        return innerY() + (max == 0 ? 0 : (int) (view() * travel / max));
    }

    /** {@return the index of the line under this point, or {@code -1} if none} */
    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < innerX() || mouseX >= innerX() + rowsWidth()
                || mouseY < innerY() || mouseY >= innerY() + innerHeight()) {
            return -1;
        }
        int index = (int) ((mouseY - innerY() + view()) / GaldurTheme.listRowHeight());
        return index < lines.get().size() ? index : -1;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return hasScrollbar() && mouseX >= scrollbarX() && mouseX < scrollbarX() + GaldurTheme.scrollbarWidth()
                && mouseY >= innerY() && mouseY < innerY() + innerHeight();
    }

    /** {@return the shown scroll offset, or the real one before the first frame} */
    private double view() {
        return Double.isNaN(shownScroll) ? scroll : shownScroll;
    }

    /** Moves the shown offset towards the real one; at once while the thumb is dragged. */
    private void updateShownScroll() {
        long now = GaldurAnimations.now();
        shownScroll = draggingThumb
                ? scroll
                : GaldurAnimations.approach(shownScroll, scroll, now - lastFrame,
                        GaldurAnimations.SCROLL_SMOOTHING_MS, 0.25);
        lastFrame = now;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setScroll(scroll);
        updateShownScroll();
        double view = view();
        List<Component> rows = lines.get();
        int rowHeight = GaldurTheme.listRowHeight();
        int rowsWidth = rowsWidth();
        int selectedIndex = selected.getAsInt();
        int hovered = active && isHovered() ? rowAt(mouseX, mouseY) : -1;
        int textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        boolean keyboardFocused = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), getY(), getWidth(), getHeight(),
                GaldurTheme.surface());
        GaldurRender.drawClipped(guiGraphics, innerX(), innerY(), rowsWidth, innerHeight(), () -> {
            int first = (int) (view / rowHeight);
            int last = Math.min(rows.size() - 1, (int) ((view + innerHeight()) / rowHeight));
            for (int index = first; index <= last; index++) {
                int rowY = innerY() + index * rowHeight - (int) Math.round(view);
                if (index == selectedIndex) {
                    GaldurRender.drawFill(guiGraphics, innerX(), rowY, rowsWidth, rowHeight,
                            active ? GaldurTheme.accent() : GaldurTheme.background());
                } else if (index == hovered) {
                    GaldurRender.drawFill(guiGraphics, innerX(), rowY, rowsWidth, rowHeight,
                            (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
                }
                int textY = rowY + (rowHeight - GaldurRender.lineHeight()) / 2 + 1;
                GaldurRender.text(guiGraphics, rows.get(index), innerX() + TEXT_PADDING, textY, textColor, false);
            }
        });

        if (hasScrollbar()) {
            int barWidth = GaldurTheme.scrollbarWidth();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_TRACK, scrollbarX(), innerY(), barWidth,
                    innerHeight(), GaldurTheme.background());
            int thumbColor = draggingThumb ? GaldurTheme.accentHover() : GaldurTheme.textMuted();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_THUMB, scrollbarX(), thumbY(), barWidth,
                    thumbHeight(), thumbColor);
        }

        if (active && (keyboardFocused || draggingThumb)) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.accentHover());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean over = mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
        if (!visible || !over || !hasScrollbar()) {
            return false;
        }
        setScroll(scroll - scrollY * GaldurTheme.listRowHeight());
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isOverScrollbar(mouseX, mouseY)) {
            draggingThumb = true;
            int thumbTop = thumbY();
            if (mouseY < thumbTop || mouseY >= thumbTop + thumbHeight()) {
                scrollThumbTo(mouseY - thumbHeight() / 2.0);
            }
            return;
        }
        int index = rowAt(mouseX, mouseY);
        if (index >= 0 && index != selected.getAsInt()) {
            onSelect.accept(index);
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (draggingThumb) {
            int travel = innerHeight() - thumbHeight();
            if (travel > 0) {
                setScroll(scroll + dragY * maxScroll() / travel);
            }
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        draggingThumb = false;
    }

    /** Puts the top of the thumb at {@code thumbTop} and scrolls to match. */
    private void scrollThumbTo(double thumbTop) {
        int travel = innerHeight() - thumbHeight();
        if (travel > 0) {
            setScroll((thumbTop - innerY()) * maxScroll() / travel);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) {
            return false;
        }
        int count = lines.get().size();
        if (count == 0 || (keyCode != GLFW.GLFW_KEY_UP && keyCode != GLFW.GLFW_KEY_DOWN)) {
            return false;
        }
        int current = selected.getAsInt();
        int next;
        if (current < 0 || current >= count) {
            next = keyCode == GLFW.GLFW_KEY_DOWN ? 0 : count - 1;
        } else {
            next = Mth.clamp(current + (keyCode == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, count - 1);
        }
        if (next != current) {
            onSelect.accept(next);
        }
        scrollToLine(next);
        return true;
    }

    /** A list does not play the button sound when a line is clicked. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Selected line: "Iron", then "Row 2 of 5 selected" (vanilla text).
        // Otherwise: "List: 5 rows".
        List<Component> rows = lines.get();
        int index = selected.getAsInt();
        Component title;
        if (index >= 0 && index < rows.size()) {
            title = rows.get(index);
            narrationElementOutput.add(NarratedElementType.POSITION,
                    Component.translatable("narrator.position.list", index + 1, rows.size()));
        } else {
            title = rows.isEmpty() ? GaldurNarration.ours("list.empty") : GaldurNarration.ours("list", rows.size());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
        if (active && !rows.isEmpty()) {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.selection.usage"));
        }
    }
}
