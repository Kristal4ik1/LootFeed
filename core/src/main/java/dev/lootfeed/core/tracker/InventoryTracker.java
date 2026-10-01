package dev.lootfeed.core.tracker;

import dev.lootfeed.core.PlayerSnapshot;
import dev.lootfeed.core.ScreenKind;
import dev.lootfeed.core.StackView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class InventoryTracker {

    private static final int SETTLE_TICKS = 40;
    private static final int DROP_GRACE_TICKS = 6;
    private static final int VANISH_TICKS = 10;
    private static final int BREAK_THRESHOLD = 2;

    private final Map<String, Integer> deposited = new HashMap<>();
    private final List<Vanished> vanished = new ArrayList<>();

    private Map<String, StackView> baseline = Collections.emptyMap();
    private String world;
    private long playerId;
    private int settle;
    private int dropGrace;
    private boolean died;
    private ScreenKind screen = ScreenKind.NONE;

    public void reset() {
        baseline = Collections.emptyMap();
        deposited.clear();
        vanished.clear();
        world = null;
        playerId = 0;
        settle = 0;
        dropGrace = 0;
        died = false;
        screen = ScreenKind.NONE;
    }

    public void tick(PlayerSnapshot snapshot, LootSink sink) {
        Map<String, StackView> current = snapshot.stacks();

        if (!snapshot.world.equals(world)) {
            reset();
            world = snapshot.world;
            playerId = snapshot.playerId;
            settle = SETTLE_TICKS;
            return;
        }
        if (snapshot.playerId != playerId) {
            playerId = snapshot.playerId;
            settle = SETTLE_TICKS;
            deposited.clear();
            return;
        }
        if (settle > 0) {
            if (--settle == 0) {
                finishSettling(current, sink);
            }
            return;
        }
        if (snapshot.creative) {
            baseline = current;
            vanished.clear();
            deposited.clear();
            return;
        }
        if (!snapshot.alive) {
            mourn(current, sink);
            return;
        }

        if (snapshot.dropKey) {
            dropGrace = DROP_GRACE_TICKS;
        } else if (dropGrace > 0) {
            dropGrace--;
        }
        if (snapshot.screen != screen) {
            screen = snapshot.screen;
            deposited.clear();
        }

        compare(current, sink);
        expireVanished(sink);
        baseline = current;
    }

    private void finishSettling(Map<String, StackView> current, LootSink sink) {
        if (died) {
            loseMissing(current, sink);
            died = false;
        }
        baseline = current;
        vanished.clear();
        sink.settled(current.values());
    }

    private void mourn(Map<String, StackView> current, LootSink sink) {
        died = true;
        for (Vanished entry : vanished) {
            sink.lost(entry.stack, entry.amount);
        }
        vanished.clear();
        loseMissing(current, sink);
        baseline = current;
    }

    private void loseMissing(Map<String, StackView> current, LootSink sink) {
        for (StackView before : baseline.values()) {
            int missing = before.count - countOf(current, before.key);
            if (missing > 0) {
                sink.lost(before, missing);
            }
        }
    }

    private void compare(Map<String, StackView> current, LootSink sink) {
        boolean gainedAnything = false;
        for (StackView now : current.values()) {
            if (now.count > countOf(baseline, now.key)) {
                gainedAnything = true;
                break;
            }
        }

        for (StackView before : baseline.values()) {
            int missing = before.count - countOf(current, before.key);
            if (missing <= 0) {
                continue;
            }
            switch (screen) {
                case LOOT:
                    deposit(before.key, missing);
                    sink.stored();
                    break;
                case OTHER:
                    deposit(before.key, missing);
                    break;
                case INVENTORY:
                    if (!gainedAnything) {
                        sink.dropped(before, missing);
                    }
                    break;
                case NONE:
                    if (dropGrace > 0) {
                        sink.dropped(before, missing);
                    } else {
                        vanished.add(new Vanished(before, missing));
                    }
                    break;
            }
        }

        for (StackView now : current.values()) {
            int extra = now.count - countOf(baseline, now.key);
            if (extra <= 0) {
                continue;
            }
            extra -= withdraw(now.key, extra);
            if (extra > 0) {
                sink.gained(now, extra, screen == ScreenKind.LOOT);
            }
        }
    }

    private void expireVanished(LootSink sink) {
        for (Iterator<Vanished> it = vanished.iterator(); it.hasNext(); ) {
            Vanished entry = it.next();
            if (++entry.age < VANISH_TICKS) {
                continue;
            }
            it.remove();
            if (entry.stack.breakable() && entry.stack.durability <= BREAK_THRESHOLD) {
                sink.lost(entry.stack, entry.amount);
            }
        }
    }

    private void deposit(String key, int amount) {
        Integer held = deposited.get(key);
        deposited.put(key, held == null ? amount : held + amount);
    }

    private int withdraw(String key, int amount) {
        Integer held = deposited.get(key);
        if (held == null) {
            return 0;
        }
        int taken = Math.min(held, amount);
        if (held == taken) {
            deposited.remove(key);
        } else {
            deposited.put(key, held - taken);
        }
        return taken;
    }

    private static int countOf(Map<String, StackView> stacks, String key) {
        StackView view = stacks.get(key);
        return view == null ? 0 : view.count;
    }

    private static final class Vanished {

        final StackView stack;
        final int amount;
        int age;

        Vanished(StackView stack, int amount) {
            this.stack = stack;
            this.amount = amount;
        }
    }
}
