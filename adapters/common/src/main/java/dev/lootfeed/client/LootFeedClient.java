package dev.lootfeed.client;

import dev.lootfeed.core.Canvas;
import dev.lootfeed.core.LootFeed;
import net.minecraft.client.Minecraft;

public final class LootFeedClient {

    private static LootFeed feed;

    private LootFeedClient() {
    }

    public static void tick() {
        LootFeed feed = feed();
        Keys.poll(feed);
        feed.tick();
    }

    public static boolean visible() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && !Screens.hudHidden(client);
    }

    public static void render(Canvas canvas) {
        feed().render(canvas);
    }

    private static LootFeed feed() {
        if (feed == null) {
            feed = new LootFeed(new GameBridge());
        }
        return feed;
    }
}
