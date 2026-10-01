package dev.lootfeed.client.mixin;

import dev.lootfeed.client.DrawCanvas;
import dev.lootfeed.client.LootFeedClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class HudMixin {

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(F)V", shift = At.Shift.AFTER))
    private void lootfeed$drawFeed(CallbackInfo info) {
        if (LootFeedClient.visible()) {
            LootFeedClient.render(DrawCanvas.begin());
        }
    }
}
