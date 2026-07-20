package org.spectralmemories.bloodmoon.locale;

import org.spectralmemories.bloodmoon.config.SemanticVersion;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Adds missing legacy locale keys with a backup while preserving every existing value. */
public final class LocaleMigrator {
    public static final String TARGET_VERSION = "1.1.0";
    private static final Pattern VERSION = Pattern.compile("(?m)^LocalesVersion\\s*:\\s*([^#\\r\\n]+)");
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private LocaleMigrator() { }

    public static MigrationResult migrate(Path file, Clock clock) throws IOException {
        String original = Files.readString(file, StandardCharsets.UTF_8);
        try {
            new Yaml().load(original);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid legacy locale YAML; original file preserved", exception);
        }
        String migrated = migrateContent(original);
        if (original.equals(migrated)) return new MigrationResult(false, null);
        Path backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()));
        int suffix = 1;
        while (Files.exists(backup)) {
            backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()) + "-" + suffix++);
        }
        Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
        Files.writeString(file, migrated, StandardCharsets.UTF_8);
        return new MigrationResult(true, backup);
    }

    /** Adds keys introduced in a newer bundled language catalog without overwriting administrator values. */
    public static MigrationResult migrateCatalog(Path file, Map<String, Object> bundled, Clock clock) throws IOException {
        String original = Files.readString(file, StandardCharsets.UTF_8);
        try {
            new Yaml().load(original);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid bundled locale YAML; original file preserved", exception);
        }
        String migrated = migrateCatalogContent(original, bundled);
        if (original.equals(migrated)) return new MigrationResult(false, null);
        Path backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()));
        int suffix = 1;
        while (Files.exists(backup)) {
            backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()) + "-" + suffix++);
        }
        Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
        Files.writeString(file, migrated, StandardCharsets.UTF_8);
        return new MigrationResult(true, backup);
    }

    public static String migrateCatalogContent(String original, Map<String, Object> bundled) {
        String source = original == null ? "" : original;
        Map<String, Object> existing = parse(source);
        StringBuilder additions = new StringBuilder();
        if (bundled != null) bundled.forEach((key, value) -> {
            if (key != null && value != null && !existing.containsKey(key)) {
                additions.append(key).append(": \"").append(escape(String.valueOf(value))).append("\"")
                        .append(System.lineSeparator());
            }
        });
        if (additions.isEmpty()) return source;
        return source.stripTrailing() + System.lineSeparator() + System.lineSeparator()
                + "# BloodMoon-Reborn missing bundled locale entries; existing values were preserved."
                + System.lineSeparator() + additions;
    }

    public static String migrateContent(String original) {
        String source = original == null ? "" : original;
        Map<String, Object> existing = parse(source);
        String currentVersion = String.valueOf(existing.getOrDefault("LocalesVersion", "0.0.0"));
        boolean selectorOnly = Boolean.parseBoolean(String.valueOf(existing.getOrDefault("UseBundledLocales", false)));
        boolean versionMissingOrOld = SemanticVersion.parse(currentVersion)
                .compareTo(SemanticVersion.parse(TARGET_VERSION)) < 0;
        boolean languageMissing = !existing.containsKey("Language");
        boolean missingLegacyKeys = !selectorOnly && LocaleDefaults.english().keySet().stream()
                .anyMatch(key -> !existing.containsKey(key));
        if (!versionMissingOrOld && !languageMissing && !missingLegacyKeys) return source;

        String result = source;
        if (versionMissingOrOld) {
            Matcher matcher = VERSION.matcher(result);
            result = matcher.find()
                    ? matcher.replaceFirst(Matcher.quoteReplacement("LocalesVersion: " + TARGET_VERSION))
                    : "LocalesVersion: " + TARGET_VERSION + System.lineSeparator() + result;
        }
        StringBuilder additions = new StringBuilder();
        if (languageMissing) additions.append("Language: en").append(System.lineSeparator());
        if (!selectorOnly) {
            LocaleDefaults.english().forEach((key, value) -> {
                if (!existing.containsKey(key)) {
                    additions.append(key).append(": \"").append(escape(value)).append("\"")
                            .append(System.lineSeparator());
                }
            });
        }
        if (!additions.isEmpty()) {
            result = result.stripTrailing() + System.lineSeparator() + System.lineSeparator()
                    + "# BloodMoon-Reborn 1.1.0 missing locale entries; existing values were preserved."
                    + System.lineSeparator() + additions;
        }
        return result;
    }

    private static Map<String, Object> parse(String source) {
        try {
            Object loaded = new Yaml().load(source);
            if (!(loaded instanceof Map<?, ?> map)) return Collections.emptyMap();
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> {
                if (key != null) result.put(String.valueOf(key), value);
            });
            return result;
        } catch (RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "").replace("\n", "$n");
    }

    public record MigrationResult(boolean changed, Path backup) { }
}
