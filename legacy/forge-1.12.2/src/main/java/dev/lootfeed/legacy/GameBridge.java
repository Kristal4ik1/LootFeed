package dev.lootfeed.legacy;

import dev.lootfeed.core.Platform;
import dev.lootfeed.core.PlayerSnapshot;
import dev.lootfeed.core.ScreenKind;
import dev.lootfeed.core.StackView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;

import java.nio.file.Path;
import java.util.List;

final class GameBridge implements Platform {

    private static final int CRAFTING_RESULT_SLOT = 0;

    @Override
    public Path configDir() {
        return Minecraft.getMinecraft().gameDir.toPath().resolve("config");
    }

    @Override
    public String translate(String key, Object... args) {
        return I18n.format(key, args);
    }

    @Override
    public boolean capture(PlayerSnapshot snapshot) {
        Minecraft client = Minecraft.getMinecraft();
        EntityPlayerSP player = client.player;
        if (player == null || client.world == null) {
            return false;
        }
        snapshot.playerId = System.identityHashCode(player);
        snapshot.world = worldName(client);
        snapshot.dimension = client.world.provider.getDimensionType().getName();
        snapshot.x = player.posX;
        snapshot.y = player.posY;
        snapshot.z = player.posZ;
        snapshot.alive = player.isEntityAlive();
        snapshot.creative = player.isCreative() || player.isSpectator();
        snapshot.dropKey = client.gameSettings.keyBindDrop.isKeyDown();
        snapshot.screen = screenKind(client.currentScreen, player.openContainer);

        List<Slot> slots = player.inventoryContainer.inventorySlots;
        for (int i = 0; i < slots.size(); i++) {
            if (i != CRAFTING_RESULT_SLOT) {
                add(snapshot, slots.get(i).getStack());
            }
        }
        add(snapshot, player.inventory.getItemStack());
        return true;
    }

    private static void add(PlayerSnapshot snapshot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int durability = stack.isItemStackDamageable() ? stack.getMaxDamage() - stack.getItemDamage() : StackView.UNBREAKABLE;
        snapshot.add(
                stack.getTranslationKey(),
                stack.getDisplayName(),
                stack.getCount(),
                stack.getRarity().ordinal(),
                stack.hasEffect(),
                durability,
                stack.copy());
    }

    private static ScreenKind screenKind(GuiScreen screen, Container container) {
        if (screen instanceof GuiInventory || screen instanceof GuiContainerCreative) {
            return ScreenKind.INVENTORY;
        }
        if (screen instanceof GuiChest || screen instanceof GuiDispenser || screen instanceof GuiHopper) {
            return enderChest(container) ? ScreenKind.OTHER : ScreenKind.LOOT;
        }
        if (screen instanceof GuiContainer) {
            return ScreenKind.OTHER;
        }
        return ScreenKind.NONE;
    }

    private static boolean enderChest(Container container) {
        if (!(container instanceof ContainerChest)) {
            return false;
        }
        String title = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        return title.equals(I18n.format("container.enderchest"));
    }

    private static String worldName(Minecraft client) {
        IntegratedServer local = client.getIntegratedServer();
        if (local != null) {
            return "local/" + local.getFolderName();
        }
        ServerData remote = client.getCurrentServerData();
        return remote == null ? "server/unknown" : "server/" + remote.serverIP;
    }
}
