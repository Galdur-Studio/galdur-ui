package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.ButtonElement;
import com.galdurstudio.galdurui.internal.window.CheckboxElement;
import com.galdurstudio.galdurui.internal.window.IconButtonElement;
import com.galdurstudio.galdurui.internal.window.ListElement;
import com.galdurstudio.galdurui.internal.window.ProgressElement;
import com.galdurstudio.galdurui.internal.window.RowElement;
import com.galdurstudio.galdurui.internal.window.SearchElement;
import com.galdurstudio.galdurui.internal.window.TextElement;
import com.galdurstudio.galdurui.internal.window.TitleElement;
import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Receives elements stacked top to bottom: a {@link GaldurWindow}, or the content of a tab
 * ({@link TabHandle}).
 *
 * <p>Elements are shown in call order. An element added while the window is shown appears on the
 * next frame. Call on the client thread.</p>
 *
 * <p>Mods do not extend this class: they use {@link GaldurWindow} and {@link TabHandle}.</p>
 */
public abstract class ElementContainer {
    /** Package-private: only {@link GaldurWindow} and {@link TabHandle} extend it. */
    ElementContainer() {
    }

    /**
     * Adds an element at the end, wires its handle and notifies whatever must update.
     *
     * @return the element's handle, returned as is to the mod
     */
    abstract <H extends ElementHandle> H add(WindowElement element, H handle);

    /**
     * Adds a left-aligned line of text at the end; see {@link #addText(Component, boolean)} to
     * centre it.
     *
     * @param text the text to show
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addText(Component text) {
        return addText(text, false);
    }

    /**
     * Adds a line of text at the end, centred or left-aligned.
     *
     * @param text     the text to show
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addText(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, centered, handle), handle);
    }

    /**
     * Adds a left-aligned title at the end; see {@link #addTitle(Component, boolean)} to centre
     * it.
     *
     * <p>A title differs from plain text by its style. It usually comes first, but nothing
     * enforces it: elements stay in call order.</p>
     *
     * @param text the title to show
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addTitle(Component text) {
        return addTitle(text, false);
    }

    /**
     * Adds a title at the end, centred or left-aligned.
     *
     * @param text     the title to show
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addTitle(Component text, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new TitleElement(text, centered, handle), handle);
    }

    /**
     * Adds a left-aligned button at the end; see {@link #addButton(Component, Runnable, boolean)}
     * to centre it.
     *
     * <p>The action runs every time the button is activated, by mouse or keyboard.</p>
     *
     * @param label  the button label
     * @param action run when the button is activated
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addButton(Component label, Runnable action) {
        return addButton(label, action, false);
    }

    /**
     * Adds a button at the end, centred or left-aligned.
     *
     * @param label    the button label
     * @param action   run when the button is activated
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addButton(Component label, Runnable action, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, centered, handle), handle);
    }

    /**
     * Adds a left-aligned icon button showing an item at the end; see
     * {@link #addIconButton(ItemStack, Component, Runnable, boolean)} to centre it.
     *
     * <p>The label is not drawn on the button: it is the tooltip and the narrated text. A tooltip
     * set on the handle replaces it. For several icons side by side, use a row
     * ({@link RowHandle#addIconButton(ItemStack, Component, Runnable)}).</p>
     *
     * <pre>{@code
     * window.addIconButton(new ItemStack(Items.HOPPER), Component.literal("Sort"), this::sort);
     * }</pre>
     *
     * @param icon   the shown item; copied
     * @param label  tooltip and narration text
     * @param action run when the button is activated
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ItemStack icon, Component label, Runnable action) {
        return addIconButton(icon, label, action, false);
    }

    /**
     * Adds an icon button showing an item, centred or left-aligned.
     *
     * @param icon     the shown item; copied
     * @param label    tooltip and narration text
     * @param action   run when the button is activated
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ItemStack icon, Component label, Runnable action, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new IconButtonElement(null, icon.copy(), label, action, centered, handle), handle);
    }

    /**
     * Adds a left-aligned icon button showing a GUI sprite at the end; see
     * {@link #addIconButton(ResourceLocation, Component, Runnable, boolean)} to centre it.
     *
     * <p>The sprite is tinted with the theme text colour: provide it in grayscale. The label is
     * the tooltip and the narrated text.</p>
     *
     * @param sprite sprite id in the GUI atlas
     * @param label  tooltip and narration text
     * @param action run when the button is activated
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ResourceLocation sprite, Component label, Runnable action) {
        return addIconButton(sprite, label, action, false);
    }

    /**
     * Adds an icon button showing a GUI sprite, centred or left-aligned.
     *
     * @param sprite   sprite id in the GUI atlas
     * @param label    tooltip and narration text
     * @param action   run when the button is activated
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ResourceLocation sprite, Component label, Runnable action, boolean centered) {
        ElementHandle handle = new ElementHandle();
        return add(new IconButtonElement(sprite, ItemStack.EMPTY, label, action, centered, handle), handle);
    }

    /**
     * Adds a left-aligned checkbox at the end and returns its handle; see
     * {@link #addCheckbox(Component, boolean, boolean)} to centre it.
     *
     * <p>The handle reads and changes the state, reports changes and, like any
     * {@link ElementHandle}, disables, hides or gives a tooltip to the checkbox.</p>
     *
     * @param label        the text next to the box
     * @param defaultState {@code true} if the checkbox is checked on open
     * @return the checkbox handle
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState) {
        return addCheckbox(label, defaultState, false);
    }

    /**
     * Adds a checkbox at the end, centred or left-aligned.
     *
     * @param label        the text next to the box
     * @param defaultState {@code true} if the checkbox is checked on open
     * @param centered     {@code true} to centre it, {@code false} to align it left
     * @return the checkbox handle
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState, boolean centered) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        return add(new CheckboxElement(label, handle, centered), handle);
    }

    /**
     * Adds a left-aligned row at the end and returns its handle, to place elements side by side;
     * see {@link #addRow(boolean)} to centre it.
     *
     * <pre>{@code
     * RowHandle actions = window.addRow();
     * actions.addButton(Component.literal("Confirm"), this::confirm);
     * actions.addButton(Component.literal("Cancel"), window::close);
     * }</pre>
     *
     * @return the row handle, used to add elements to it
     */
    public RowHandle addRow() {
        return addRow(false);
    }

    /**
     * Adds a row at the end, centred or left-aligned, and returns its handle.
     *
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the row handle, used to add elements to it
     */
    public RowHandle addRow(boolean centered) {
        WindowModel children = new WindowModel();
        RowHandle handle = new RowHandle(children);
        return add(new RowElement(children, centered, handle), handle);
    }

    /**
     * Adds a left-aligned scrolling list at the end and returns its handle, which holds its lines
     * and selection; see {@link #addList(int, boolean)} to centre it.
     *
     * <pre>{@code
     * ListHandle ores = window.addList(5);
     * ores.addLine(Component.literal("Iron"));
     * ores.onSelect(index -> choose(index));
     * }</pre>
     *
     * @param visibleRows number of lines visible at once (at least 1); sets the height
     * @return the list handle
     */
    public ListHandle addList(int visibleRows) {
        return addList(visibleRows, false);
    }

    /**
     * Adds a scrolling list at the end, centred or left-aligned, and returns its handle.
     *
     * @param visibleRows number of lines visible at once (at least 1); sets the height
     * @param centered    {@code true} to centre it, {@code false} to align it left
     * @return the list handle
     */
    public ListHandle addList(int visibleRows, boolean centered) {
        ListHandle handle = new ListHandle();
        return add(new ListElement(handle, visibleRows, centered), handle);
    }

    /**
     * Adds a left-aligned search field at the end and returns its handle, which holds the typed
     * text; see {@link #addSearch(Component, boolean)} to centre it.
     *
     * <pre>{@code
     * SearchHandle search = window.addSearch(Component.literal("Search..."));
     * search.onChange(text -> filter(text));
     * }</pre>
     *
     * @param placeholder hint text, shown while the field is empty
     * @return the field handle
     */
    public SearchHandle addSearch(Component placeholder) {
        return addSearch(placeholder, false);
    }

    /**
     * Adds a search field at the end, centred or left-aligned, and returns its handle.
     *
     * @param placeholder hint text, shown while the field is empty
     * @param centered    {@code true} to centre it, {@code false} to align it left
     * @return the field handle
     */
    public SearchHandle addSearch(Component placeholder, boolean centered) {
        SearchHandle handle = new SearchHandle();
        return add(new SearchElement(placeholder, handle, centered), handle);
    }

    /**
     * Adds a left-aligned progress bar at the end and returns its handle, which holds its
     * progress and text; see {@link #addProgress(Component, boolean)} to centre it.
     *
     * <pre>{@code
     * ProgressHandle craft = window.addProgress(Component.literal("0 / 64"));
     * craft.setProgress(16, 64);
     * }</pre>
     *
     * @param label the text shown on the bar, or {@code null} for none; it also sets the bar width
     *              (at least 100 pixels)
     * @return the bar handle, empty at first
     */
    public ProgressHandle addProgress(@Nullable Component label) {
        return addProgress(label, false);
    }

    /**
     * Adds a progress bar at the end, centred or left-aligned, and returns its handle.
     *
     * @param label    the text shown on the bar, or {@code null} for none
     * @param centered {@code true} to centre it, {@code false} to align it left
     * @return the bar handle, empty at first
     */
    public ProgressHandle addProgress(@Nullable Component label, boolean centered) {
        ProgressHandle handle = new ProgressHandle(label);
        return add(new ProgressElement(handle, centered), handle);
    }
}
