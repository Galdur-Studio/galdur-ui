package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.ButtonElement;
import com.galdurstudio.galdurui.internal.window.CheckboxElement;
import com.galdurstudio.galdurui.internal.window.IconButtonElement;
import com.galdurstudio.galdurui.internal.window.TextElement;
import com.galdurstudio.galdurui.internal.window.WindowElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Handle to a row of a {@link GaldurWindow}: elements placed side by side, left to right, instead
 * of stacked.
 *
 * <p>Returned by {@code GaldurWindow.addRow()}. Elements are added as in the window, and each
 * returns its own handle. The elements of a row are centred vertically on each other and
 * separated by the theme gap ({@code spacing.gap}).</p>
 *
 * <pre>{@code
 * RowHandle actions = window.addRow(true);
 * actions.addButton(Component.literal("Confirm"), this::confirm);
 * actions.addButton(Component.literal("Cancel"), window::close);
 * }</pre>
 *
 * <p>Like any {@link ElementHandle}, the row can be disabled, hidden or given a tooltip all at
 * once:</p>
 * <ul>
 *   <li><strong>disabled</strong>, it disables all its elements;</li>
 *   <li><strong>hidden</strong>, it disappears with its elements and frees its space; a row whose
 *       elements are all hidden frees its space too;</li>
 *   <li>its <strong>tooltip</strong> shows on the elements that have none of their own
 *       (including in place of an icon button's label).</li>
 * </ul>
 *
 * <p>An element added while the window is shown appears on the next frame. Call on the client
 * thread. A row does not contain another row.</p>
 */
public class RowHandle extends ElementHandle {
    private final WindowModel children;

    /**
     * Package-private: only {@link GaldurWindow} creates rows.
     *
     * @param children the row's elements, shared with the shown element
     */
    RowHandle(WindowModel children) {
        this.children = children;
    }

    /**
     * Adds a text at the end of the row.
     *
     * @param text the text to show
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addText(Component text) {
        ElementHandle handle = new ElementHandle();
        return add(new TextElement(text, false, handle), handle);
    }

    /**
     * Adds a button at the end of the row.
     *
     * @param label  the button label
     * @param action run when the button is activated, by mouse or keyboard
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addButton(Component label, Runnable action) {
        ElementHandle handle = new ElementHandle();
        return add(new ButtonElement(label, action, false, handle), handle);
    }

    /**
     * Adds an icon button showing an item at the end of the row.
     *
     * @param icon   the shown item; copied
     * @param label  tooltip and narration text (not drawn on the button)
     * @param action run when the button is activated, by mouse or keyboard
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ItemStack icon, Component label, Runnable action) {
        ElementHandle handle = new ElementHandle();
        return add(new IconButtonElement(null, icon.copy(), label, action, false, handle), handle);
    }

    /**
     * Adds an icon button showing a GUI sprite at the end of the row, tinted with the theme text
     * colour.
     *
     * @param sprite sprite id in the GUI atlas, in grayscale
     * @param label  tooltip and narration text (not drawn on the button)
     * @param action run when the button is activated, by mouse or keyboard
     * @return the element's handle (disable, hide, tooltip)
     */
    public ElementHandle addIconButton(ResourceLocation sprite, Component label, Runnable action) {
        ElementHandle handle = new ElementHandle();
        return add(new IconButtonElement(sprite, ItemStack.EMPTY, label, action, false, handle), handle);
    }

    /**
     * Adds a checkbox at the end of the row and returns its handle.
     *
     * @param label        the text next to the box
     * @param defaultState {@code true} if the checkbox is checked on open
     * @return the checkbox handle
     */
    public CheckboxHandle addCheckbox(Component label, boolean defaultState) {
        CheckboxHandle handle = new CheckboxHandle(defaultState);
        return add(new CheckboxElement(label, handle, false), handle);
    }

    /**
     * Adds an element to the row and wires its handle to the row's: a change to the element thus
     * reaches the window.
     *
     * @return the element's handle, returned as is to the mod
     */
    private <H extends ElementHandle> H add(WindowElement element, H handle) {
        handle.setChangeListener(this::changed);
        children.add(element);
        changed();
        return handle;
    }
}
