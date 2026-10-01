package dev.lootfeed.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lootfeed.client.DrawCanvas;
import dev.lootfeed.client.LootFeedClient;
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
    private PoseStack lootfeed$pose;

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"),
            index = 0)
    private PoseStack lootfeed$rememberPose(PoseStack pose) {
        lootfeed$pose = pose;
        return pose;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
                    shift = At.Shift.AFTER))
    private void lootfeed$drawFeed(CallbackInfo info) {
        if (lootfeed$pose != null && LootFeedClient.visible()) {
            LootFeedClient.render(DrawCanvas.begin(lootfeed$pose));
        }
    }
}
