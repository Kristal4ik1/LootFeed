package dev.lootfeed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

final class ItemIcons {

    private ItemIcons() {
    }

    static void draw(PoseStack pose, ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(pose, stack, x, y);
    }
}
