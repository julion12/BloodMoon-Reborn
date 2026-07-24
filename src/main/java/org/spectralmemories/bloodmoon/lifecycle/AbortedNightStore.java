package org.spectralmemories.bloodmoon.lifecycle;

import org.spectralmemories.bloodmoon.config.SafeYaml;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Internal, privacy-minimal marker for nights consumed by interrupted active events. */
public final class AbortedNightStore {
    public static final int VERSION = 1;

    public record Marker(UUID worldId, String worldName, long cycle, String cause, Instant recordedAt) { }

    private final Path file;
    private final Logger logger;
    private final Yaml yaml = SafeYaml.create();
    private final Map<UUID, Marker> markers = new LinkedHashMap<>();

    public AbortedNightStore(Path file, Logger logger) {
        this.file = file;
        this.logger = logger;
        load();
    }

    public synchronized void mark(UUID worldId, String worldName, long fullTime, String cause) {
        markCycle(worldId, worldName, NightCycle.identity(fullTime), cause);
    }

    public synchronized void markCycle(UUID worldId, String worldName, long cycle, String cause) {
        markers.put(worldId, new Marker(worldId, safe(worldName), cycle, safe(cause), Instant.now()));
        save();
    }

    public synchronized boolean suppresses(UUID worldId, long fullTime, long worldTime) {
        Marker marker = markers.get(worldId);
        if (marker == null) return false;
        if (marker.cycle() != NightCycle.identity(fullTime)) {
            markers.remove(worldId);
            save();
            return false;
        }
        return NightCycle.isNight(worldTime);
    }

    public synchronized boolean hasMarker(UUID worldId) {
        return markers.containsKey(worldId);
    }

    public synchronized Marker marker(UUID worldId) {
        return markers.get(worldId);
    }

    public synchronized void clear(UUID worldId) {
        if (markers.remove(worldId) != null) save();
    }

    private void load() {
        if (!Files.isRegularFile(file)) return;
        try {
            Object loaded = yaml.load(Files.readString(file, StandardCharsets.UTF_8));
            if (!(loaded instanceof Map<?, ?> root)) throw new IllegalArgumentException("root is not a map");
            if (integer(root.get("RecoveryVersion")) != VERSION) {
                throw new IllegalArgumentException("unsupported RecoveryVersion");
            }
            Object worldsValue = root.get("Worlds");
            if (worldsValue == null) return;
            if (!(worldsValue instanceof Map<?, ?> worlds)) throw new IllegalArgumentException("Worlds is not a map");
            for (Map.Entry<?, ?> entry : worlds.entrySet()) {
                UUID worldId = UUID.fromString(String.valueOf(entry.getKey()));
                if (!(entry.getValue() instanceof Map<?, ?> value)) {
                    throw new IllegalArgumentException("world marker is not a map");
                }
                markers.put(worldId, new Marker(worldId, safe(value.get("World")),
                        number(value.get("Cycle")), safe(value.get("Cause")),
                        Instant.parse(safe(value.get("RecordedAt")))));
            }
        } catch (Exception exception) {
            preserveInvalid(exception);
            markers.clear();
        }
    }

    private boolean save() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            if (markers.isEmpty()) {
                Files.deleteIfExists(file);
                Files.deleteIfExists(file.resolveSibling(file.getFileName() + ".tmp"));
                return true;
            }
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.deleteIfExists(temporary);
            Files.writeString(temporary, yaml.dump(document()), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            logger.log(Level.SEVERE, "Could not atomically save aborted Blood Moon night state", exception);
            return false;
        }
    }

    private Map<String, Object> document() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("RecoveryVersion", VERSION);
        Map<String, Object> worlds = new LinkedHashMap<>();
        for (Marker marker : markers.values()) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("World", marker.worldName());
            value.put("Cycle", marker.cycle());
            value.put("Cause", marker.cause());
            value.put("RecordedAt", marker.recordedAt().toString());
            worlds.put(marker.worldId().toString(), value);
        }
        root.put("Worlds", worlds);
        return root;
    }

    private void preserveInvalid(Exception cause) {
        Path backup = file.resolveSibling("aborted-nights.invalid-" + Instant.now().toEpochMilli() + ".yml");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            logger.log(Level.WARNING, "Invalid aborted-night state was preserved as "
                    + backup.getFileName() + "; no arbitrary night will be suppressed", cause);
        } catch (IOException backupFailure) {
            logger.log(Level.WARNING, "Invalid aborted-night state could not be preserved; "
                    + "no arbitrary night will be suppressed", backupFailure);
        }
    }

    private static int integer(Object value) {
        return Math.toIntExact(number(value));
    }

    private static long number(Object value) {
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
