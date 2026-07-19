package org.spectralmemories.bloodmoon.boss;

import java.util.Locale;

public final class BossModeResolver {
    public enum Mode { VANILLA, MYTHICMOBS, NONE }
    private BossModeResolver() { }

    public static Mode resolve(String configured, boolean mythicEnabled, boolean mythicAvailable, boolean fallback) {
        String value = configured == null ? "VANILLA" : configured.toUpperCase(Locale.ROOT);
        if (value.equals("NONE")) return Mode.NONE;
        if (!value.equals("MYTHICMOBS")) return Mode.VANILLA;
        if (mythicEnabled && mythicAvailable) return Mode.MYTHICMOBS;
        return fallback ? Mode.VANILLA : Mode.NONE;
    }
}
