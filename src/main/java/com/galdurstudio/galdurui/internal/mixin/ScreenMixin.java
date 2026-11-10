package com.galdurstudio.galdurui.internal.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
//? if >=26.1 {
/*import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import com.galdurstudio.galdurui.internal.bridge.InventoryPainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
*///?}

/**
 * Since 26.1, Minecraft asks a screen for its background apart from the rest: for a screen with
 * slots restyled with {@code Background.REPLACE}, the dim and the Galdur panel are drawn instead.
 * Empty before 26.1 ({@link AbstractContainerScreenMixin} does it).
 */
@Mixin(Screen.class)
abstract class ScreenMixin {
    //? if >=26.1 {
    /*@Redirect(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void galdurui$background(Screen self, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (self instanceof AbstractContainerScreen<?> container && BridgePainter.replaceBackground()
                && !InventoryPainter.paintsItself(container)) {
            self.renderTransparentBackground(graphics);
            BridgePainter.containerBackground(graphics, container, (AbstractContainerScreenAccessor) container);
        } else {
            self.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }
    *///?}
}
