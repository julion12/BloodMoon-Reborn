package org.spectralmemories.bloodmoon.snapshot;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotWorldFailureTrackerTest {
    @Test void warningIsDeduplicatedByWorldAndCause() {
        SnapshotWorldFailureTracker tracker = new SnapshotWorldFailureTracker(() -> 0L);
        UUID world = UUID.randomUUID();

        assertAll(() -> assertTrue(tracker.recordFailure(world, "missing")),
                () -> assertFalse(tracker.recordFailure(world, "missing")),
                () -> assertTrue(tracker.recordFailure(world, "unloaded")),
                () -> assertTrue(tracker.recordFailure(UUID.randomUUID(), "missing")));
    }

    @Test void failedResolutionIsThrottledAndCanRecover() {
        AtomicLong clock = new AtomicLong(1_000L);
        SnapshotWorldFailureTracker tracker = new SnapshotWorldFailureTracker(clock::get);
        UUID world = UUID.randomUUID();

        assertTrue(tracker.mayAttemptResolution(world));
        tracker.recordFailure(world, "configuration-unavailable");
        assertFalse(tracker.mayAttemptResolution(world));
        clock.addAndGet(SnapshotWorldFailureTracker.RETRY_DELAY_MILLIS);
        assertTrue(tracker.mayAttemptResolution(world));
        tracker.resolutionSucceeded(world);
        assertAll(() -> assertTrue(tracker.mayAttemptResolution(world)),
                () -> assertTrue(tracker.recordFailure(world, "configuration-unavailable")));
    }

    @Test void concurrentWarningsStillLogOnlyOnce() throws Exception {
        SnapshotWorldFailureTracker tracker = new SnapshotWorldFailureTracker(() -> 0L);
        UUID world = UUID.randomUUID();
        AtomicInteger firstWarnings = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(8);
        try {
            var tasks = java.util.stream.IntStream.range(0, 100)
                    .<java.util.concurrent.Callable<Void>>mapToObj(ignored -> () -> {
                        if (tracker.warnOnce(world, "capture")) firstWarnings.incrementAndGet();
                        return null;
                    }).toList();
            executor.invokeAll(tasks);
        } finally {
            executor.shutdownNow();
        }
        assertEquals(1, firstWarnings.get());
    }
}
