package dev.lootfeed.core.hud;

public enum Anchor {
    TOP_LEFT(false, false),
    TOP_RIGHT(true, false),
    BOTTOM_LEFT(false, true),
    BOTTOM_RIGHT(true, true);

    public final boolean right;
    public final boolean bottom;

    Anchor(boolean right, boolean bottom) {
        this.right = right;
        this.bottom = bottom;
    }

    public static Anchor parse(String id) {
        for (Anchor anchor : values()) {
            if (anchor.name().equalsIgnoreCase(id)) {
                return anchor;
            }
        }
        return BOTTOM_RIGHT;
    }
}
