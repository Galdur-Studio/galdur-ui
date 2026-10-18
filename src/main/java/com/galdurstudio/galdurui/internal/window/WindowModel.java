package com.galdurstudio.galdurui.internal.window;

import java.util.ArrayList;
import java.util.List;

/**
 * Content of a window: the ordered list of {@link WindowElement}s added by the mod.
 *
 * <p>{@link com.galdurstudio.galdurui.api.window.GaldurWindow} keeps it in a private field and
 * hands it to {@link WindowScreen} on open, so the screen can read the content without
 * {@code GaldurWindow} exposing a public getter.</p>
 */
public class WindowModel {
    private final List<WindowElement> elements = new ArrayList<>();

    /**
     * Adds an element at the end of the window.
     *
     * @param element the element to add
     */
    public void add(WindowElement element) {
        elements.add(element);
    }

    /**
     * Returns the elements in insertion order, top to bottom on screen.
     *
     * @return an unmodifiable copy of the list; changing it does not change the window
     */
    public List<WindowElement> elements() {
        return List.copyOf(elements);
    }
}
