package com.galdurstudio.galdurui.api.window;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Handle to a search field of a {@link GaldurWindow}: its text, and the action called on every
 * keystroke.
 *
 * <p>Returned by {@code GaldurWindow.addSearch(...)}. As with {@link CheckboxHandle}, the state
 * lives here, not in the shown widget: the text survives resizes, and {@link #setText(String)}
 * changes it even while the window is open, without rebuilding the screen.</p>
 *
 * <pre>{@code
 * SearchHandle search = window.addSearch(Component.literal("Search..."));
 * ListHandle results = window.addList(6);
 * search.onChange(text -> results.setLines(filter(all, text)));
 * }</pre>
 *
 * <p>Like any {@link ElementHandle}, the field can be disabled, hidden or given a tooltip. Call on
 * the client thread.</p>
 */
public class SearchHandle extends ElementHandle {
    private String text = "";
    private Consumer<String> action;

    /** Package-private: only {@link GaldurWindow} creates fields. */
    SearchHandle() {
    }

    /** {@return the typed text, never {@code null}; empty at first} */
    public String getText() {
        return text;
    }

    /**
     * Changes the field text. When the value really changes, the action given to
     * {@link #onChange(Consumer)} is called, whether the change comes from the player or from code.
     *
     * @param text the new text; {@code null} counts as empty
     */
    public void setText(String text) {
        String value = Objects.requireNonNullElse(text, "");
        if (value.equals(this.text)) {
            return;
        }
        this.text = value;
        if (action != null) {
            action.accept(value);
        }
    }

    /**
     * Sets the action called when the text changes, on every keystroke. Only one action is kept:
     * a new call replaces the previous one.
     *
     * @param action receives the new text
     */
    public void onChange(Consumer<String> action) {
        this.action = action;
    }
}
