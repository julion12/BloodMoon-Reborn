package org.spectralmemories.bloodmoon.boss;

import java.util.Locale;
import java.util.function.Supplier;

/** Resolves the configured mode before invoking either concrete spawning implementation. */
public final class ConfiguredBossSpawner {
    private ConfiguredBossSpawner() { }

    public static SpawnedBossResult spawn(String configuredMode, boolean mythicEnabled, boolean mythicAvailable,
                                          boolean fallbackToVanilla,
                                          Supplier<SpawnedBossResult> vanillaSpawner,
                                          Supplier<SpawnedBossResult> mythicSpawner) {
        String requested = configuredMode == null ? "VANILLA" : configuredMode.trim().toUpperCase(Locale.ROOT);
        if (requested.equals("NONE") || requested.equals("DISABLED")) return SpawnedBossResult.disabled();
        if (!requested.equals("MYTHICMOBS")) return vanillaSpawner.get();

        if (mythicEnabled && mythicAvailable) {
            SpawnedBossResult mythic = mythicSpawner.get();
            if (mythic.success()) return mythic;
        }
        if (!fallbackToVanilla) return SpawnedBossResult.failed();
        return vanillaSpawner.get().asFallback();
    }
}
