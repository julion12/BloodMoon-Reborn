package org.spectralmemories.bloodmoon.placeholder;

public record PlayerPlaceholderState(boolean participating, long participationSeconds,
                                     boolean eligible, boolean disqualified) {
    public static PlayerPlaceholderState none() { return new PlayerPlaceholderState(false, 0, false, false); }
}
