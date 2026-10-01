package dev.lootfeed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

final class ItemIcons {

    private ItemIcons() {
    }

    static void draw(PoseStack pose, ItemStack stack, int x, int y) {
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(pose.last().pose());
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y);
        RenderSystem.popMatrix();
    }
}
