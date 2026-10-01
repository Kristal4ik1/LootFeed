package dev.lootfeed.core.world;

import dev.lootfeed.core.io.JsonFiles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WorldMemory {

    private static final long SAVE_INTERVAL = 5000L;
    private static final int NAME_LIMIT = 48;

    private final Path file;
    private final String world;
    private final Data data;
    private final boolean blank;
    private boolean dirty;
    private long lastSave;

    private WorldMemory(Path file, String world, Data data, boolean blank) {
        this.file = file;
        this.world = world;
        this.data = data;
        this.blank = blank;
    }

    public static WorldMemory open(Path directory, String world) {
        Path file = directory.resolve(fileName(world));
        Data data = JsonFiles.read(file, Data.class);
        boolean blank = data == null;
        if (blank) {
            data = new Data();
        }
        if (data.seen == null) {
            data.seen = new LinkedHashSet<>();
        }
        if (data.sites == null) {
            data.sites = new LinkedHashMap<>();
        }
        return new WorldMemory(file, world, data, blank);
    }

    public String world() {
        return world;
    }

    public boolean blank() {
        return blank;
    }

    public boolean discover(String item) {
        if (data.seen.add(item)) {
            dirty = true;
            return true;
        }
        return false;
    }

    public boolean enterSite(String dimension, double x, double y, double z, int radius) {
        if (knownSite(dimension, x, y, z, radius)) {
            return false;
        }
        List<int[]> sites = data.sites.get(dimension);
        if (sites == null) {
            sites = new ArrayList<>();
            data.sites.put(dimension, sites);
        }
        sites.add(new int[] {(int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)});
        dirty = true;
        return true;
    }

    public void saveIfDue(long now) {
        if (dirty && now - lastSave >= SAVE_INTERVAL) {
            lastSave = now;
            save();
        }
    }

    public void save() {
        if (dirty) {
            dirty = false;
            JsonFiles.write(file, data);
        }
    }

    private boolean knownSite(String dimension, double x, double y, double z, int radius) {
        List<int[]> sites = data.sites.get(dimension);
        if (sites == null) {
            return false;
        }
        double limit = (double) radius * radius;
        for (int[] site : sites) {
            if (site == null || site.length < 3) {
                continue;
            }
            double dx = site[0] - x;
            double dy = site[1] - y;
            double dz = site[2] - z;
            if (dx * dx + dy * dy + dz * dz <= limit) {
                return true;
            }
        }
        return false;
    }

    private static String fileName(String world) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < world.length() && name.length() < NAME_LIMIT; i++) {
            char c = world.charAt(i);
            name.append(Character.isLetterOrDigit(c) && c < 128 || c == '.' || c == '-' ? c : '_');
        }
        return name + "-" + Integer.toHexString(world.hashCode()) + ".json";
    }

    private static final class Data {

        Set<String> seen;
        Map<String, List<int[]>> sites;
    }
}
