package org.spectralmemories.bloodmoon.placeholder;

import org.spectralmemories.bloodmoon.statistics.HistoricalStatistics;

/** Constant-time public projection of the immutable historical statistics snapshot. */
public record HistoricalPlaceholderState(
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
        String lastEventEndedAt,
        boolean hasCompletedEvent
) {
    public static HistoricalPlaceholderState from(HistoricalStatistics statistics) {
        return new HistoricalPlaceholderState(statistics.totalEvents(), statistics.totalDeathEvents(),
                statistics.totalUniqueDeaths(), statistics.totalBossesSpawned(),
                statistics.totalBossesDefeated(), statistics.lastEventWorld(),
                statistics.lastEventDurationSeconds(), statistics.lastEventDeathCount(),
                statistics.lastEventUniqueDeaths(), statistics.lastEventParticipants(),
                statistics.lastEventSurvivors(), statistics.lastBossName(), statistics.lastBossType(),
                statistics.lastEventEndedAt(), statistics.hasCompletedEvent());
    }

    public static HistoricalPlaceholderState none() {
        return new HistoricalPlaceholderState(0, 0, 0, 0, 0,
                "", 0, 0, 0, 0, 0, "", "NONE", "", false);
    }
}
