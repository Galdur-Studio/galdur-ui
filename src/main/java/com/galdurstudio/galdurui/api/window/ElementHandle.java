package com.galdurstudio.galdurui.api.window;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Handle to an element of a {@link GaldurWindow}: disable it, hide it or give it a tooltip,
 * before or after the window opens.
 *
 * <p>Returned by every {@code GaldurWindow.add...(...)}. As with {@link CheckboxHandle}, the state
 * lives here, not in the shown widget: Minecraft rebuilds widgets on every resize, and they read
 * this state back.</p>
 *
 * <pre>{@code
 * ElementHandle confirm = window.addButton(Component.literal("Confirm"), this::confirm);
 * confirm.setEnabled(false);
 * confirm.setTooltip(Component.literal("Fill in the form first"));
 *
 * ElementHandle details = window.addText(Component.literal("Details..."));
 * details.setVisible(false);   // the element disappears and frees its space
 * }</pre>
 *
 * <ul>
 *   <li><strong>Disabled</strong>: drawn in a muted colour, ignores mouse and keyboard. The
 *       tooltip still shows on hover, to explain why.</li>
 *   <li><strong>Hidden</strong>: not drawn and <strong>frees its space</strong>; the window lays
 *       itself out again without it.</li>
 *   <li><strong>Tooltip</strong>: text shown when the element is hovered.</li>
 * </ul>
 *
 * <p>A change made while the window is shown is visible on the next frame. Call on the client
 * thread.</p>
 */
public class ElementHandle {
    private boolean enabled = true;
    private boolean visible = true;
    @Nullable
    private Component tooltip;

    /** Tells the window a state changed; set by {@link GaldurWindow}. */
    private Runnable onChange = () -> {};

    /** Package-private: only {@link GaldurWindow} and {@link RowHandle} create handles. */
    ElementHandle() {
    }

    /** Wires the window (or row) to update when the state changes. */
    void setChangeListener(Runnable onChange) {
        this.onChange = onChange;
    }

    /** Tells the window the element changed and it must rebuild. */
    void changed() {
        onChange.run();
    }

    /**
     * Enables or disables the element. No effect if it is already in that state.
     *
     * @param enabled {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            changed();
        }
    }

    /** {@return {@code true} if the element is enabled (the default)} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shows or hides the element. Hidden, it frees its space in the window. No effect if it is
     * already in that state.
     *
     * @param visible {@code false} to hide it
     */
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            changed();
        }
    }

    /** {@return {@code true} if the element is shown (the default)} */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the text shown when the element is hovered.
     *
     * @param tooltip the tooltip text, or {@code null} to remove it
     */
    public void setTooltip(@Nullable Component tooltip) {
        if (!Objects.equals(tooltip, this.tooltip)) {
            this.tooltip = tooltip;
            changed();
        }
    }

    /** {@return the tooltip text, or {@code null} if none} */
    @Nullable
    public Component getTooltip() {
        return tooltip;
    }
}
