package dev.lootfeed.client.mixin;

import dev.lootfeed.client.DrawCanvas;
import dev.lootfeed.client.LootFeedClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class HudMixin {

    @Unique
    private GuiGraphicsExtractor lootfeed$graphics;

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            index = 0)
    private GuiGraphicsExtractor lootfeed$rememberGraphics(GuiGraphicsExtractor graphics) {
        lootfeed$graphics = graphics;
        return graphics;
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER))
    private void lootfeed$drawFeed(CallbackInfo info) {
        if (lootfeed$graphics != null && LootFeedClient.visible()) {
            LootFeedClient.render(DrawCanvas.begin(lootfeed$graphics));
        }
    }
}
