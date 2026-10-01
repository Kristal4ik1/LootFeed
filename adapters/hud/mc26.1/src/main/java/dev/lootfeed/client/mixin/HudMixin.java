package dev.lootfeed.client.mixin;

import dev.lootfeed.client.DrawCanvas;
import dev.lootfeed.client.LootFeedClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class HudMixin {

    @Unique
    private GuiGraphicsExtractor lootfeed$graphics;

    @ModifyArg(
            method = "extractGui",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            index = 0)
    private GuiGraphicsExtractor lootfeed$rememberGraphics(GuiGraphicsExtractor graphics) {
        lootfeed$graphics = graphics;
        return graphics;
    }

    @Inject(
            method = "extractGui",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER))
    private void lootfeed$drawFeed(CallbackInfo info) {
        if (lootfeed$graphics != null && LootFeedClient.visible()) {
            LootFeedClient.render(DrawCanvas.begin(lootfeed$graphics));
        }
    }
}
