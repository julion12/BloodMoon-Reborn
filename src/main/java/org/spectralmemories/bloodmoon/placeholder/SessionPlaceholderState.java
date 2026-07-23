package org.spectralmemories.bloodmoon.placeholder;

/** Constant-time snapshot of counters owned by the active world session. */
public record SessionPlaceholderState(long deathCount, int uniqueDeaths,
                                      int currentParticipants, int currentSurvivors) {
    public static SessionPlaceholderState none() {
        return new SessionPlaceholderState(0, 0, 0, 0);
    }
}
