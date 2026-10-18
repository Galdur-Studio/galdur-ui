package com.galdurstudio.galdurui.internal.window;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Content of a tab row: the title and elements of each tab, in order.
 *
 * <p>Shared between the public handle ({@link com.galdurstudio.galdurui.api.window.TabsHandle}),
 * which adds tabs to it, and {@link TabsElement}, which {@link WindowScreen} reads to build the
 * display.</p>
 */
public class TabsModel {
    private final List<Component> titles = new ArrayList<>();
    private final List<WindowModel> contents = new ArrayList<>();

    /**
     * Adds a tab at the end.
     *
     * @param title   the tab title
     * @param content the tab's elements
     */
    public void add(Component title, WindowModel content) {
        titles.add(title);
        contents.add(content);
    }

    /** {@return the tab titles, in order; unmodifiable copy} */
    public List<Component> titles() {
        return List.copyOf(titles);
    }

    /** {@return each tab's content, in order; unmodifiable copy} */
    public List<WindowModel> contents() {
        return List.copyOf(contents);
    }
}
