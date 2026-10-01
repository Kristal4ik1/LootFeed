package dev.lootfeed.core;

public final class StackView {

    public static final int UNBREAKABLE = -1;

    public final String key;
    public final String id;
    public final String name;
    public final int count;
    public final int rarity;
    public final boolean glint;
    public final int durability;
    public final Object handle;

    public StackView(String id, String name, int count, int rarity, boolean glint, int durability, Object handle) {
        this.key = id + '\n' + name;
        this.id = id;
        this.name = name;
        this.count = count;
        this.rarity = rarity;
        this.glint = glint;
        this.durability = durability;
        this.handle = handle;
    }

    public StackView plus(StackView other) {
        int weakest = durability;
        if (other.durability != UNBREAKABLE && (weakest == UNBREAKABLE || other.durability < weakest)) {
            weakest = other.durability;
        }
        return new StackView(id, name, count + other.count, Math.max(rarity, other.rarity), glint || other.glint, weakest, handle);
    }

    public boolean breakable() {
        return durability != UNBREAKABLE;
    }
}
