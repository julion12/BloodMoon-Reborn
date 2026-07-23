package org.spectralmemories.bloodmoon.placeholder;

import org.spectralmemories.bloodmoon.session.BossSessionState;

public record PlaceholderContext(boolean active, String world, long remainingSeconds, BossPlaceholderState boss,
                                 BossSessionState bossState,
                                 PlayerPlaceholderState player, SessionPlaceholderState session,
                                 HistoricalPlaceholderState history,
                                 PlaceholderLabels labels) {
    public PlaceholderContext(boolean active, String world, long remainingSeconds, BossPlaceholderState boss,
                              BossSessionState bossState, PlayerPlaceholderState player,
                              SessionPlaceholderState session, PlaceholderLabels labels) {
        this(active, world, remainingSeconds, boss, bossState, player, session,
                HistoricalPlaceholderState.none(), labels);
    }

    public static PlaceholderContext inactive(PlaceholderLabels labels) {
        return inactive(labels, HistoricalPlaceholderState.none());
    }

    public static PlaceholderContext inactive(PlaceholderLabels labels, HistoricalPlaceholderState history) {
        return new PlaceholderContext(false, "", 0, BossPlaceholderState.none(),
                BossSessionState.NONE, PlayerPlaceholderState.none(), SessionPlaceholderState.none(),
                history, labels);
    }
}
