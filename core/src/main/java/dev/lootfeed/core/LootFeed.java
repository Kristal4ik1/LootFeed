package dev.lootfeed.core;

import dev.lootfeed.core.config.Settings;
import dev.lootfeed.core.feed.EntryKind;
import dev.lootfeed.core.feed.Feed;
import dev.lootfeed.core.feed.Filter;
import dev.lootfeed.core.hud.FeedRenderer;
import dev.lootfeed.core.tracker.InventoryTracker;
import dev.lootfeed.core.tracker.LootSink;
import dev.lootfeed.core.world.WorldMemory;

import java.nio.file.Path;
import java.util.Collection;

public final class LootFeed implements LootSink {

    private static final long NOTICE_TIME = 2200L;

    private final Platform platform;
    private final Path settingsFile;
    private final Path worldsDirectory;
    private final Settings settings;
    private final Feed feed = new Feed();
    private final InventoryTracker tracker = new InventoryTracker();
    private final FeedRenderer renderer;

    private Filter filter;
    private WorldMemory memory;
    private PlayerSnapshot player;
    private boolean history;
    private String notice;
    private long noticeUntil;

    public LootFeed(Platform platform) {
        this.platform = platform;
        this.settingsFile = platform.configDir().resolve("lootfeed.json");
        this.worldsDirectory = platform.configDir().resolve("lootfeed").resolve("worlds");
        this.settings = Settings.load(settingsFile);
        this.filter = Filter.parse(settings.filter);
        this.renderer = new FeedRenderer(platform);
    }

    public void tick() {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        if (!platform.capture(snapshot)) {
            leaveWorld();
            return;
        }
        if (memory == null || !memory.world().equals(snapshot.world)) {
            leaveWorld();
            memory = WorldMemory.open(worldsDirectory, snapshot.world);
        }
        long now = System.currentTimeMillis();
        player = snapshot;
        tracker.tick(snapshot, this);
        feed.prune(now, settings.historyMinutes * 60000L);
        memory.saveIfDue(now);
    }

    public void render(Canvas canvas) {
        boolean noticed = notice != null && System.currentTimeMillis() < noticeUntil;
        renderer.render(canvas, feed, settings, filter, history, noticed ? notice : null);
    }

    public void toggle() {
        settings.enabled = !settings.enabled;
        settings.save(settingsFile);
        announce(platform.translate(settings.enabled ? "lootfeed.state.on" : "lootfeed.state.off"));
    }

    public void cycleFilter() {
        filter = filter.next();
        settings.filter = filter.id();
        settings.save(settingsFile);
        announce(platform.translate("lootfeed.filter." + filter.id()));
    }

    public void history(boolean shown) {
        history = shown;
    }

    @Override
    public void gained(StackView stack, int amount, boolean fromContainer) {
        boolean fresh = memory.discover(stack.id);
        boolean discovery = fromContainer
                && memory.enterSite(player.dimension, player.x, player.y, player.z, settings.structureRadius);
        feed.addPickup(stack, amount, fresh, discovery, System.currentTimeMillis(), mergeWindow());
    }

    @Override
    public void dropped(StackView stack, int amount) {
        if (settings.showDropped) {
            feed.add(EntryKind.DROPPED, stack, amount, System.currentTimeMillis(), mergeWindow());
        }
    }

    @Override
    public void lost(StackView stack, int amount) {
        if (settings.showLost) {
            feed.add(EntryKind.LOST, stack, amount, System.currentTimeMillis(), mergeWindow());
        }
    }

    @Override
    public void stored() {
        memory.enterSite(player.dimension, player.x, player.y, player.z, settings.structureRadius);
    }

    @Override
    public void settled(Collection<StackView> inventory) {
        if (memory.blank()) {
            for (StackView stack : inventory) {
                memory.discover(stack.id);
            }
        }
    }

    private long mergeWindow() {
        return settings.visibleSeconds * 1000L;
    }

    private void announce(String text) {
        notice = text;
        noticeUntil = System.currentTimeMillis() + NOTICE_TIME;
    }

    private void leaveWorld() {
        if (memory != null) {
            memory.save();
            memory = null;
        }
        tracker.reset();
        feed.clear();
        player = null;
    }
}
