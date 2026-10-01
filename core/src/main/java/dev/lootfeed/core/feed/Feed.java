package dev.lootfeed.core.feed;

import dev.lootfeed.core.StackView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Feed {

    private static final int CAPACITY = 256;

    private final List<FeedEntry> entries = new ArrayList<>();
    private final List<FeedEntry> view = Collections.unmodifiableList(entries);

    public List<FeedEntry> entries() {
        return view;
    }

    public void add(EntryKind kind, StackView stack, int amount, long now, long mergeWindow) {
        if (!merge(kind, stack, amount, now, mergeWindow)) {
            insert(new FeedEntry(kind, stack, amount, false, false, now));
        }
    }

    public void addPickup(StackView stack, int amount, boolean fresh, boolean discovery, long now, long mergeWindow) {
        if (discovery || !merge(EntryKind.PICKED_UP, stack, amount, now, mergeWindow)) {
            insert(new FeedEntry(EntryKind.PICKED_UP, stack, amount, fresh, discovery, now));
        }
    }

    public void prune(long now, long lifetime) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (now - entries.get(i).touched() > lifetime) {
                entries.remove(i);
            }
        }
    }

    public void clear() {
        entries.clear();
    }

    private boolean merge(EntryKind kind, StackView stack, int amount, long now, long mergeWindow) {
        for (FeedEntry entry : entries) {
            if (entry.absorbs(kind, stack, now, mergeWindow)) {
                entry.absorb(amount, now);
                return true;
            }
        }
        return false;
    }

    private void insert(FeedEntry entry) {
        entries.add(0, entry);
        if (entries.size() > CAPACITY) {
            entries.remove(entries.size() - 1);
        }
    }
}
