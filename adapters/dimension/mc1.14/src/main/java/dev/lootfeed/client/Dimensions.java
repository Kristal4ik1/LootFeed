package dev.lootfeed.client;

import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.world.level.dimension.DimensionType;

final class Dimensions {

    private Dimensions() {
    }

    static String id(MultiPlayerLevel level) {
        return String.valueOf(DimensionType.getName(level.dimension.getType()));
    }
}
