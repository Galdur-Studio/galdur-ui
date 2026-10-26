package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;

/**
 * Handle to one tab of a tab row ({@link TabsHandle}): its content, filled like a window with the
 * {@code add...(...)} methods inherited from {@link ElementContainer}.
 *
 * <pre>{@code
 * TabHandle storage = tabs.addTab(Component.literal("Storage"));
 * SearchHandle search = storage.addSearch(Component.literal("Search..."));
 * ListHandle items = storage.addList(6);
 * }</pre>
 *
 * <p>The content is only shown while the tab is active, but its handles stay usable at any time:
 * a change in a hidden tab shows when the player comes back. A tab does not contain another tab
 * row.</p>
 *
 * <p>Call on the client thread.</p>
 */
public class TabHandle extends ElementContainer {
    private final TabsHandle owner;
    private final WindowModel content;
    private final int index;

    /**
     * Package-private: only {@link TabsHandle} creates tabs.
     *
     * @param owner   the tab row, notified of every content change
     * @param content the tab's elements, shared with the shown element
     * @param index   the tab position in the row
     */
    TabHandle(TabsHandle owner, WindowModel content, int index) {
        this.owner = owner;
        this.content = content;
        this.index = index;
    }

    /**
     * Adds an element to the content and wires its handle to the tab row: a change to the element
     * thus reaches the window.
     *
     * @return the element's handle, returned as is to the mod
     */
    @Override
    <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(owner::changed);
        content.add(element);
        owner.changed();
        return handle;
    }

    /** {@return the tab position in its row, starting at 0} */
    public int getIndex() {
        return index;
    }

    /** {@return {@code true} if this tab is the active one} */
    public boolean isSelected() {
        return owner.getSelected() == index;
    }

    /** Makes this tab active; same as {@code setSelected(getIndex())} on the row. */
    public void select() {
        owner.setSelected(index);
    }
}
