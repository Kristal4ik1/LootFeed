package dev.lootfeed.legacy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "lootfeed", useMetadata = true, clientSideOnly = true, acceptedMinecraftVersions = "[1.12,1.12.2]")
public final class LootFeedMod {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Keys.register();
        MinecraftForge.EVENT_BUS.register(new ClientHooks());
    }
}
