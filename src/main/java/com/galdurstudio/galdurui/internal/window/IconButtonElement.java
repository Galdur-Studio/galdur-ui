package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurIconButton;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Window element showing an icon button, with a sprite or an item.
 *
 * @param sprite   the sprite to show, or {@code null} to show {@code item}
 * @param item     the item shown when there is no sprite (copied by the caller)
 * @param label    tooltip and narration text
 * @param action   run when the button is activated
 * @param centered {@code true} to centre the button, {@code false} to align it left
 * @param handle   the common state (disabled, hidden, tooltip), shared with the mod
 */
public record IconButtonElement(@Nullable ResourceLocation sprite, ItemStack item, Component label,
                                Runnable action, boolean centered, ElementHandle handle) implements WindowElement {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        return sprite != null
                ? GaldurIconButton.of(sprite, label, button -> action.run())
                : GaldurIconButton.ofItem(item, label, button -> action.run());
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
