package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.*;
import net.minecraft.client.Minecraft;

/**
 * A screen described by a mod's code: title, texts, buttons, checkboxes, lists, tabs...
 *
 * <p>Created with {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}. It only holds
 * the description of the screen; layout, theme and rendering are handled by Galdur UI, and no
 * coordinate ever needs computing.</p>
 *
 * <p>The {@code add...(...)} methods inherited from {@link ElementContainer} add elements top to
 * bottom, in call order. An element added while the window is shown appears on the next frame
 * (call on the client thread).</p>
 */
public class GaldurWindow extends ElementContainer {
    private final WindowModel model = new WindowModel();

    /** Screen shown right now, or {@code null} when the window is not on screen. */
    private WindowScreen screen;

    /**
     * Screen created by {@link #open()} but not shown yet: {@code open()} goes through
     * {@code Minecraft.tell}, which only runs on the next client loop.
     */
    private WindowScreen pending;

    /** True when a screen rebuild is already scheduled (see {@link #requestRebuild()}). */
    private boolean rebuildScheduled;

    /**
     * Creates an empty window. Prefer {@code GaldurLib.createWindow()}.
     */
    public GaldurWindow() {
    }

    /**
     * Shows the window. No effect if it is already open or opening.
     *
     * <p>The screen shown until then is remembered: {@link #close()} and Escape go back to it.</p>
     */
    public void open() {
        if (screen != null || pending != null) {
            return;
        }
        WindowScreen created = new WindowScreen(model, this::shown, this::hidden);
        pending = created;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.tell(() -> {
            if (pending != created) {
                return;
            }
            pending = null;
            created.setPrevious(minecraft.screen);
            minecraft.setScreen(created);
        });
    }

    /**
     * Closes the window and goes back to the previous screen, exactly like Escape. No effect when
     * it is closed.
     */
    public void close() {
        if (pending != null) {
            pending = null;
            return;
        }
        WindowScreen current = screen;
        if (current != null && Minecraft.getInstance().screen == current) {
            current.onClose();
        }
    }

    /** {@return {@code true} if the window is on screen or opening} */
    public boolean isOpen() {
        return screen != null || pending != null;
    }

    /** The screen becomes the current one (opening, or back from a screen opened on top). */
    private void shown(WindowScreen shownScreen) {
        screen = shownScreen;
    }

    /** The screen is no longer the current one, whatever the reason (Escape, close(), other screen...). */
    private void hidden(WindowScreen hiddenScreen) {
        if (screen == hiddenScreen) {
            screen = null;
        }
    }

    /**
     * Adds an element to the model, wires its handle and, if the window is shown, rebuilds it.
     *
     * @return the element's handle, returned as is to the mod
     */
    @Override
    <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(this::requestRebuild);
        model.add(element);
        requestRebuild();
        return handle;
    }

    /**
     * Rebuilds the screen on the next client loop, if it is shown.
     *
     * <p>Deferred with {@code Minecraft.tell}: a mod often changes a state from a button action,
     * so while the screen iterates over its widgets; replacing them at that moment would be
     * risky. Several changes in the same frame give a single rebuild.</p>
     */
    private void requestRebuild() {
        if (screen == null || rebuildScheduled) {
            return;
        }
        rebuildScheduled = true;
        Minecraft.getInstance().tell(() -> {
            rebuildScheduled = false;
            if (screen != null) {
                screen.rebuild();
            }
        });
    }

    /**
     * Adds a left-aligned tab row at the end of the window and returns its handle; see
     * {@link #addTabs(boolean)} to centre it.
     *
     * <p>Each tab has its own content, filled like a window. Only the active tab's content is
     * shown, below the row; the window keeps the size of the largest content, so it does not
     * resize when switching tabs.</p>
     *
     * <pre>{@code
     * TabsHandle tabs = window.addTabs();
     * TabHandle storage = tabs.addTab(Component.literal("Storage"));
     * storage.addSearch(Component.literal("Search..."));
     * TabHandle crafting = tabs.addTab(Component.literal("Crafting"));
     * crafting.addText(Component.literal("3 × 3 grid"));
     * }</pre>
     *
     * @return the tabs handle, used to add tabs and pick the active one
     */
    public TabsHandle addTabs() {
        return addTabs(false);
    }

    /**
     * Adds a tab row at the end of the window, centred or left-aligned, and returns its handle.
     *
     * @param centered {@code true} to centre it in the window, {@code false} to align it left
     * @return the tabs handle
     */
    public TabsHandle addTabs(boolean centered) {
        TabsModel tabs = new TabsModel();
        TabsHandle handle = new TabsHandle(tabs);
        return add(new TabsElement(tabs, handle, centered), handle);
    }
}
