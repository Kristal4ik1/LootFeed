package dev.lootfeed.core.tracker;

import dev.lootfeed.core.PlayerSnapshot;
import dev.lootfeed.core.ScreenKind;
import dev.lootfeed.core.StackView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryTrackerTest {

    private static final int SETTLE_TICKS = 40;

    private final InventoryTracker tracker = new InventoryTracker();
    private final Recorder recorder = new Recorder();
    private final Map<String, int[]> inventory = new LinkedHashMap<>();

    private long playerId = 1L;
    private boolean alive = true;
    private boolean creative;
    private boolean dropKey;
    private ScreenKind screen = ScreenKind.NONE;

    @BeforeEach
    void joinWorld() {
        give("cobblestone", 16);
        give("pickaxe", 1, 1);
        tick(SETTLE_TICKS + 1);
        recorder.events.clear();
    }

    @Test
    void joiningReportsNothing() {
        assertEquals(Collections.emptyList(), recorder.events);
    }

    @Test
    void pickupIsReportedWithItsAmount() {
        give("cobblestone", 20);
        give("diamond", 3);
        tick(1);
        assertEquals(Arrays.asList("gain cobblestone 4", "gain diamond 3"), recorder.events);
    }

    @Test
    void dropKeyTurnsMissingItemsIntoDrops() {
        dropKey = true;
        give("cobblestone", 15);
        tick(1);
        assertEquals(Collections.singletonList("drop cobblestone 1"), recorder.events);
    }

    @Test
    void usedItemsAreNotReported() {
        give("cobblestone", 10);
        tick(20);
        assertEquals(Collections.emptyList(), recorder.events);
    }

    @Test
    void wornOutToolIsLost() {
        inventory.remove("pickaxe");
        tick(20);
        assertEquals(Collections.singletonList("lose pickaxe 1"), recorder.events);
    }

    @Test
    void itemsThrownOutOfInventoryScreenAreDrops() {
        screen = ScreenKind.INVENTORY;
        tick(1);
        give("cobblestone", 0);
        tick(1);
        assertEquals(Collections.singletonList("drop cobblestone 16"), recorder.events);
    }

    @Test
    void craftingInInventoryOnlyReportsTheResult() {
        screen = ScreenKind.INVENTORY;
        tick(1);
        give("cobblestone", 8);
        give("furnace", 1);
        tick(1);
        assertEquals(Collections.singletonList("gain furnace 1"), recorder.events);
    }

    @Test
    void movingItemsThroughContainerReportsOnlyNetGain() {
        screen = ScreenKind.LOOT;
        tick(1);
        give("cobblestone", 6);
        tick(1);
        give("cobblestone", 16);
        tick(1);
        give("cobblestone", 20);
        give("diamond", 2);
        tick(1);
        assertEquals(Arrays.asList("store", "loot cobblestone 4", "loot diamond 2"), recorder.events);
    }

    @Test
    void deathLosesEverythingThatDisappears() {
        alive = false;
        inventory.clear();
        tick(1);
        assertEquals(Arrays.asList("lose cobblestone 16", "lose pickaxe 1"), recorder.events);
    }

    @Test
    void inventoryClearedJustBeforeDeathIsStillLost() {
        inventory.clear();
        tick(2);
        alive = false;
        tick(1);
        assertEquals(Arrays.asList("lose cobblestone 16", "lose pickaxe 1"), recorder.events);
    }

    @Test
    void respawnWithEmptyInventoryReportsLossOnce() {
        alive = false;
        tick(1);
        recorder.events.clear();

        alive = true;
        playerId = 2L;
        inventory.clear();
        tick(SETTLE_TICKS + 1);
        assertEquals(Arrays.asList("lose cobblestone 16", "lose pickaxe 1"), recorder.events);
    }

    @Test
    void keptInventoryAfterRespawnReportsNothing() {
        alive = false;
        tick(1);
        alive = true;
        playerId = 2L;
        tick(SETTLE_TICKS + 5);
        assertEquals(Collections.emptyList(), recorder.events);
    }

    @Test
    void creativeModeIsIgnored() {
        creative = true;
        give("diamond", 64);
        tick(3);
        creative = false;
        tick(3);
        assertEquals(Collections.emptyList(), recorder.events);
    }

    private void give(String name, int count) {
        give(name, count, StackView.UNBREAKABLE);
    }

    private void give(String name, int count, int durability) {
        inventory.put(name, new int[] {count, durability});
    }

    private void tick(int times) {
        for (int i = 0; i < times; i++) {
            PlayerSnapshot snapshot = new PlayerSnapshot();
            snapshot.playerId = playerId;
            snapshot.world = "test";
            snapshot.alive = alive;
            snapshot.creative = creative;
            snapshot.dropKey = dropKey;
            snapshot.screen = screen;
            for (Map.Entry<String, int[]> item : inventory.entrySet()) {
                snapshot.add(item.getKey(), item.getKey(), item.getValue()[0], 0, false, item.getValue()[1], null);
            }
            tracker.tick(snapshot, recorder);
        }
    }

    private static final class Recorder implements LootSink {

        final List<String> events = new ArrayList<>();

        @Override
        public void gained(StackView stack, int amount, boolean fromContainer) {
            events.add((fromContainer ? "loot " : "gain ") + stack.name + " " + amount);
        }

        @Override
        public void dropped(StackView stack, int amount) {
            events.add("drop " + stack.name + " " + amount);
        }

        @Override
        public void lost(StackView stack, int amount) {
            events.add("lose " + stack.name + " " + amount);
        }

        @Override
        public void stored() {
            events.add("store");
        }

        @Override
        public void settled(Collection<StackView> inventory) {
        }
    }
}
