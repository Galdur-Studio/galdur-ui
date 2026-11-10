package com.galdurstudio.galdurui.internal.mixin;

import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import com.galdurstudio.galdurui.internal.bridge.InventoryPainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Screens with slots restyled with {@code Background.REPLACE}: the Galdur panel and slot frames
 * replace the screen's background ({@code renderBg}), and its labels take the theme text colour.
 * Since 26.1 the background is asked for apart: see {@link ScreenMixin}.
 */
@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Shadow
    protected abstract void renderLabels(GuiGraphics graphics, int mouseX, int mouseY);

    //? if >=26.1 {
    /*@Redirect(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void galdurui$labels(AbstractContainerScreen<?> self, GuiGraphics graphics, int mouseX, int mouseY) {
        galdurui$drawLabels(self, graphics, mouseX, mouseY);
    }
    *///?} elif >=1.20.2 {
    // The panel is drawn in renderBackground from 1.20.2, in render before.
    @Shadow
    protected abstract void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY);

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void galdurui$labels(AbstractContainerScreen<?> self, GuiGraphics graphics, int mouseX, int mouseY) {
        galdurui$drawLabels(self, graphics, mouseX, mouseY);
    }

    @Redirect(method = "renderBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void galdurui$background(AbstractContainerScreen<?> self, GuiGraphics graphics, float partialTick,
                                     int mouseX, int mouseY) {
        if (BridgePainter.replaceBackground() && !InventoryPainter.paintsItself(self)) {
            BridgePainter.containerBackground(graphics, self, (AbstractContainerScreenAccessor) self);
        } else {
            renderBg(graphics, partialTick, mouseX, mouseY);
        }
    }
    //?} else {
    /*@Shadow
    protected abstract void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY);

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void galdurui$labels(AbstractContainerScreen<?> self, GuiGraphics graphics, int mouseX, int mouseY) {
        galdurui$drawLabels(self, graphics, mouseX, mouseY);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void galdurui$background(AbstractContainerScreen<?> self, GuiGraphics graphics, float partialTick,
                                     int mouseX, int mouseY) {
        if (BridgePainter.replaceBackground() && !InventoryPainter.paintsItself(self)) {
            BridgePainter.containerBackground(graphics, self, (AbstractContainerScreenAccessor) self);
        } else {
            renderBg(graphics, partialTick, mouseX, mouseY);
        }
    }
    *///?}

    private void galdurui$drawLabels(AbstractContainerScreen<?> self, GuiGraphics graphics, int mouseX, int mouseY) {
        if (BridgePainter.onGaldurPanel(self)) {
            BridgePainter.themedLabels(() -> renderLabels(graphics, mouseX, mouseY));
        } else {
            renderLabels(graphics, mouseX, mouseY);
        }
    }
}
