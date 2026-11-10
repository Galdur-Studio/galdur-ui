package com.galdurstudio.galdurui.internal.bridge;

import com.galdurstudio.galdurui.GaldurUI;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Development check of the bridge mixins: with {@code -Dgaldurui.checkMixins=true} (set by the
 * dev runs), the classes they change are loaded at startup, so a mixin that no longer applies
 * stops the game right away instead of when a screen opens.
 */
public final class BridgeCheck {
    private BridgeCheck() {
    }

    public static void runIfAsked() {
        if (!Boolean.getBoolean("galdurui.checkMixins")) {
            return;
        }
        List<Class<?>> targets = List.of(AbstractButton.class, AbstractSliderButton.class, Checkbox.class,
                EditBox.class, Screen.class, AbstractContainerScreen.class, GuiGraphics.class);
        GaldurUI.LOGGER.info("Bridge mixins applied to {} classes.", targets.size());
    }
}
