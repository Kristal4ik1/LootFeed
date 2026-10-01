package dev.lootfeed.core.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonFiles {

    private static final Logger LOGGER = LogManager.getLogger("LootFeed");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonFiles() {
    }

    public static <T> T read(Path file, Class<T> type) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Could not read {}, falling back to defaults", file, e);
            return null;
        }
    }

    public static void write(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(value, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }
}
