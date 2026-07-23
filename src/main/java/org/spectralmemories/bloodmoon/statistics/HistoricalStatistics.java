package org.spectralmemories.bloodmoon.statistics;

import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.time.Instant;

/** Immutable server-wide aggregate and last-completed-event snapshot. */
public record HistoricalStatistics(
        long totalEvents,
        long totalDeathEvents,
        long totalUniqueDeaths,
        long totalBossesSpawned,
        long totalBossesDefeated,
        String lastEventWorld,
        long lastEventDurationSeconds,
        long lastEventDeathCount,
        int lastEventUniqueDeaths,
        int lastEventParticipants,
        int lastEventSurvivors,
        String lastBossName,
        String lastBossType,
        String lastEventEndedAt
) {
    public HistoricalStatistics {
        totalEvents = nonNegative(totalEvents);
        totalDeathEvents = nonNegative(totalDeathEvents);
        totalUniqueDeaths = nonNegative(totalUniqueDeaths);
        totalBossesSpawned = nonNegative(totalBossesSpawned);
        totalBossesDefeated = nonNegative(totalBossesDefeated);
        lastEventWorld = safe(lastEventWorld);
        lastEventDurationSeconds = nonNegative(lastEventDurationSeconds);
        lastEventDeathCount = nonNegative(lastEventDeathCount);
        lastEventUniqueDeaths = nonNegative(lastEventUniqueDeaths);
        lastEventParticipants = nonNegative(lastEventParticipants);
        lastEventSurvivors = nonNegative(lastEventSurvivors);
        lastBossName = safe(lastBossName);
        lastBossType = safe(lastBossType).isBlank() ? "NONE" : lastBossType;
        lastEventEndedAt = safe(lastEventEndedAt);
    }

    public static HistoricalStatistics empty() {
        return new HistoricalStatistics(0, 0, 0, 0, 0,
                "", 0, 0, 0, 0, 0, "", "NONE", "");
    }

    public HistoricalStatistics recordBossSpawned() {
        return new HistoricalStatistics(totalEvents, totalDeathEvents, totalUniqueDeaths,
                totalBossesSpawned + 1, totalBossesDefeated, lastEventWorld,
                lastEventDurationSeconds, lastEventDeathCount, lastEventUniqueDeaths,
                lastEventParticipants, lastEventSurvivors, lastBossName, lastBossType,
                lastEventEndedAt);
    }

    public HistoricalStatistics recordBossDefeated() {
        return new HistoricalStatistics(totalEvents, totalDeathEvents, totalUniqueDeaths,
                totalBossesSpawned, totalBossesDefeated + 1, lastEventWorld,
                lastEventDurationSeconds, lastEventDeathCount, lastEventUniqueDeaths,
                lastEventParticipants, lastEventSurvivors, lastBossName, lastBossType,
                lastEventEndedAt);
    }

    public HistoricalStatistics recordCompletedEvent(BloodMoonSession session) {
        Instant endedAt = session.endedAt().orElse(Instant.now());
        return new HistoricalStatistics(totalEvents + 1,
                totalDeathEvents + session.totalDeathEvents(),
                totalUniqueDeaths + session.uniqueDeadPlayers(),
                totalBossesSpawned, totalBossesDefeated,
                session.worldName(), session.durationSeconds(), session.totalDeathEvents(),
                session.uniqueDeadPlayers(), session.currentParticipants(), session.currentSurvivors(),
                session.lastBossName(), session.lastBossType(), endedAt.toString());
    }

    public boolean hasCompletedEvent() {
        return totalEvents > 0 && !lastEventEndedAt.isBlank();
    }

    private static long nonNegative(long value) { return Math.max(0, value); }
    private static int nonNegative(int value) { return Math.max(0, value); }
    private static String safe(String value) { return value == null ? "" : value; }
}
