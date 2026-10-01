package dev.lootfeed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

final class ItemIcons {

    private ItemIcons() {
    }

    static void draw(PoseStack pose, ItemStack stack, int x, int y) {
        PoseStack view = RenderSystem.getModelViewStack();
        view.pushPose();
        view.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y);
        view.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
