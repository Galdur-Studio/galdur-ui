package com.galdurstudio.galdurui.internal.mixin;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//? if >=26.1
//import com.mojang.renderpearl.api.pipeline.RenderPipeline;

/** Checkboxes: the vanilla box becomes a Galdur one in a restyled screen; the label stays vanilla. */
@Mixin(Checkbox.class)
abstract class CheckboxMixin {
    //? if >=26.1 {
    /*@Redirect(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void galdurui$box(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation sprite,
                              int x, int y, int width, int height, int color) {
        Checkbox self = (Checkbox) (Object) this;
        if (BridgePainter.active(GaldurBridge.Element.CHECKBOXES)) {
            BridgePainter.checkbox(graphics, self, self.selected(), x, y, width, height);
        } else {
            graphics.blitSprite(pipeline, sprite, x, y, width, height, color);
        }
    }
    *///?} elif >=1.20.2 {
    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void galdurui$box(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        Checkbox self = (Checkbox) (Object) this;
        if (BridgePainter.active(GaldurBridge.Element.CHECKBOXES)) {
            BridgePainter.checkbox(graphics, self, self.selected(), x, y, width, height);
        } else {
            graphics.blitSprite(sprite, x, y, width, height);
        }
    }
    //?} else {
    /*@Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void galdurui$box(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v,
                              int width, int height, int textureWidth, int textureHeight) {
        Checkbox self = (Checkbox) (Object) this;
        if (BridgePainter.active(GaldurBridge.Element.CHECKBOXES)) {
            BridgePainter.checkbox(graphics, self, self.selected(), x, y, width, height);
        } else {
            graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        }
    }
    *///?}
}
