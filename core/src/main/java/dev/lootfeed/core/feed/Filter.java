package dev.lootfeed.core.feed;

import java.util.Locale;

public enum Filter {
    ALL,
    NEW,
    RARE;

    public boolean accepts(FeedEntry entry) {
        switch (this) {
            case NEW:
                return entry.fresh() || entry.discovery();
            case RARE:
                return entry.rare() || entry.discovery();
            default:
                return true;
        }
    }

    public Filter next() {
        Filter[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Filter parse(String id) {
        for (Filter filter : values()) {
            if (filter.id().equalsIgnoreCase(id)) {
                return filter;
            }
        }
        return ALL;
    }
}
