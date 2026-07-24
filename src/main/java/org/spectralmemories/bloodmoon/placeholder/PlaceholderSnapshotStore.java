package org.spectralmemories.bloodmoon.placeholder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Lock-free publication and lookup of complete immutable player contexts. */
public final class PlaceholderSnapshotStore {
    private final AtomicReference<PublishedSnapshots> published;

    public PlaceholderSnapshotStore(PlaceholderContext inactive) {
        published = new AtomicReference<>(new PublishedSnapshots(Map.of(), inactive));
    }

    public PlaceholderContext snapshot(UUID playerId) {
        PublishedSnapshots current = published.get();
        if (playerId == null) return current.inactive();
        return current.players().getOrDefault(playerId, current.inactive());
    }

    public void publish(Map<UUID, PlaceholderContext> players, PlaceholderContext inactive) {
        published.set(new PublishedSnapshots(Map.copyOf(players), inactive));
    }

    private record PublishedSnapshots(Map<UUID, PlaceholderContext> players,
                                      PlaceholderContext inactive) {
        private PublishedSnapshots {
            players = Map.copyOf(players);
            if (inactive == null) throw new IllegalArgumentException("inactive snapshot is required");
        }
    }
}
