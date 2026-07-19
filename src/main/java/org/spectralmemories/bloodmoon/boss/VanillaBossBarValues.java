package org.spectralmemories.bloodmoon.boss;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/** Deterministic health calculations and placeholders for the vanilla boss bar. */
public final class VanillaBossBarValues {
    private VanillaBossBarValues() { }

    public static double progress(double health, double maximum) {
        if (!Double.isFinite(health) || !Double.isFinite(maximum) || maximum <= 0.0) return 0.0;
        return Math.max(0.0, Math.min(1.0, health / maximum));
    }

    public static Map<String, Object> placeholders(String name, double health, double maximum) {
        return placeholders(name, "VANILLA", health, maximum);
    }

    public static Map<String, Object> placeholders(String name, String type, double health, double maximum) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("boss_name", name == null ? "" : name);
        values.put("boss_type", type == null ? "NONE" : type);
        values.put("boss_health", displayNumber(Math.max(0.0, health)));
        values.put("boss_max_health", displayNumber(Math.max(0.0, maximum)));
        values.put("boss_health_percent", Math.round(progress(health, maximum) * 100.0));
        return values;
    }

    private static String displayNumber(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
