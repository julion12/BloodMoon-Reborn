package org.spectralmemories.bloodmoon.statistics;

import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/** Thread-visible in-memory authority; disk access only occurs on lifecycle mutations. */
public final class HistoricalStatisticsService {
    private final StatisticsStore store;
    private volatile HistoricalStatistics snapshot;
    private final Set<UUID> completedSessions = new HashSet<>();
    private boolean dirty;

    public HistoricalStatisticsService(Path file, Logger logger) {
        this.store = new StatisticsStore(file, logger);
        this.snapshot = store.loadOrCreate();
    }

    public HistoricalStatistics snapshot() {
        return snapshot;
    }

    public synchronized void recordBossSpawned(boolean successful) {
        if (!successful) return;
        snapshot = snapshot.recordBossSpawned();
        dirty = true;
        saveIfDirty();
    }

    public synchronized void recordBossDefeated(boolean naturalDefeat) {
        if (!naturalDefeat) return;
        snapshot = snapshot.recordBossDefeated();
        dirty = true;
        saveIfDirty();
    }

    public synchronized void recordCompletedEvent(BloodMoonSession session) {
        if (session == null || session.endedAt().isEmpty()) return;
        if (!completedSessions.add(session.sessionId())) return;
        snapshot = snapshot.recordCompletedEvent(session);
        dirty = true;
        saveIfDirty();
    }

    public synchronized void saveIfDirty() {
        if (dirty && store.save(snapshot)) dirty = false;
    }
}
