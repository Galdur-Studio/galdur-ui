package com.galdurstudio.galdurui.api.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;

/**
 * Handle to a scrolling list of a {@link GaldurWindow}: its lines, the selected line and the
 * scroll position.
 *
 * <p>Returned by {@code GaldurWindow.addList(...)}. As with {@link CheckboxHandle}, the state
 * lives here, not in the shown widget: it survives resizes, and the lines can change at any time,
 * even while the window is open, without rebuilding the screen.</p>
 *
 * <pre>{@code
 * ListHandle ores = window.addList(5);
 * ores.addLine(Component.literal("Iron"));
 * ores.addLine(Component.literal("Gold"));
 * ores.onSelect(index -> System.out.println("Chosen: " + index));
 * }</pre>
 *
 * <p>The list width is computed when the screen is built, from the longest line. A longer line
 * added later is clipped at the edge until the next rebuild (resize, or an element added to the
 * window). Like any {@link ElementHandle}, the list can be disabled, hidden or given a
 * tooltip.</p>
 *
 * <p>Call on the client thread.</p>
 */
public class ListHandle extends ElementHandle {
    private final List<Component> lines = new ArrayList<>();
    private final List<Component> linesView = Collections.unmodifiableList(lines);
    private int selected = -1;
    private double scroll;
    private IntConsumer action;

    /** Package-private: only {@link GaldurWindow} creates lists. */
    ListHandle() {
    }

    /**
     * Adds a line at the end of the list.
     *
     * @param line the line text
     */
    public void addLine(Component line) {
        lines.add(line);
    }

    /**
     * Replaces all lines, for instance to show a search result. The selection is cleared, since
     * its index would now point at another line; if there was one, {@link #onSelect(IntConsumer)}
     * is called with {@code -1}.
     *
     * @param newLines the new lines, in display order; the list is copied
     */
    public void setLines(List<Component> newLines) {
        lines.clear();
        lines.addAll(newLines);
        setSelected(-1);
    }

    /** Removes all lines, and the selection with them. */
    public void clearLines() {
        setLines(List.of());
    }

    /** {@return the lines, read-only; the view follows changes to the list} */
    public List<Component> getLines() {
        return linesView;
    }

    /** {@return the index of the selected line, or {@code -1} if none} */
    public int getSelected() {
        return selected;
    }

    /**
     * Selects a line. When the value really changes, the action given to
     * {@link #onSelect(IntConsumer)} is called, whether the change comes from the player or from
     * code.
     *
     * @param index the line index, or {@code -1} for none; an index outside the list counts as
     *              {@code -1}
     */
    public void setSelected(int index) {
        int valid = index >= 0 && index < lines.size() ? index : -1;
        if (valid == selected) {
            return;
        }
        selected = valid;
        if (action != null) {
            action.accept(valid);
        }
    }

    /**
     * Sets the action called when the selection changes. Only one action is kept: a new call
     * replaces the previous one. It receives the line index, or {@code -1}.
     *
     * @param action called on every selection change
     */
    public void onSelect(IntConsumer action) {
        this.action = action;
    }

    /** {@return the scroll offset, in pixels from the top of the content} */
    public double getScroll() {
        return scroll;
    }

    /**
     * Remembers the scroll offset, to restore it when the screen is rebuilt. Called by the shown
     * widget; a mod usually does not need it.
     *
     * @param scroll offset, in pixels from the top of the content
     */
    public void setScroll(double scroll) {
        this.scroll = scroll;
    }
}
