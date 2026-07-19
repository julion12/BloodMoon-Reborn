package org.spectralmemories.bloodmoon.locale;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Immutable legacy override -> selected language -> English fallback catalog. */
public final class LocaleCatalog {
    private final Map<String, Object> legacy;
    private final Map<String, Object> selected;
    private final Map<String, Object> english;

    public LocaleCatalog(Map<String, Object> legacy, Map<String, Object> selected, Map<String, Object> english) {
        this.legacy = copy(legacy);
        this.selected = copy(selected);
        this.english = copy(english);
    }

    public Optional<String> get(String key) {
        return value(legacy, key).or(() -> value(selected, key)).or(() -> value(english, key));
    }

    public String language() {
        return value(legacy, "Language").filter(value -> !value.isBlank()).orElse("en");
    }

    public String version() {
        return value(legacy, "LocalesVersion").orElse("NaN");
    }

    public static Map<String, Object> load(Path file) throws IOException {
        if (file == null || !Files.isRegularFile(file)) return Collections.emptyMap();
        try (InputStream input = Files.newInputStream(file)) {
            Object loaded = new Yaml().load(input);
            if (!(loaded instanceof Map<?, ?> map)) return Collections.emptyMap();
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> {
                if (key != null) result.put(String.valueOf(key), value);
            });
            return result;
        }
    }

    private static Optional<String> value(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null || "null".equals(String.valueOf(value))) return Optional.empty();
        return Optional.of(String.valueOf(value));
    }

    private static Map<String, Object> copy(Map<String, Object> source) {
        return source == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
