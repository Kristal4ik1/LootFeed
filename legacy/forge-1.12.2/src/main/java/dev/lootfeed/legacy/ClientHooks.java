package dev.lootfeed.legacy;

import dev.lootfeed.core.LootFeed;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ClientHooks {

    private final LootFeed feed = new LootFeed(new GameBridge());

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Keys.poll(feed);
            feed.tick();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft client = Minecraft.getMinecraft();
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && client.player != null) {
            feed.render(new DrawCanvas(client, event.getResolution()));
        }
    }
}
