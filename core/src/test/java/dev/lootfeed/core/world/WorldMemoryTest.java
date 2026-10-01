package dev.lootfeed.core.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldMemoryTest {

    private static final int RADIUS = 64;

    @TempDir
    Path directory;

    @Test
    void itemIsNewOnlyOnce() {
        WorldMemory memory = WorldMemory.open(directory, "local/New World");

        assertTrue(memory.discover("item.minecraft.diamond"));
        assertFalse(memory.discover("item.minecraft.diamond"));
    }

    @Test
    void siteCoversEverythingWithinRadius() {
        WorldMemory memory = WorldMemory.open(directory, "local/New World");

        assertTrue(memory.enterSite("minecraft:overworld", 0.0, 64.0, 0.0, RADIUS));
        assertFalse(memory.enterSite("minecraft:overworld", 40.0, 64.0, 40.0, RADIUS));
        assertTrue(memory.enterSite("minecraft:overworld", 100.0, 64.0, 0.0, RADIUS));
        assertTrue(memory.enterSite("minecraft:the_nether", 0.0, 64.0, 0.0, RADIUS));
    }

    @Test
    void memorySurvivesReopening() {
        WorldMemory memory = WorldMemory.open(directory, "server/play.example.org");
        assertTrue(memory.blank());
        memory.discover("item.minecraft.diamond");
        memory.enterSite("minecraft:overworld", 10.0, 70.0, -30.0, RADIUS);
        memory.save();

        WorldMemory reopened = WorldMemory.open(directory, "server/play.example.org");
        assertFalse(reopened.blank());
        assertFalse(reopened.discover("item.minecraft.diamond"));
        assertFalse(reopened.enterSite("minecraft:overworld", 12.0, 70.0, -28.0, RADIUS));
    }

    @Test
    void worldsDoNotShareMemory() {
        WorldMemory first = WorldMemory.open(directory, "local/First");
        first.discover("item.minecraft.diamond");
        first.save();

        assertTrue(WorldMemory.open(directory, "local/Second").discover("item.minecraft.diamond"));
    }
}
