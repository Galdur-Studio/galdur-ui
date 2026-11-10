package com.galdurstudio.galdurui.internal.mixin;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Text fields: in a restyled screen, a Galdur field is drawn where Minecraft would draw its
 * border, and Minecraft is told the field has none. The text keeps its bordered position, which
 * Minecraft reads from the field, not from this call.
 */
@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Shadow
    private boolean bordered;

    /** Render context of the frame being drawn, for the redirect below. */
    @Unique
    private GuiGraphics galdurui$graphics;

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void galdurui$keepGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
        galdurui$graphics = graphics;
    }

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;isBordered()Z"))
    private boolean galdurui$background(EditBox self) {
        if (!bordered || !BridgePainter.active(GaldurBridge.Element.TEXT_FIELDS) || galdurui$graphics == null) {
            return bordered;
        }
        BridgePainter.field(galdurui$graphics, self, self.getX(), self.getY(), self.getWidth(), self.getHeight());
        return false;
    }
}
