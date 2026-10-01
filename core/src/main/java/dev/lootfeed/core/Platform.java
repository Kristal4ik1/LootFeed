package dev.lootfeed.core;

import java.nio.file.Path;

public interface Platform {

    Path configDir();

    String translate(String key, Object... args);

    boolean capture(PlayerSnapshot snapshot);
}
