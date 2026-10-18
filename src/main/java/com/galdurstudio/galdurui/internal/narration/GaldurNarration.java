package com.galdurstudio.galdurui.internal.narration;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Texts read by Minecraft's narrator (Ctrl+B) for Galdur UI widgets.
 *
 * <p>Everything goes through translation keys: Minecraft's own when it has one for the same thing
 * ("Button: %s", "Tab %s of %s"...), otherwise ours, prefixed {@code narration.galdurui.} and
 * translated in {@code assets/galdurui/lang}. The narrator thus speaks the player's language, as
 * for vanilla screens.</p>
 *
 * <p>How it works: each widget fills typed entries ({@code TITLE}: what it is, {@code POSITION}:
 * where you are, {@code USAGE}: how to use it, {@code HINT}: the tooltip). Minecraft only reads
 * again what changed since the last reading: hovering another cell of a grid only reads the
 * cell.</p>
 */
public final class GaldurNarration {
    private static final String PREFIX = "narration." + GaldurUI.MODID + ".";

    private GaldurNarration() {
    }

    /** {@return the translation of {@code narration.galdurui.<key>}} */
    public static Component ours(String key, Object... args) {
        return Component.translatable(PREFIX + key, args);
    }

    /**
     * {@return {@code title} followed by "disabled" when the widget is inactive, as is otherwise}
     *
     * @param title  what the widget already reads
     * @param active the widget state
     */
    public static Component withState(Component title, boolean active) {
        return active ? title : CommonComponents.joinForNarration(title, ours("disabled"));
    }

    /**
     * Adds the usage hint, for the keyboard when the widget has focus, for the mouse otherwise.
     * Nothing for an inactive widget: it reacts to nothing.
     *
     * @param output     the widget output
     * @param active     the widget state
     * @param focused    {@code true} if the widget has keyboard focus
     * @param focusedKey translation key of the keyboard hint
     * @param hoveredKey translation key of the mouse hint
     */
    public static void usage(NarrationElementOutput output, boolean active, boolean focused,
                             String focusedKey, String hoveredKey) {
        if (active) {
            output.add(NarratedElementType.USAGE, Component.translatable(focused ? focusedKey : hoveredKey));
        }
    }

    /**
     * {@return an item name followed by its full amount ("Iron Ingot, 1 234"), or the name alone
     * for an amount of 1 or less, as displayed}
     *
     * @param stack  the item
     * @param amount the shown amount
     */
    public static Component item(ItemStack stack, long amount) {
        if (amount <= 1) {
            return stack.getHoverName();
        }
        return ours("amount", stack.getHoverName(), GaldurFormat.fullAmount(amount));
    }
}
