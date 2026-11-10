package com.galdurstudio.galdurui.internal.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the panel position and size of a container screen, which Minecraft keeps protected. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int galdurui$leftPos();

    @Accessor("topPos")
    int galdurui$topPos();

    @Accessor("imageWidth")
    int galdurui$imageWidth();

    @Accessor("imageHeight")
    int galdurui$imageHeight();
}
