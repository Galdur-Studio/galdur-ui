package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * Description of a window element (text, button, checkbox...).
 *
 * <p>An element does <strong>not</strong> hold a widget: it only describes what to show.
 * Minecraft calls {@code Screen.init()} again on every game window resize and widgets are then
 * rebuilt, so {@link WindowScreen} rebuilds them from these descriptions, which survive.</p>
 *
 * <p>Internal to Galdur UI: mods do not use this type, they go through
 * {@link com.galdurstudio.galdurui.api.window.GaldurWindow}.</p>
 */
public interface WindowElement {
    /**
     * {@return the element's common state (disabled, hidden, tooltip), shared with the mod}
     *
     * <p>{@link WindowScreen} reads it on every build: a hidden element is left out of the
     * layout, and the disabled state and tooltip are applied to the created widget.</p>
     */
    ElementHandle handle();

    /**
     * Creates a new widget from this description.
     *
     * <p>Called on every screen {@code init()}: it must return a new instance on every call,
     * never a widget already given to the screen.</p>
     *
     * <p>The enabled state comes from {@link WindowScreen} instead of {@link #handle()}: an
     * element in a disabled row is disabled too, even when its own handle is enabled.</p>
     *
     * @param enabled {@code true} if the element and everything containing it are enabled
     * @return the widget, ready to be placed in a layout
     */
    LayoutElement createWidget(boolean enabled);

    /**
     * Sets how the element is placed in the layout (alignment, padding...).
     *
     * <p>{@link WindowScreen} calls this for each element when adding it to the layout, with its
     * settings. By default it changes nothing: the element keeps Minecraft's settings. An element
     * only overrides the settings it cares about.</p>
     *
     * @param settings this element's layout settings, to modify directly
     */
    default void adjustSettings(LayoutSettings settings) {}
}
