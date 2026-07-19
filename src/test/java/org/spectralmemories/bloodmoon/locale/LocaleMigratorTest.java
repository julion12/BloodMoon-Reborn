package org.spectralmemories.bloodmoon.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
