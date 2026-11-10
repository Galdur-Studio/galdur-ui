package com.galdurstudio.galdurui.api.window;

import java.util.function.Consumer;

/**
 * Handle to a checkbox of a {@link GaldurWindow}: read its state, change it and be told when it
 * changes.
 *
 * <p>Returned by {@code GaldurWindow.addCheckbox(...)}. Like any {@link ElementHandle}, it can
 * also disable the checkbox, hide it or give it a tooltip.</p>
 *
 * <p>The state lives here, not in the shown widget: Minecraft rebuilds widgets on every resize,
 * so the checkbox stays checked (or not) and the widget reads this state back.</p>
 *
 * <pre>{@code
 * CheckboxHandle sound = window.addCheckbox(Component.literal("Enable sound"), false);
 * sound.onChange(on -> System.out.println("Sound: " + on));
 * sound.setChecked(true);   // prints "Sound: true"
 * }</pre>
 */
public class CheckboxHandle extends ElementHandle {
    private boolean checked;
    private Consumer<Boolean> action;

    /**
     * Package-private: only {@link GaldurWindow} creates checkboxes.
     *
     * @param defaultState {@code true} if the checkbox starts checked
     */
    CheckboxHandle(boolean defaultState) {
        this.checked = defaultState;
    }

    /**
     * Checks or unchecks the checkbox.
     *
     * <p>When the value really changes, the action given to {@link #onChange(Consumer)} is called
     * with the new value, whether the change comes from the player or from code. Nothing happens
     * when the checkbox is already in that state.</p>
     *
     * @param checked {@code true} to check, {@code false} to uncheck
     */
    public void setChecked(boolean checked) {
        if (checked == this.checked) {
            return;
        }
        this.checked = checked;
        if (action != null) {
            action.accept(checked);
        }
    }

    /** {@return {@code true} if the checkbox is checked} */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Sets the action called when the state changes.
     *
     * <p>Only one action is kept: a new call replaces the previous one. It receives the new state
     * ({@code true} if the checkbox was just checked).</p>
     *
     * @param action called on every state change
     */
    public void onChange(Consumer<Boolean> action) {
        this.action = action;
    }
}
