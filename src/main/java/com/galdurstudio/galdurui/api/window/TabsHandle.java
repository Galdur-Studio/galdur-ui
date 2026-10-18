package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.TabsModel;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;

/**
 * Handle to a tab row of a {@link GaldurWindow}: its tabs and the active tab.
 *
 * <p>Returned by {@code GaldurWindow.addTabs()}. Each {@link #addTab(Component)} returns a
 * {@link TabHandle}, filled like a window. The first tab added starts active.</p>
 *
 * <pre>{@code
 * TabsHandle tabs = window.addTabs(true);
 * TabHandle storage = tabs.addTab(Component.literal("Storage"));
 * TabHandle autocraft = tabs.addTab(Component.literal("Autocraft"));
 * tabs.onSelect(index -> System.out.println("Tab: " + index));
 * autocraft.select();   // or tabs.setSelected(1)
 * }</pre>
 *
 * <p>As with {@link ListHandle}, the state lives here, not in the shown widget: the active tab
 * survives resizes, and each tab's content (search text, list scroll...) is kept when switching
 * tabs.</p>
 *
 * <p>Like any {@link ElementHandle}, the row can be disabled, hidden or given a tooltip:</p>
 * <ul>
 *   <li><strong>disabled</strong>, tabs can no longer be switched, and the shown content is
 *       disabled too;</li>
 *   <li><strong>hidden</strong>, it disappears with all its content and frees its space;</li>
 *   <li>its <strong>tooltip</strong> shows only when the tab row is hovered, not on the
 *       content.</li>
 * </ul>
 *
 * <p>Call on the client thread.</p>
 */
public class TabsHandle extends ElementHandle {
    private final TabsModel model;
    private final List<TabHandle> tabs = new ArrayList<>();
    private final List<TabHandle> tabsView = Collections.unmodifiableList(tabs);
    private int selected;
    private IntConsumer action;

    /**
     * Package-private: only {@link GaldurWindow} creates tab rows.
     *
     * @param model the tab titles and contents, shared with the shown element
     */
    TabsHandle(TabsModel model) {
        this.model = model;
    }

    /**
     * Adds a tab to the right of the others and returns its handle, used to fill its content.
     *
     * @param title the title shown on the tab
     * @return the tab handle
     */
    public TabHandle addTab(Component title) {
        WindowModel content = new WindowModel();
        TabHandle tab = new TabHandle(this, content, tabs.size());
        model.add(title, content);
        tabs.add(tab);
        changed();
        return tab;
    }

    /** {@return the tabs, in order, read-only; the view follows additions} */
    public List<TabHandle> getTabs() {
        return tabsView;
    }

    /** {@return the index of the active tab, or {@code -1} if there is no tab yet} */
    public int getSelected() {
        return tabs.isEmpty() ? -1 : selected;
    }

    /**
     * Makes a tab active. When the value really changes, its content replaces the old one on the
     * next frame and the action given to {@link #onSelect(IntConsumer)} is called, whether the
     * change comes from the player or from code.
     *
     * @param index the tab index; an index outside the tabs is ignored
     */
    public void setSelected(int index) {
        if (index < 0 || index >= tabs.size() || index == selected) {
            return;
        }
        selected = index;
        changed();
        if (action != null) {
            action.accept(index);
        }
    }

    /**
     * Sets the action called when the active tab changes. Only one action is kept: a new call
     * replaces the previous one. It receives the index of the new active tab.
     *
     * @param action called on every tab change
     */
    public void onSelect(IntConsumer action) {
        this.action = action;
    }
}
