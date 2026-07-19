package org.spectralmemories.bloodmoon.command;

import java.util.Map;

/** Deterministic literal placeholder replacement. Unknown placeholders are retained. */
public final class PlaceholderEngine {
    private PlaceholderEngine() { }

    public static String replace(String template, Map<String, ?> values) {
        if (template == null) return "";
        String result = template;
        if (values == null) return result;
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) continue;
            String token = key.startsWith("%") ? key : "%" + key + "%";
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            result = result.replace(token, sanitize(value));
        }
        return result;
    }

    public static String replaceLegacy(String template, Map<String, ?> values) {
        String result = template == null ? "" : template;
        if (values == null) return result;
        result = result.replace("$w", legacyValue(values, "world"));
        result = result.replace("$p", legacyValue(values, "player"));
        return result.replace("$b", legacyValue(values, "boss_name"));
    }

    private static String legacyValue(Map<String, ?> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : sanitize(String.valueOf(value));
    }

    private static String sanitize(String value) {
        return value.replace('\r', ' ').replace('\n', ' ');
    }
}
