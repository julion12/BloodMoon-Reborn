package org.spectralmemories.bloodmoon.boss;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BossLifecycleArchitectureTest {
    private final Path actuator = Path.of(System.getProperty("user.dir"), "src", "main", "java",
            "org", "spectralmemories", "bloodmoon", "BloodmoonActuator.java");

    @Test void completedAndAdministrativeBossLifecyclesReleaseTrackingState() throws IOException {
        String source = Files.readString(actuator);

        assertAll(
                () -> assertTrue(source.contains("private void forgetBossLifecycle(UUID bossId)")),
                () -> assertTrue(occurrences(source, "forgetBossLifecycle(") >= 4),
                () -> assertTrue(source.contains("bossDamagers.remove(boss.getUniqueId())")),
                () -> assertTrue(source.contains("administrativelyRemovedBosses.remove(bossId)")),
                () -> assertTrue(source.contains("historicallyDefeatedBosses.remove(bossId)")),
                () -> assertTrue(source.contains("rewardedBosses.remove(bossId)")));
    }

    @Test void closeDoesNotIgnoreTrackedMythicBosses() throws IOException {
        String source = Files.readString(actuator);

        assertTrue(source.contains("if (bosses.isEmpty() && mythicBosses.isEmpty()) return;"));
    }

    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
