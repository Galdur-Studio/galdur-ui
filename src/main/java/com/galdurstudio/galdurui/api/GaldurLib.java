package com.galdurstudio.galdurui.api;

import com.galdurstudio.galdurui.api.window.GaldurWindow;

/**
 * Entry point of the Galdur UI API.
 *
 * <pre>{@code
 * GaldurWindow window = GaldurLib.createWindow();
 * window.addTitle(Component.literal("Storage"), true);
 * window.addButton(Component.literal("Sort"), () -> sort());
 * window.open();
 * }</pre>
 */
public final class GaldurLib {
    private GaldurLib() {}

    /** {@return a new, empty and closed window} */
    public static GaldurWindow createWindow() {
        return new GaldurWindow();
    }
}
