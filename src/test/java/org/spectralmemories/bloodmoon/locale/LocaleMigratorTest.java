package org.spectralmemories.bloodmoon.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

class LocaleMigratorTest {
    @Test void legacyMigrationPreservesCustomizedBossNameAndAddsMissingKeys() {
        String legacy = "LocalesVersion: 0.8.1\nZombieBossName: \"el duro\"\nBossSlain: \"custom\"\n";
        String migrated = LocaleMigrator.migrateContent(legacy);
        assertTrue(migrated.contains("LocalesVersion: 1.1.0"));
        assertTrue(migrated.contains("ZombieBossName: \"el duro\""));
        assertTrue(migrated.contains("BossSlain: \"custom\""));
        assertTrue(migrated.contains("Language: en"));
        assertTrue(migrated.contains("SurvivorRewardReceived:"));
        assertEquals(migrated, LocaleMigrator.migrateContent(migrated));
    }

    @Test void selectorOnlyFileDoesNotCopyEnglishOverSelectedLanguage() {
        String selector = "LocalesVersion: 1.1.0\nLanguage: es\nUseBundledLocales: true\n";
        assertEquals(selector, LocaleMigrator.migrateContent(selector));
    }

    @Test void fileMigrationCreatesOneBackupBeforeChangingContent(@TempDir Path directory) throws Exception {
        Path locale = directory.resolve("locales.yml");
        String original = "LocalesVersion: 0.8.1\nZombieBossName: \"el duro\"\n";
        Files.writeString(locale, original);
        Clock clock = Clock.fixed(Instant.parse("2026-07-19T12:00:00Z"), ZoneOffset.UTC);
        var first = LocaleMigrator.migrate(locale, clock);
        assertTrue(first.changed());
        assertEquals(original, Files.readString(first.backup()));
        assertTrue(Files.readString(locale).contains("ZombieBossName: \"el duro\""));
        assertTrue(!LocaleMigrator.migrate(locale, clock).changed());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.filter(path -> path.getFileName().toString().contains(".bak-")).count());
        }
    }

    @Test void bundledCatalogMigrationAddsPlaceholderLabelsAndPreservesCustomValues(@TempDir Path directory) throws Exception {
        Path locale = directory.resolve("en.yml");
        String original = "LocalesVersion: 1.1.0\nPlaceholderActive: \"Custom active\"\n";
        Files.writeString(locale, original);
        var bundled = new LinkedHashMap<String, Object>();
        bundled.put("LocalesVersion", "1.1.0");
        bundled.put("PlaceholderActive", "Active");
        bundled.put("PlaceholderInactive", "Inactive");
        Clock clock = Clock.fixed(Instant.parse("2026-07-19T12:00:00Z"), ZoneOffset.UTC);
        var migration = LocaleMigrator.migrateCatalog(locale, bundled, clock);
        String result = Files.readString(locale);
        assertAll(() -> assertTrue(migration.changed()),
                () -> assertEquals(original, Files.readString(migration.backup())),
                () -> assertTrue(result.contains("PlaceholderActive: \"Custom active\"")),
                () -> assertTrue(result.contains("PlaceholderInactive: \"Inactive\"")),
                () -> assertEquals(result, LocaleMigrator.migrateCatalogContent(result, bundled)));
    }
}
