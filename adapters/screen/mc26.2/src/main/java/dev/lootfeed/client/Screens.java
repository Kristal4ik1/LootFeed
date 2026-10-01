package dev.lootfeed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class Screens {

    private Screens() {
    }

    static Screen current(Minecraft client) {
        return client.gui.screen();
    }

    static boolean hudHidden(Minecraft client) {
        return client.gui.hud.isHidden();
    }
}
