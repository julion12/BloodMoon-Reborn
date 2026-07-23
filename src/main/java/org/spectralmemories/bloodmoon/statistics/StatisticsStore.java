package org.spectralmemories.bloodmoon.statistics;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/** Versioned statistics.yml persistence with corrupt-file preservation and atomic replacement. */
public final class StatisticsStore {
    public static final int VERSION = 1;

    private final Path file;
    private final Logger logger;
    private final Yaml yaml = new Yaml();

    public StatisticsStore(Path file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public HistoricalStatistics loadOrCreate() {
        if (!Files.exists(file)) {
            HistoricalStatistics empty = HistoricalStatistics.empty();
            save(empty);
            return empty;
        }
        try {
            Object loaded = yaml.load(Files.readString(file, StandardCharsets.UTF_8));
            if (!(loaded instanceof Map<?, ?> root)) throw new IllegalArgumentException("root is not a map");
            int version = integer(root.get("StatisticsVersion"));
            if (version != VERSION) throw new IllegalArgumentException("unsupported StatisticsVersion " + version);
            Map<?, ?> totals = map(root.get("Totals"), "Totals");
            Map<?, ?> last = map(root.get("LastEvent"), "LastEvent");
            return new HistoricalStatistics(
                    number(totals.get("Events")),
                    number(totals.get("DeathEvents")),
                    number(totals.get("UniqueDeaths")),
                    number(totals.get("BossesSpawned")),
                    number(totals.get("BossesDefeated")),
                    text(last.get("World")),
                    number(last.get("DurationSeconds")),
                    number(last.get("DeathCount")),
                    integer(last.get("UniqueDeaths")),
                    integer(last.get("Participants")),
                    integer(last.get("Survivors")),
                    text(last.get("BossName")),
                    text(last.get("BossType")),
                    timestamp(last.get("EndedAt")));
        } catch (Exception exception) {
            preserveCorruptFile(exception);
            HistoricalStatistics empty = HistoricalStatistics.empty();
            save(empty);
            return empty;
        }
    }

    public boolean save(HistoricalStatistics statistics) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.deleteIfExists(temporary);
            Files.writeString(temporary, yaml.dump(document(statistics)), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            logger.log(Level.SEVERE, "Could not atomically save BloodMoon statistics to " + file, exception);
            return false;
        }
    }

    private Map<String, Object> document(HistoricalStatistics statistics) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("StatisticsVersion", VERSION);
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("Events", statistics.totalEvents());
        totals.put("DeathEvents", statistics.totalDeathEvents());
        totals.put("UniqueDeaths", statistics.totalUniqueDeaths());
        totals.put("BossesSpawned", statistics.totalBossesSpawned());
        totals.put("BossesDefeated", statistics.totalBossesDefeated());
        root.put("Totals", totals);
        Map<String, Object> last = new LinkedHashMap<>();
        last.put("World", statistics.lastEventWorld());
        last.put("DurationSeconds", statistics.lastEventDurationSeconds());
        last.put("DeathCount", statistics.lastEventDeathCount());
        last.put("UniqueDeaths", statistics.lastEventUniqueDeaths());
        last.put("Participants", statistics.lastEventParticipants());
        last.put("Survivors", statistics.lastEventSurvivors());
        last.put("BossName", statistics.lastBossName());
        last.put("BossType", statistics.lastBossType());
        last.put("EndedAt", statistics.lastEventEndedAt());
        root.put("LastEvent", last);
        return root;
    }

    private void preserveCorruptFile(Exception cause) {
        Path backup = file.resolveSibling("statistics.corrupt-" + Instant.now().toEpochMilli() + ".yml");
        try {
            Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
            logger.log(Level.WARNING, "Invalid BloodMoon statistics were preserved as "
                    + backup.getFileName() + "; safe defaults will be used", cause);
        } catch (IOException backupFailure) {
            logger.log(Level.WARNING, "Invalid BloodMoon statistics could not be backed up; safe defaults will be used",
                    backupFailure);
        }
    }

    private static Map<?, ?> map(Object value, String name) {
        if (value instanceof Map<?, ?> map) return map;
        throw new IllegalArgumentException(name + " is not a map");
    }

    private static long number(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return Math.max(0, number.longValue());
        return Math.max(0, Long.parseLong(value.toString()));
    }

    private static int integer(Object value) {
        return (int) Math.min(Integer.MAX_VALUE, number(value));
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String timestamp(Object value) {
        String timestamp = text(value);
        if (!timestamp.isBlank()) Instant.parse(timestamp);
        return timestamp;
    }
}
