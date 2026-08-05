package org.spectralmemories.bloodmoon.snapshot;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

/** Deduplicates world/cause warnings and throttles failed configuration resolution attempts. */
final class SnapshotWorldFailureTracker {
    static final long RETRY_DELAY_MILLIS = 30_000L;

    private final LongSupplier clock;
    private final Set<Failure> warned = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<UUID, Long> retryAfter = new ConcurrentHashMap<>();

    SnapshotWorldFailureTracker(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    boolean mayAttemptResolution(UUID worldId) {
        if (worldId == null) return false;
        return clock.getAsLong() >= retryAfter.getOrDefault(worldId, Long.MIN_VALUE);
    }

    boolean recordFailure(UUID worldId, String cause) {
        if (worldId == null) return false;
        retryAfter.put(worldId, clock.getAsLong() + RETRY_DELAY_MILLIS);
        return warned.add(new Failure(worldId, safeCause(cause)));
    }

    boolean warnOnce(UUID worldId, String cause) {
        return worldId != null && warned.add(new Failure(worldId, safeCause(cause)));
    }

    void resolutionSucceeded(UUID worldId) {
        if (worldId == null) return;
        retryAfter.remove(worldId);
        warned.removeIf(failure -> failure.worldId().equals(worldId)
                && failure.cause().startsWith("configuration-"));
    }

    void clearWarning(UUID worldId, String cause) {
        if (worldId != null) warned.remove(new Failure(worldId, safeCause(cause)));
    }

    private static String safeCause(String cause) {
        return cause == null || cause.isBlank() ? "unknown" : cause;
    }

    private record Failure(UUID worldId, String cause) { }
}
