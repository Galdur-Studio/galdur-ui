package com.galdurstudio.galdurui.internal.mixin;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//? if >=26.1
//import com.mojang.renderpearl.api.pipeline.RenderPipeline;

/**
 * Buttons and cycle buttons: the vanilla button sprite becomes the Galdur one in a restyled
 * screen. Buttons drawing their own picture (icons, checkboxes) are not affected.
 */
@Mixin(AbstractButton.class)
abstract class AbstractButtonMixin {
    //? if >=26.1 {
    /*@Redirect(method = "extractDefaultSprite", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void galdurui$background(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation sprite,
                                     int x, int y, int width, int height, int color) {
        if (BridgePainter.active(GaldurBridge.Element.BUTTONS)) {
            BridgePainter.button(graphics, (AbstractWidget) (Object) this, x, y, width, height);
        } else {
            graphics.blitSprite(pipeline, sprite, x, y, width, height, color);
        }
    }
    *///?} elif >=1.20.2 {
    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void galdurui$background(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (BridgePainter.active(GaldurBridge.Element.BUTTONS)) {
            BridgePainter.button(graphics, (AbstractWidget) (Object) this, x, y, width, height);
        } else {
            graphics.blitSprite(sprite, x, y, width, height);
        }
    }
    //?} else {
    /*@Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitNineSliced(Lnet/minecraft/resources/ResourceLocation;IIIIIIIIII)V"))
    private void galdurui$background(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                     int border, int cornerWidth, int cornerHeight, int textureWidth, int u, int v) {
        if (BridgePainter.active(GaldurBridge.Element.BUTTONS)) {
            BridgePainter.button(graphics, (AbstractWidget) (Object) this, x, y, width, height);
        } else {
            graphics.blitNineSliced(texture, x, y, width, height, border, cornerWidth, cornerHeight, textureWidth, u, v);
        }
    }
    *///?}
}
