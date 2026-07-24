package org.spectralmemories.bloodmoon.lifecycle;

/** Stable world-time identity used to scope restart suppression to one Minecraft night. */
public final class NightCycle {
    public static final long TICKS_PER_DAY = 24000L;
    public static final long NIGHT_START = 12000L;

    private NightCycle() { }

    public static long identity(long fullTime) {
        return Math.floorDiv(fullTime, TICKS_PER_DAY);
    }

    public static boolean isNight(long worldTime) {
        return Math.floorMod(worldTime, TICKS_PER_DAY) >= NIGHT_START;
    }
}
