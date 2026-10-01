package dev.lootfeed.core.config;

import dev.lootfeed.core.io.JsonFiles;

import java.nio.file.Path;

public final class Settings {

    public boolean enabled = true;
    public String anchor = "bottom_right";
    public int offsetX = 4;
    public int offsetY = 4;
    public float scale = 1.0f;
    public int maxRows = 7;
    public int visibleSeconds = 10;
    public int historyMinutes = 5;
    public boolean showDropped = true;
    public boolean showLost = true;
    public String filter = "all";
    public int structureRadius = 64;

    public static Settings load(Path file) {
        Settings settings = JsonFiles.read(file, Settings.class);
        if (settings == null) {
            settings = new Settings();
        }
        settings.sanitize();
        settings.save(file);
        return settings;
    }

    public void save(Path file) {
        JsonFiles.write(file, this);
    }

    private void sanitize() {
        if (anchor == null) {
            anchor = "bottom_right";
        }
        if (filter == null) {
            filter = "all";
        }
        offsetX = clamp(offsetX, 0, 4000);
        offsetY = clamp(offsetY, 0, 4000);
        scale = Math.max(0.5f, Math.min(2.0f, scale));
        maxRows = clamp(maxRows, 1, 20);
        visibleSeconds = clamp(visibleSeconds, 2, 120);
        historyMinutes = clamp(historyMinutes, 1, 60);
        structureRadius = clamp(structureRadius, 8, 512);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
