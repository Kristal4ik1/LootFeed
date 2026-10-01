package dev.lootfeed.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lootfeed.core.LootFeed;
import net.minecraft.client.KeyMapping;

import java.util.Arrays;

public final class Keys {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.MISC;
    private static final KeyMapping TOGGLE = new KeyMapping("key.lootfeed.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final KeyMapping FILTER = new KeyMapping("key.lootfeed.filter", InputConstants.KEY_G, CATEGORY);
    private static final KeyMapping HISTORY = new KeyMapping("key.lootfeed.history", InputConstants.KEY_H, CATEGORY);

    private Keys() {
    }

    public static KeyMapping[] appendTo(KeyMapping[] mappings) {
        if (Arrays.asList(mappings).contains(TOGGLE)) {
            return mappings;
        }
        KeyMapping[] extended = Arrays.copyOf(mappings, mappings.length + 3);
        extended[mappings.length] = TOGGLE;
        extended[mappings.length + 1] = FILTER;
        extended[mappings.length + 2] = HISTORY;
        return extended;
    }

    static void poll(LootFeed feed) {
        while (TOGGLE.consumeClick()) {
            feed.toggle();
        }
        while (FILTER.consumeClick()) {
            feed.cycleFilter();
        }
        feed.history(HISTORY.isDown());
    }
}
