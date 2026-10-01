package dev.lootfeed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

final class WorldNames {

    private WorldNames() {
    }

    static String current(Minecraft client) {
        IntegratedServer local = client.getSingleplayerServer();
        if (local != null) {
            return "local/" + local.getLevelIdName();
        }
        ServerData remote = client.getCurrentServer();
        return remote == null ? "server/unknown" : "server/" + remote.ip;
    }
}
