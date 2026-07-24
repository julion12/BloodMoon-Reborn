package org.spectralmemories.bloodmoon.placeholder;

import org.spectralmemories.bloodmoon.session.BossSessionState;

/** Immutable boss state published atomically by the main thread. */
public record BossPlaceholderSnapshot(BossSessionState state, BossPlaceholderState boss) {
    public BossPlaceholderSnapshot {
        state = state == null ? BossSessionState.NONE : state;
        boss = boss == null ? BossPlaceholderState.none() : boss;
    }

    public static BossPlaceholderSnapshot none() {
        return new BossPlaceholderSnapshot(BossSessionState.NONE, BossPlaceholderState.none());
    }

    public static BossPlaceholderSnapshot notSpawned() {
        return new BossPlaceholderSnapshot(BossSessionState.NOT_SPAWNED, BossPlaceholderState.none());
    }

    public static BossPlaceholderSnapshot alive(String name, String type, double health, double maximumHealth) {
        return new BossPlaceholderSnapshot(BossSessionState.ALIVE,
                new BossPlaceholderState(true, name, type, health, maximumHealth));
    }

    public static BossPlaceholderSnapshot defeated(String name, String type) {
        return new BossPlaceholderSnapshot(BossSessionState.DEFEATED,
                new BossPlaceholderState(false, name, type, 0, 0));
    }
}
