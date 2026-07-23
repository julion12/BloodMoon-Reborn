package org.spectralmemories.bloodmoon.placeholder;

import org.spectralmemories.bloodmoon.session.BossSessionState;

public record PlaceholderContext(boolean active, String world, long remainingSeconds, BossPlaceholderState boss,
                                 BossSessionState bossState,
                                 PlayerPlaceholderState player, SessionPlaceholderState session,
                                 PlaceholderLabels labels) {
    public static PlaceholderContext inactive(PlaceholderLabels labels) {
        return new PlaceholderContext(false, "", 0, BossPlaceholderState.none(),
                BossSessionState.NONE, PlayerPlaceholderState.none(), SessionPlaceholderState.none(), labels);
    }
}
