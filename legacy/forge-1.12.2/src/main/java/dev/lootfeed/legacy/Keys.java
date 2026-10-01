package dev.lootfeed.legacy;

import dev.lootfeed.core.LootFeed;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

final class Keys {

    private static final String CATEGORY = "key.categories.misc";
    private static final KeyBinding TOGGLE = new KeyBinding("key.lootfeed.toggle", Keyboard.KEY_NONE, CATEGORY);
    private static final KeyBinding FILTER = new KeyBinding("key.lootfeed.filter", Keyboard.KEY_G, CATEGORY);
    private static final KeyBinding HISTORY = new KeyBinding("key.lootfeed.history", Keyboard.KEY_H, CATEGORY);

    private Keys() {
    }

    static void register() {
        ClientRegistry.registerKeyBinding(TOGGLE);
        ClientRegistry.registerKeyBinding(FILTER);
        ClientRegistry.registerKeyBinding(HISTORY);
    }

    static void poll(LootFeed feed) {
        while (TOGGLE.isPressed()) {
            feed.toggle();
        }
        while (FILTER.isPressed()) {
            feed.cycleFilter();
        }
        feed.history(HISTORY.isKeyDown());
    }
}
