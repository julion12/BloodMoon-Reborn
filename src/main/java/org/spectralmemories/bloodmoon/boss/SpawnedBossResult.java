package org.spectralmemories.bloodmoon.boss;

import java.util.UUID;

/** Immutable description of what the configured boss spawn actually produced. */
public record SpawnedBossResult(Status status, BossModeResolver.Mode actualMode, UUID entityUuid,
                                String displayName, boolean vanillaBossBarCreated, boolean fallbackUsed) {
    public enum Status { SUCCESS, DISABLED, FAILED }

    public static SpawnedBossResult success(BossModeResolver.Mode mode, UUID entityUuid, String displayName,
                                            boolean vanillaBossBarCreated) {
        return new SpawnedBossResult(Status.SUCCESS, mode, entityUuid, displayName,
                vanillaBossBarCreated, false);
    }

    public static SpawnedBossResult disabled() {
        return new SpawnedBossResult(Status.DISABLED, BossModeResolver.Mode.NONE, null, "", false, false);
    }

    public static SpawnedBossResult failed() {
        return new SpawnedBossResult(Status.FAILED, BossModeResolver.Mode.NONE, null, "", false, false);
    }

    public SpawnedBossResult asFallback() {
        return new SpawnedBossResult(status, actualMode, entityUuid, displayName,
                vanillaBossBarCreated, true);
    }

    public boolean success() { return status == Status.SUCCESS; }
}
