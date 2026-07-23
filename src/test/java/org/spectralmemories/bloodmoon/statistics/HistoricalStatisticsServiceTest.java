package org.spectralmemories.bloodmoon.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class HistoricalStatisticsServiceTest {
    private static final Instant START = Instant.parse("2026-07-23T12:00:00Z");
    private static final Logger LOGGER = Logger.getLogger("statistics-test");

    @Test void newFileLoadsSafeDefaultsAndCreatesVersionedYaml(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("statistics.yml");
        HistoricalStatisticsService service = new HistoricalStatisticsService(file, LOGGER);

        assertAll(() -> assertTrue(Files.isRegularFile(file)),
                () -> assertEquals(HistoricalStatistics.empty(), service.snapshot()),
                () -> assertTrue(Files.readString(file).contains("StatisticsVersion: 1")));
    }

    @Test void completedEventAccumulatesTotalsExactlyOnceAndCapturesLastValues(@TempDir Path directory) {
        HistoricalStatisticsService service = service(directory);
        BloodMoonSession session = completedSession("world", 75);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        session.join(first, START);
        session.join(second, START);
        session.die(first, true);
        session.die(first, true);
        session.die(second, false);
        session.bossSpawned(UUID.randomUUID(), "Crimson King", "MYTHICMOBS");
        session.end(START.plusSeconds(75));

        service.recordCompletedEvent(session);
        service.recordCompletedEvent(session);
        HistoricalStatistics statistics = service.snapshot();
        assertAll(() -> assertEquals(1, statistics.totalEvents()),
                () -> assertEquals(3, statistics.totalDeathEvents()),
                () -> assertEquals(2, statistics.totalUniqueDeaths()),
                () -> assertEquals("world", statistics.lastEventWorld()),
                () -> assertEquals(75, statistics.lastEventDurationSeconds()),
                () -> assertEquals(3, statistics.lastEventDeathCount()),
                () -> assertEquals(2, statistics.lastEventUniqueDeaths()),
                () -> assertEquals(2, statistics.lastEventParticipants()),
                () -> assertEquals(0, statistics.lastEventSurvivors()),
                () -> assertEquals("Crimson King", statistics.lastBossName()),
                () -> assertEquals("MYTHICMOBS", statistics.lastBossType()),
                () -> assertEquals("2026-07-23T12:01:15Z", statistics.lastEventEndedAt()));
    }

    @Test void abortedOrDuplicateFinishInputDoesNotIncrementThroughServiceGuard(@TempDir Path directory) {
        HistoricalStatisticsService service = service(directory);
        BloodMoonSession active = new BloodMoonSession(UUID.randomUUID(), "world", START);
        service.recordCompletedEvent(active);
        service.recordCompletedEvent(null);
        assertEquals(0, service.snapshot().totalEvents());
    }

    @Test void bossTotalsIgnoreFailuresAndAdministrativeRemovalSignals(@TempDir Path directory) {
        HistoricalStatisticsService service = service(directory);
        service.recordBossSpawned(false);
        service.recordBossSpawned(true);
        service.recordBossDefeated(false);
        service.recordBossDefeated(true);
        assertAll(() -> assertEquals(1, service.snapshot().totalBossesSpawned()),
                () -> assertEquals(1, service.snapshot().totalBossesDefeated()));
    }

    @Test void saveReloadAndRepeatedSaveAreStable(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("statistics.yml");
        HistoricalStatisticsService first = new HistoricalStatisticsService(file, LOGGER);
        first.recordBossSpawned(true);
        BloodMoonSession event = completedSession("second", 10);
        event.end(START.plusSeconds(10));
        first.recordCompletedEvent(event);
        String saved = Files.readString(file);
        first.saveIfDirty();

        HistoricalStatisticsService reloaded = new HistoricalStatisticsService(file, LOGGER);
        assertAll(() -> assertEquals(first.snapshot(), reloaded.snapshot()),
                () -> assertEquals(saved, Files.readString(file)),
                () -> assertFalse(Files.exists(directory.resolve("statistics.yml.tmp"))));
    }

    @Test void corruptFileIsPreservedAndDoesNotPreventSafeStartup(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("statistics.yml");
        Files.writeString(file, "StatisticsVersion: [broken");

        HistoricalStatisticsService service = new HistoricalStatisticsService(file, LOGGER);

        assertEquals(HistoricalStatistics.empty(), service.snapshot());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.filter(path -> path.getFileName().toString()
                    .startsWith("statistics.corrupt-")).count());
        }
        assertTrue(Files.readString(file).contains("StatisticsVersion: 1"));
    }

    @Test void latestCompletedSessionReplacesLastValuesAndDurationNeverNegative(@TempDir Path directory) {
        HistoricalStatisticsService service = service(directory);
        BloodMoonSession first = completedSession("first", 20);
        first.end(START.plusSeconds(20));
        service.recordCompletedEvent(first);
        BloodMoonSession second = completedSession("latest", -5);
        second.end(START.minusSeconds(5));
        service.recordCompletedEvent(second);

        assertAll(() -> assertEquals(2, service.snapshot().totalEvents()),
                () -> assertEquals("latest", service.snapshot().lastEventWorld()),
                () -> assertEquals(0, service.snapshot().lastEventDurationSeconds()));
    }

    private HistoricalStatisticsService service(Path directory) {
        return new HistoricalStatisticsService(directory.resolve("statistics.yml"), LOGGER);
    }

    private BloodMoonSession completedSession(String world, long ignoredDuration) {
        return new BloodMoonSession(UUID.randomUUID(), world, START);
    }
}
