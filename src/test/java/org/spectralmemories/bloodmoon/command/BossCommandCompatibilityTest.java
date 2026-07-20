package org.spectralmemories.bloodmoon.command;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BossCommandCompatibilityTest {
    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test void historicalCommandAndPermissionRemainRegistered() throws IOException {
        String plugin = Files.readString(root.resolve("src/main/resources/plugin.yml"));
        String executor = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/BloodmoonCommandExecutor.java"));
        assertAll(() -> assertTrue(plugin.contains("bloodmoon.spawnzombieboss:")),
                () -> assertTrue(executor.contains("arg0.equalsIgnoreCase(\"spawnzombieboss\")")),
                () -> assertTrue(executor.contains("CheckPermission(sender, \"spawnzombieboss\")")));
    }

    @Test void administrativeCommandUsesConfiguredBossEntryPoint() throws IOException {
        String executor = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/BloodmoonCommandExecutor.java"));
        assertTrue(executor.contains("actuator.SpawnConfiguredBoss()"));
        assertFalse(executor.contains("actuator.SpawnZombieBoss()"));
    }

    @Test void automaticAndPermanentRespawnUseConfiguredBossEntryPoint() throws IOException {
        String actuator = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java"));
        String vanilla = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/ZombieIBoss.java"));
        assertAll(() -> assertTrue(actuator.contains("public void SpawnBosses")),
                () -> assertTrue(actuator.contains("SpawnConfiguredBoss();")),
                () -> assertTrue(vanilla.contains(".SpawnConfiguredBoss();")));
    }

    @Test void mythicSpawnDoesNotConstructOrRefreshVanillaBossBar() throws IOException {
        String actuator = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java"));
        int start = actuator.indexOf("private SpawnedBossResult spawnMythicBoss()");
        int end = actuator.indexOf("private void HandleMythicBossDeath", start);
        String method = actuator.substring(start, end);
        assertAll(() -> assertFalse(method.contains("new ZombieIBoss")),
                () -> assertFalse(method.contains("VanillaBossBar")),
                () -> assertFalse(method.contains("ZombieBossName")));
    }

    @Test void killBossesRemovesTrackedMythicState() throws IOException {
        String actuator = Files.readString(root.resolve("src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java"));
        int start = actuator.indexOf("public void KillBosses (boolean giveRewards, boolean effects, boolean respawn)");
        int end = actuator.indexOf("public void SpawnHorde", start);
        String method = actuator.substring(start, end);
        assertAll(() -> assertTrue(method.contains("getMythicMobs().remove(mythicBossId)")),
                () -> assertTrue(method.contains("mythicBosses.clear()")),
                () -> assertTrue(method.contains("session.bossId(null)")));
    }
}
