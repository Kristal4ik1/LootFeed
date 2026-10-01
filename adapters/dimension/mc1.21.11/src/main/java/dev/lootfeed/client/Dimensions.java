package dev.lootfeed.client;

import net.minecraft.client.multiplayer.ClientLevel;

final class Dimensions {

    private Dimensions() {
    }

    static String id(ClientLevel level) {
        return level.dimension().identifier().toString();
    }
}
