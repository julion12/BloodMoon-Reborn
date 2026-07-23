package org.spectralmemories.bloodmoon.placeholder;

public record PlaceholderContext(boolean active, String world, long remainingSeconds, BossPlaceholderState boss,
                                 PlayerPlaceholderState player, SessionPlaceholderState session,
                                 PlaceholderLabels labels) {
    public static PlaceholderContext inactive(PlaceholderLabels labels) {
        return new PlaceholderContext(false, "", 0, BossPlaceholderState.none(),
                PlayerPlaceholderState.none(), SessionPlaceholderState.none(), labels);
    }
}
