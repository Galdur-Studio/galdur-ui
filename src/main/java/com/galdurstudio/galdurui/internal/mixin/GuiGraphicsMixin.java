package com.galdurstudio.galdurui.internal.mixin;

import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import com.galdurstudio.galdurui.internal.bridge.InventoryPainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Two things drawn by Minecraft that the Galdur look takes over:
 * <ul>
 * <li>container labels drawn on a Galdur panel: the vanilla dark grey would not be readable, so it
 * becomes the theme text colour, only while {@link BridgePainter#themedLabels} runs;</li>
 * <li>the textures and sprites of the player and creative inventories, answered one by one by
 * {@link InventoryPainter}.</li>
 * </ul>
 */
@Mixin(GuiGraphics.class)
abstract class GuiGraphicsMixin {
    //? if >=26.1 {
    /*@ModifyVariable(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 2)
    *///?} else {
    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
            at = @At("HEAD"), argsOnly = true, ordinal = 2)
    //?}
    private int galdurui$labelColor(int color) {
        return BridgePainter.labelColor(color);
    }

    //? if >=26.1 {
    /*@Inject(method = "blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
            at = @At("HEAD"), cancellable = true)
    private void galdurui$texture(com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline, ResourceLocation id,
                                  int x, int y, float u, float v, int width, int height, int textureWidth,
                                  int textureHeight, CallbackInfo ci) {
        if (InventoryPainter.texture((GuiGraphics) (Object) this, id, x, y, (int) u, (int) v, width, height)) {
            ci.cancel();
        }
    }

    @Inject(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V",
            at = @At("HEAD"), cancellable = true)
    private void galdurui$sprite(com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline, ResourceLocation id,
                                 int x, int y, int width, int height, CallbackInfo ci) {
        if (InventoryPainter.sprite((GuiGraphics) (Object) this, id, x, y, width, height)) {
            ci.cancel();
        }
    }

    @Inject(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
            at = @At("HEAD"), cancellable = true)
    private void galdurui$spritePart(com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline, ResourceLocation id,
                                     int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width,
                                     int height, CallbackInfo ci) {
        if (InventoryPainter.spritePart((GuiGraphics) (Object) this, id, x, y, width, height)) {
            ci.cancel();
        }
    }
    *///?} else {
    @Inject(method = "blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", at = @At("HEAD"), cancellable = true)
    private void galdurui$texture(ResourceLocation id, int x, int y, int u, int v, int width, int height, CallbackInfo ci) {
        if (InventoryPainter.texture((GuiGraphics) (Object) this, id, x, y, u, v, width, height)) {
            ci.cancel();
        }
    }

    //? if >=1.20.2 {
    @Inject(method = "blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", at = @At("HEAD"), cancellable = true)
    private void galdurui$sprite(ResourceLocation id, int x, int y, int width, int height, CallbackInfo ci) {
        if (InventoryPainter.sprite((GuiGraphics) (Object) this, id, x, y, width, height)) {
            ci.cancel();
        }
    }

    // Part of a sprite: the furnace flame and arrow while they fill up.
    @Inject(method = "blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V", at = @At("HEAD"), cancellable = true)
    private void galdurui$spritePart(ResourceLocation id, int spriteWidth, int spriteHeight, int u, int v, int x, int y,
                                     int width, int height, CallbackInfo ci) {
        if (InventoryPainter.spritePart((GuiGraphics) (Object) this, id, x, y, width, height)) {
            ci.cancel();
        }
    }
    //?}
    //?}
}
