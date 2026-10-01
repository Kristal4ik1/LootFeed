package dev.lootfeed.client;

import dev.lootfeed.core.Platform;
import dev.lootfeed.core.PlayerSnapshot;
import dev.lootfeed.core.ScreenKind;
import dev.lootfeed.core.StackView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.List;

final class GameBridge implements Platform {

    private static final int CRAFTING_RESULT_SLOT = 0;

    @Override
    public Path configDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }

    @Override
    public String translate(String key, Object... args) {
        return I18n.get(key, args);
    }

    @Override
    public boolean capture(PlayerSnapshot snapshot) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return false;
        }
        snapshot.playerId = System.identityHashCode(player);
        snapshot.world = WorldNames.current(client);
        snapshot.dimension = Dimensions.id(client.level);
        Vec3 position = player.position();
        snapshot.x = position.x;
        snapshot.y = position.y;
        snapshot.z = position.z;
        snapshot.alive = player.isAlive();
        snapshot.creative = player.isCreative() || player.isSpectator();
        snapshot.dropKey = client.options.keyDrop.isDown();
        snapshot.screen = screenKind(Screens.current(client));

        List<Slot> slots = player.inventoryMenu.slots;
        for (int i = 0; i < slots.size(); i++) {
            if (i != CRAFTING_RESULT_SLOT) {
                add(snapshot, slots.get(i).getItem());
            }
        }
        add(snapshot, GameAccess.carried(player));
        return true;
    }

    private static void add(PlayerSnapshot snapshot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int durability = stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : StackView.UNBREAKABLE;
        snapshot.add(
                stack.getItem().getDescriptionId(),
                stack.getHoverName().getString(),
                stack.getCount(),
                stack.getRarity().ordinal(),
                stack.hasFoil(),
                durability,
                stack.copy());
    }

    private static ScreenKind screenKind(Screen screen) {
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            return ScreenKind.INVENTORY;
        }
        if (screen instanceof ContainerScreen || screen instanceof DispenserScreen || screen instanceof HopperScreen) {
            boolean enderChest = screen.getTitle().getString().equals(I18n.get("container.enderchest"));
            return enderChest ? ScreenKind.OTHER : ScreenKind.LOOT;
        }
        if (screen instanceof AbstractContainerScreen) {
            return ScreenKind.OTHER;
        }
        return ScreenKind.NONE;
    }
}
