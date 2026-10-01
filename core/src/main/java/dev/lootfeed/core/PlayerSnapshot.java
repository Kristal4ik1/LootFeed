package dev.lootfeed.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerSnapshot {

    public long playerId;
    public String world = "";
    public String dimension = "";
    public double x;
    public double y;
    public double z;
    public boolean alive = true;
    public boolean creative;
    public boolean dropKey;
    public ScreenKind screen = ScreenKind.NONE;

    private final Map<String, StackView> stacks = new LinkedHashMap<>();

    public void add(String id, String name, int count, int rarity, boolean glint, int durability, Object handle) {
        if (count <= 0) {
            return;
        }
        StackView view = new StackView(id, name, count, rarity, glint, durability, handle);
        StackView known = stacks.get(view.key);
        stacks.put(view.key, known == null ? view : known.plus(view));
    }

    public Map<String, StackView> stacks() {
        return stacks;
    }
}
