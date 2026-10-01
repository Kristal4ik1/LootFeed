package dev.lootfeed.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.dimension.DimensionType;

final class Dimensions {

    private Dimensions() {
    }

    static String id(ClientLevel level) {
        return String.valueOf(DimensionType.getName(level.dimension.getType()));
    }
}
