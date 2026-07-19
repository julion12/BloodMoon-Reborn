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

    private static String sanitize(String value) {
        return value.replace('\r', ' ').replace('\n', ' ');
    }
}
