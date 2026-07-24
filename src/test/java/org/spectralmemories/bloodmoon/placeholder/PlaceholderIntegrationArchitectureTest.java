package org.spectralmemories.bloodmoon.placeholder;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderIntegrationArchitectureTest {
    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test void placeholderApiIsOptionalAndAbsentPathIsNoop() throws IOException {
        String plugin = read("src/main/resources/plugin.yml");
        String main = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        assertAll(() -> assertTrue(plugin.contains("- \"PlaceholderAPI\"")),
                () -> assertTrue(main.contains("new NoPlaceholderIntegration()")),
                () -> assertTrue(main.contains("isPluginEnabled(\"PlaceholderAPI\")")));
    }

    @Test void expansionRegistersOnceAndPluginReloadDoesNotRegisterAgain() throws IOException {
        String main = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        String command = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonCommandExecutor.java");
        assertAll(() -> assertEquals(1, occurrences(main, "new org.spectralmemories.bloodmoon.placeholder.papi.PlaceholderApiIntegration")),
                () -> assertFalse(command.contains("PlaceholderApiIntegration")),
                () -> assertTrue(main.contains("placeholderIntegration.close()")));
    }

    @Test void requestPathContainsNoDiskScanSchedulerCommandOrGlobalEntityTraversal() throws IOException {
        String service = read("src/main/java/org/spectralmemories/bloodmoon/placeholder/PlaceholderStateService.java");
        String store = read("src/main/java/org/spectralmemories/bloodmoon/placeholder/PlaceholderSnapshotStore.java");
        String resolver = read("src/main/java/org/spectralmemories/bloodmoon/placeholder/BloodMoonPlaceholderResolver.java");
        String expansion = read("src/main/java/org/spectralmemories/bloodmoon/placeholder/papi/BloodMoonPlaceholderExpansion.java");
        String requestPath = service + store + resolver + expansion;
        assertAll(() -> assertFalse(requestPath.contains("java.nio.file")),
                () -> assertFalse(requestPath.contains("Yaml")),
                () -> assertFalse(requestPath.contains(".participants()")),
                () -> assertFalse(requestPath.contains("Bukkit.getEntity")),
                () -> assertFalse(requestPath.contains(".getWorld()")),
                () -> assertFalse(requestPath.contains(".getPlayer()")),
                () -> assertFalse(requestPath.contains(".isOnline()")),
                () -> assertFalse(requestPath.contains("getLivingEntities")),
                () -> assertFalse(requestPath.contains("getScheduler")),
                () -> assertFalse(requestPath.contains("runTask")),
                () -> assertFalse(requestPath.contains("dispatchCommand")));
    }

    @Test void bossEntityAccessIsConfinedToMainThreadSnapshotPublication() throws IOException {
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        String publisher = read("src/main/java/org/spectralmemories/bloodmoon/snapshot/PlaceholderSnapshotPublisher.java");
        assertAll(() -> assertFalse(actuator.contains("Bukkit.getEntity(")),
                () -> assertTrue(actuator.contains("AtomicReference<BossPlaceholderSnapshot>")),
                () -> assertTrue(actuator.contains("refreshBossPlaceholderSnapshotOnMainThread")),
                () -> assertTrue(publisher.contains("Bukkit.isPrimaryThread()")),
                () -> assertTrue(publisher.contains("publishFromMainThread")),
                () -> assertFalse(publisher.contains("runTask")));
    }

    @Test void bossLifecyclePublishesEveryNarrativeTransition() throws IOException {
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        assertAll(() -> assertTrue(actuator.contains("bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.notSpawned())")),
                () -> assertTrue(actuator.contains("bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.alive(")),
                () -> assertTrue(actuator.contains("bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.defeated(")),
                () -> assertTrue(actuator.contains("bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.none())")),
                () -> assertTrue(actuator.contains("onBossRegainHealth(EntityRegainHealthEvent event)")),
                () -> assertTrue(actuator.contains("if (session != null) session.bossRemoved(currentPlaceholderBossId)")));
    }

    private String read(String path) throws IOException { return Files.readString(root.resolve(path)); }
    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
