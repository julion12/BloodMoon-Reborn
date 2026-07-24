package org.spectralmemories.bloodmoon.placeholder;

import org.junit.jupiter.api.Test;
import org.spectralmemories.bloodmoon.session.BossSessionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderSnapshotStoreConcurrencyTest {
    private static final PlaceholderLabels LABELS = new PlaceholderLabels(
            "Active", "Inactive", "None", "Eligible", "Disqualified", "Not participating",
            "No boss", "No event", "Not spawned", "Alive", "Defeated",
            "pending", "name:%boss_name%", "won:%boss_name%", "type:%boss_type%",
            "health:%boss_health%", "defeated");

    @Test void oneThousandConcurrentReadsOfAllIdentifiersAreSafe() throws Exception {
        UUID playerId = UUID.randomUUID();
        PlaceholderContext alive = context("world", "Vanilla", "VANILLA", 75, 100);
        PlaceholderSnapshotStore store = new PlaceholderSnapshotStore(PlaceholderContext.inactive(LABELS));
        store.publish(Map.of(playerId, alive), PlaceholderContext.inactive(LABELS));
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> reads = new ArrayList<>();
            for (int index = 0; index < 1000; index++) {
                reads.add(executor.submit(() -> BloodMoonPlaceholderResolver.identifiers().forEach(identifier ->
                        assertNotNull(BloodMoonPlaceholderResolver.resolve(store.snapshot(playerId), identifier),
                                identifier))));
            }
            for (Future<?> read : reads) read.get();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test void completeSnapshotsArePublishedAtomicallyWithoutMixedFields() throws Exception {
        UUID playerId = UUID.randomUUID();
        PlaceholderContext vanilla = context("world-a", "Vanilla", "VANILLA", 75, 100);
        PlaceholderContext mythic = context("world-b", "Mythic", "MYTHICMOBS", 200, 400);
        PlaceholderSnapshotStore store = new PlaceholderSnapshotStore(PlaceholderContext.inactive(LABELS));
        store.publish(Map.of(playerId, vanilla), PlaceholderContext.inactive(LABELS));
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            Future<?> writer = executor.submit(() -> {
                for (int index = 0; index < 10_000; index++) {
                    PlaceholderContext next = (index & 1) == 0 ? vanilla : mythic;
                    store.publish(Map.of(playerId, next), PlaceholderContext.inactive(LABELS));
                }
            });
            List<Future<?>> readers = new ArrayList<>();
            for (int task = 0; task < 5; task++) {
                readers.add(executor.submit(() -> {
                    for (int index = 0; index < 10_000; index++) {
                        PlaceholderContext value = store.snapshot(playerId);
                        boolean completeVanilla = value.world().equals("world-a")
                                && value.boss().name().equals("Vanilla")
                                && value.boss().type().equals("VANILLA")
                                && value.boss().health() == 75;
                        boolean completeMythic = value.world().equals("world-b")
                                && value.boss().name().equals("Mythic")
                                && value.boss().type().equals("MYTHICMOBS")
                                && value.boss().health() == 200;
                        assertTrue(completeVanilla || completeMythic);
                    }
                }));
            }
            writer.get();
            for (Future<?> reader : readers) reader.get();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test void worldsAndLifecycleStatesRemainIndependent() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        PlaceholderContext notSpawned = new PlaceholderContext(true, "world-a", 60,
                BossPlaceholderState.none(), BossSessionState.NOT_SPAWNED,
                PlayerPlaceholderState.none(), SessionPlaceholderState.none(), LABELS);
        PlaceholderContext defeated = new PlaceholderContext(true, "world-b", 60,
                new BossPlaceholderState(false, "Mythic", "MYTHICMOBS", 0, 0),
                BossSessionState.DEFEATED, PlayerPlaceholderState.none(),
                SessionPlaceholderState.none(), LABELS);
        PlaceholderSnapshotStore store = new PlaceholderSnapshotStore(PlaceholderContext.inactive(LABELS));
        store.publish(Map.of(first, notSpawned, second, defeated), PlaceholderContext.inactive(LABELS));

        assertAll(() -> assertEquals("NOT_SPAWNED",
                        BloodMoonPlaceholderResolver.resolve(store.snapshot(first), "boss_state")),
                () -> assertEquals("DEFEATED",
                        BloodMoonPlaceholderResolver.resolve(store.snapshot(second), "boss_state")),
                () -> assertEquals("", BloodMoonPlaceholderResolver.resolve(
                        store.snapshot(UUID.randomUUID()), "boss_display_line_1")));
    }

    @Test void laterHealthAndDeathSnapshotsBecomeVisibleToAsyncReaders() throws Exception {
        UUID playerId = UUID.randomUUID();
        PlaceholderSnapshotStore store = new PlaceholderSnapshotStore(PlaceholderContext.inactive(LABELS));
        store.publish(Map.of(playerId, context("world", "Boss", "VANILLA", 100, 100)),
                PlaceholderContext.inactive(LABELS));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            store.publish(Map.of(playerId, context("world", "Boss", "VANILLA", 25, 100)),
                    PlaceholderContext.inactive(LABELS));
            assertEquals("25", executor.submit(() -> BloodMoonPlaceholderResolver.resolve(
                    store.snapshot(playerId), "boss_health_percent")).get());

            PlaceholderContext defeated = new PlaceholderContext(true, "world", 60,
                    new BossPlaceholderState(false, "Boss", "VANILLA", 0, 0),
                    BossSessionState.DEFEATED, PlayerPlaceholderState.none(),
                    SessionPlaceholderState.none(), LABELS);
            store.publish(Map.of(playerId, defeated), PlaceholderContext.inactive(LABELS));
            assertEquals("DEFEATED", executor.submit(() -> BloodMoonPlaceholderResolver.resolve(
                    store.snapshot(playerId), "boss_state")).get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static PlaceholderContext context(String world, String name, String type,
                                              double health, double maximum) {
        return new PlaceholderContext(true, world, 60,
                new BossPlaceholderState(true, name, type, health, maximum),
                BossSessionState.ALIVE, PlayerPlaceholderState.none(),
                SessionPlaceholderState.none(), LABELS);
    }
}
