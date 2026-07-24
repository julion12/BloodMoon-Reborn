package org.spectralmemories.bloodmoon.lifecycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class AbortedNightStoreTest {
    @TempDir Path directory;
    private final Logger logger = Logger.getLogger("AbortedNightStoreTest");

    @Test void activeRestartSuppressesTheSameNightAndSurvivesReload() {
        UUID world = UUID.randomUUID();
        Path file = directory.resolve("aborted-nights.yml");
        AbortedNightStore first = new AbortedNightStore(file, logger);
        first.mark(world, "world", 13240, "server-shutdown");

        AbortedNightStore reloaded = new AbortedNightStore(file, logger);
        assertAll(() -> assertTrue(reloaded.suppresses(world, 13400, 13400)),
                () -> assertEquals(0, reloaded.marker(world).cycle()),
                () -> assertEquals("server-shutdown", reloaded.marker(world).cause()));
    }

    @Test void includeLateJoinerConfigurationCannotBypassOrCreateSuppression() {
        for (boolean includeLateJoiners : new boolean[]{false, true}) {
            assertFalse(AutomaticStartPolicy.mayStart(true, false, true),
                    "automatic start must stay suppressed when IncludeLateJoiners=" + includeLateJoiners);
        }
        assertTrue(AutomaticStartPolicy.mayStart(true, false, false));
    }

    @Test void nextNightIsNotBlockedAndExpiresTheOldMarker() {
        UUID world = UUID.randomUUID();
        AbortedNightStore store = store();
        store.mark(world, "world", 13000, "server-shutdown");

        assertFalse(store.suppresses(world, 37000, 13000));
        assertFalse(store.hasMarker(world));
        assertTrue(AutomaticStartPolicy.mayStart(true, false, false));
    }

    @Test void repeatedReloadsDoNotExtendTheBlockedCycle() {
        UUID world = UUID.randomUUID();
        Path file = directory.resolve("aborted-nights.yml");
        new AbortedNightStore(file, logger).mark(world, "world", 13000, "unexpected-stop");

        assertTrue(new AbortedNightStore(file, logger).suppresses(world, 15000, 15000));
        assertTrue(new AbortedNightStore(file, logger).suppresses(world, 23000, 23000));
        assertFalse(new AbortedNightStore(file, logger).suppresses(world, 37000, 13000));
    }

    @Test void markersAreIsolatedByWorld() {
        UUID aborted = UUID.randomUUID();
        UUID unaffected = UUID.randomUUID();
        AbortedNightStore store = store();
        store.mark(aborted, "first", 13000, "server-shutdown");

        assertAll(() -> assertTrue(store.suppresses(aborted, 15000, 15000)),
                () -> assertFalse(store.suppresses(unaffected, 15000, 15000)),
                () -> assertTrue(AutomaticStartPolicy.mayStart(true, false,
                        store.suppresses(unaffected, 15000, 15000))));
    }

    @Test void explicitAdministrativeStartOverridesAndCanClearSuppression() {
        UUID world = UUID.randomUUID();
        AbortedNightStore store = store();
        store.mark(world, "world", 13000, "server-shutdown");

        assertTrue(AutomaticStartPolicy.mayStart(true, true,
                store.suppresses(world, 14000, 14000)));
        store.clear(world);
        assertFalse(store.hasMarker(world));
    }

    @Test void missingMarkerIsBackwardCompatibleAndInvalidStateDoesNotBlock() throws Exception {
        Path file = directory.resolve("aborted-nights.yml");
        AbortedNightStore missing = new AbortedNightStore(file, logger);
        assertFalse(missing.suppresses(UUID.randomUUID(), 13000, 13000));

        Files.writeString(file, "RecoveryVersion: broken\nWorlds: [");
        AbortedNightStore invalid = new AbortedNightStore(file, logger);
        assertFalse(invalid.suppresses(UUID.randomUUID(), 13000, 13000));
        assertTrue(Files.list(directory)
                .anyMatch(path -> path.getFileName().toString().startsWith("aborted-nights.invalid-")));
    }

    @Test void atomicSaveLeavesNoTemporaryFileAndStoresNoPlayerData() throws Exception {
        UUID world = UUID.randomUUID();
        Path file = directory.resolve("aborted-nights.yml");
        AbortedNightStore store = new AbortedNightStore(file, logger);
        store.mark(world, "world", 13000, "server-shutdown");

        String content = Files.readString(file);
        assertAll(() -> assertFalse(Files.exists(directory.resolve("aborted-nights.yml.tmp"))),
                () -> assertTrue(content.contains(world.toString())),
                () -> assertFalse(content.toLowerCase().contains("player")),
                () -> assertFalse(content.toLowerCase().contains("participant")),
                () -> assertFalse(content.toLowerCase().contains("reward")));
    }

    @Test void nightCycleUsesFullTimeRatherThanAProcessLifetimeFlag() {
        assertAll(() -> assertEquals(0, NightCycle.identity(23999)),
                () -> assertEquals(1, NightCycle.identity(24000)),
                () -> assertTrue(NightCycle.isNight(12000)),
                () -> assertFalse(NightCycle.isNight(0)));
    }

    private AbortedNightStore store() {
        return new AbortedNightStore(directory.resolve("aborted-nights.yml"), logger);
    }
}
