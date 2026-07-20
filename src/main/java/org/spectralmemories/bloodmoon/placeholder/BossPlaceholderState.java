package org.spectralmemories.bloodmoon.placeholder;

public record BossPlaceholderState(boolean alive, String name, String type, double health, double maximumHealth) {
    public static BossPlaceholderState none() { return new BossPlaceholderState(false, "", "NONE", 0, 0); }
}
