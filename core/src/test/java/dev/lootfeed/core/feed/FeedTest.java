package dev.lootfeed.core.feed;

import dev.lootfeed.core.StackView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedTest {

    private static final long WINDOW = 10_000L;

    private final Feed feed = new Feed();

    @Test
    void repeatedPickupsWithinWindowMerge() {
        feed.addPickup(stack("diamond", 0), 2, true, false, 0L, WINDOW);
        feed.addPickup(stack("diamond", 0), 3, false, false, 4_000L, WINDOW);

        assertEquals(1, feed.entries().size());
        assertEquals(5, feed.entries().get(0).count());
        assertTrue(feed.entries().get(0).fresh());
    }

    @Test
    void pickupAfterWindowStartsNewEntry() {
        feed.addPickup(stack("diamond", 0), 2, false, false, 0L, WINDOW);
        feed.addPickup(stack("diamond", 0), 3, false, false, WINDOW + 1L, WINDOW);

        assertEquals(2, feed.entries().size());
        assertEquals(3, feed.entries().get(0).count());
    }

    @Test
    void differentKindsNeverMerge() {
        feed.addPickup(stack("diamond", 0), 2, false, false, 0L, WINDOW);
        feed.add(EntryKind.DROPPED, stack("diamond", 0), 2, 1L, WINDOW);

        assertEquals(2, feed.entries().size());
    }

    @Test
    void discoveryAlwaysGetsItsOwnEntry() {
        feed.addPickup(stack("apple", 0), 1, false, false, 0L, WINDOW);
        feed.addPickup(stack("apple", 0), 1, false, true, 1L, WINDOW);
        feed.addPickup(stack("apple", 0), 1, false, false, 2L, WINDOW);

        assertEquals(2, feed.entries().size());
        assertTrue(feed.entries().get(0).discovery());
        assertEquals(2, feed.entries().get(1).count());
    }

    @Test
    void pruneDropsEntriesOlderThanLifetime() {
        feed.addPickup(stack("diamond", 0), 1, false, false, 0L, WINDOW);
        feed.addPickup(stack("emerald", 0), 1, false, false, 50_000L, WINDOW);
        feed.prune(70_000L, 60_000L);

        assertEquals(1, feed.entries().size());
        assertEquals("emerald", feed.entries().get(0).stack().name);
    }

    @Test
    void filtersLookAtFreshnessAndRarity() {
        feed.addPickup(stack("dirt", 0), 1, false, false, 0L, WINDOW);
        feed.addPickup(stack("star", 2), 1, false, false, 0L, WINDOW);
        feed.addPickup(stack("sand", 0), 1, true, false, 0L, WINDOW);
        FeedEntry sand = feed.entries().get(0);
        FeedEntry star = feed.entries().get(1);
        FeedEntry dirt = feed.entries().get(2);

        assertTrue(Filter.ALL.accepts(dirt));
        assertTrue(Filter.NEW.accepts(sand));
        assertFalse(Filter.NEW.accepts(star));
        assertTrue(Filter.RARE.accepts(star));
        assertFalse(Filter.RARE.accepts(dirt));
    }

    private static StackView stack(String name, int rarity) {
        return new StackView(name, name, 1, rarity, false, StackView.UNBREAKABLE, null);
    }
}
