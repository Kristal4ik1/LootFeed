package dev.lootfeed.core.feed;

import dev.lootfeed.core.StackView;

public final class FeedEntry {

    private final EntryKind kind;
    private final StackView stack;
    private final boolean fresh;
    private final boolean discovery;
    private int count;
    private long touched;

    FeedEntry(EntryKind kind, StackView stack, int count, boolean fresh, boolean discovery, long now) {
        this.kind = kind;
        this.stack = stack;
        this.count = count;
        this.fresh = fresh;
        this.discovery = discovery;
        this.touched = now;
    }

    public EntryKind kind() {
        return kind;
    }

    public StackView stack() {
        return stack;
    }

    public boolean fresh() {
        return fresh;
    }

    public boolean discovery() {
        return discovery;
    }

    public int count() {
        return count;
    }

    public long touched() {
        return touched;
    }

    public boolean rare() {
        return stack.rarity > 0 || stack.glint;
    }

    boolean absorbs(EntryKind kind, StackView stack, long now, long mergeWindow) {
        return !discovery && this.kind == kind && now - touched <= mergeWindow && this.stack.key.equals(stack.key);
    }

    void absorb(int amount, long now) {
        count += amount;
        touched = now;
    }
}
