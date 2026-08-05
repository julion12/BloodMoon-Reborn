package org.spectralmemories.bloodmoon.lifecycle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RestartSuppressionArchitectureTest {
    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test void cleanShutdownMarksAndPersistsBeforeAbortingTheActuator() throws IOException {
        String plugin = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        int disable = plugin.indexOf("public void onDisable()");
        String body = plugin.substring(disable);
        assertAll(
                () -> assertTrue(body.contains("PrepareAbortedShutdown(\"server-shutdown\")")),
                () -> assertTrue(body.indexOf("PrepareAbortedShutdown") < body.indexOf("UpdateCacheDatabase")),
                () -> assertTrue(body.indexOf("UpdateCacheDatabase") < body.indexOf("actuator.AbortBloodMoon()")));
    }

    @Test void unexpectedStopRecoveryUsesMetadataOnlySessionWorlds() throws IOException {
        String coordinator = read("src/main/java/org/spectralmemories/bloodmoon/session/SessionCoordinator.java");
        String plugin = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        String recovery = read("src/main/java/org/spectralmemories/bloodmoon/lifecycle/AbortedNightStore.java");
        assertAll(() -> assertTrue(coordinator.contains("recoveredIncompleteWorlds")),
                () -> assertTrue(plugin.contains("consumeRecoveredIncompleteWorld(world.getUID())")),
                () -> assertTrue(plugin.contains("nightCheck.RecoverIncompleteSession(recoveredCycle)")),
                () -> assertFalse(recovery.toLowerCase().contains("playerid")),
                () -> assertFalse(recovery.toLowerCase().contains("participants")));
    }

    @Test void manualStartOverridesButStopAndReloadDoNotCreateRecoverySessions() throws IOException {
        String commands = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonCommandExecutor.java");
        int start = commands.indexOf("private boolean ExecuteStart");
        int stop = commands.indexOf("private boolean ExecuteStop");
        String startBody = commands.substring(start, stop);
        String stopBody = commands.substring(stop, commands.indexOf("private boolean ExecuteSpawnZombieBoss", stop));
        assertAll(() -> assertTrue(startBody.contains("RequestManualStart()")),
                () -> assertTrue(stopBody.contains("ClearRestartSuppression()")),
                () -> assertFalse(commands.contains("new AbortedNightStore")),
                () -> assertFalse(commands.contains("RecoverIncompleteSession")));
    }

    @Test void abortedEventCannotPayOrEnterCompletedHistory() throws IOException {
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        String coordinator = read("src/main/java/org/spectralmemories/bloodmoon/session/SessionCoordinator.java");
        int abort = actuator.indexOf("public void AbortBloodMoon");
        assertAll(() -> assertTrue(actuator.substring(abort, abort + 100).contains("stopBloodMoon(false)")),
                () -> assertTrue(coordinator.contains("if (complete) rewardSurvivors")),
                () -> assertTrue(coordinator.contains("if (complete) plugin.getStatisticsService().recordCompletedEvent")));
    }

    @Test void abortedLifecycleCleansVanillaMythicBarsTasksAndReferences() throws IOException {
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        int stop = actuator.indexOf("private void stopBloodMoon(boolean complete)");
        String body = actuator.substring(stop, actuator.indexOf("public void KillBosses", stop));
        assertAll(() -> assertTrue(body.contains("HideNightBar()")),
                () -> assertTrue(body.contains("actuatorPeriodic.close()")),
                () -> assertTrue(body.contains("if (complete)")),
                () -> assertTrue(body.contains("KillBosses(false, false, false)")),
                () -> assertTrue(body.contains("bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.none())")));
    }

    @Test void shutdownAbortCannotScheduleDelayedVanillaBossEffects() throws IOException {
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        int stop = actuator.indexOf("private void stopBloodMoon(boolean complete)");
        String body = actuator.substring(stop, actuator.indexOf("public void KillBosses", stop));
        assertAll(
                () -> assertTrue(body.contains("KillBosses(false, false, false)")),
                () -> assertTrue(body.indexOf("if (complete)") < body.indexOf("KillBosses()")),
                () -> assertTrue(body.indexOf("} else {") < body.indexOf("KillBosses(false, false, false)")));
    }

    @Test void lateJoinerSemanticsRemainOwnedOnlyByExistingConfiguration() throws IOException {
        String coordinator = read("src/main/java/org/spectralmemories/bloodmoon/session/SessionCoordinator.java");
        String recovery = read("src/main/java/org/spectralmemories/bloodmoon/lifecycle/AbortedNightStore.java");
        assertAll(() -> assertTrue(coordinator.contains("GetSurvivorIncludeLateJoiners()")),
                () -> assertTrue(coordinator.contains("allowsRegistration")),
                () -> assertFalse(recovery.contains("IncludeLateJoiners")));
    }

    @Test void unloadAndDisableStopSnapshotAndWorldSchedulersCleanly() throws IOException {
        String plugin = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        String manager = read("src/main/java/org/spectralmemories/bloodmoon/WorldManager.java");
        String night = read("src/main/java/org/spectralmemories/bloodmoon/PeriodicNightCheck.java");
        String actuator = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java");
        int disable = plugin.indexOf("public void onDisable()");
        String disableBody = plugin.substring(disable);
        assertAll(() -> assertTrue(disableBody.contains("placeholderIntegration.close()")),
                () -> assertTrue(disableBody.contains("nightCheck.close()")),
                () -> assertTrue(manager.contains("nightCheck.close()")),
                () -> assertTrue(manager.contains("UnloadWorld(event.getWorld())")),
                () -> assertTrue(night.contains("if (closed) return;")),
                () -> assertTrue(night.contains("HandlerList.unregisterAll(this)")),
                () -> assertTrue(night.contains("nightChecks.remove(world.getUID(), this)")),
                () -> assertTrue(actuator.contains("HandlerList.unregisterAll(this)")),
                () -> assertTrue(actuator.contains("reader != null && reader.GetPermanentBloodMoonConfig()")));
    }

    @Test void configRegistryUsesWorldUuidAndIsClearedOnUnload() throws IOException {
        String plugin = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        String registry = read("src/main/java/org/spectralmemories/bloodmoon/config/WorldConfigRegistry.java");
        assertAll(() -> assertTrue(plugin.contains("new WorldConfigRegistry()")),
                () -> assertTrue(plugin.contains("configReaders.remove(world)")),
                () -> assertTrue(plugin.contains("resolveConfigReader(World world)")),
                () -> assertTrue(registry.contains("ConcurrentMap<UUID, ConfigReader>")),
                () -> assertTrue(registry.contains("world.getUID()")));
    }

    private String read(String relative) throws IOException {
        return Files.readString(root.resolve(relative));
    }
}
