package dev.lootfeed.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

final class GameAccess {

    private GameAccess() {
    }

    static ItemStack carried(LocalPlayer player) {
        return player.containerMenu.getCarried();
    }
}
