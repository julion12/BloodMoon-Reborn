package org.spectralmemories.bloodmoon.placeholder;

import java.util.Map;
import java.util.UUID;

/**
 * Async-safe facade used by PlaceholderAPI. Snapshot reads contain no Bukkit,
 * world, entity, configuration, scheduler, or filesystem access.
 */
public final class PlaceholderStateService {
    private final PlaceholderSnapshotStore store;

    public PlaceholderStateService(PlaceholderContext inactive) {
        store = new PlaceholderSnapshotStore(inactive);
    }

    public PlaceholderContext snapshot(UUID playerId) {
        return store.snapshot(playerId);
    }

    public void publishFromMainThread(Map<UUID, PlaceholderContext> players,
                                      PlaceholderContext inactive) {
        store.publish(players, inactive);
    }
}
