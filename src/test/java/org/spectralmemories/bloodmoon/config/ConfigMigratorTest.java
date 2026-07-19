package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigMigratorTest {
    @Test void migratesOldConfigurationWithoutChangingCustomValues() {
        String old = "ConfigVersion: 1.0.1\nBloodMoonInterval: 17\nCommandsOnStart:\n  - \"say custom;s\"\n";
        String migrated = ConfigMigrator.migrateContent(old);
        assertTrue(migrated.contains("ConfigVersion: 1.1.0"));
        assertTrue(migrated.contains("BloodMoonInterval: 17"));
        assertTrue(migrated.contains("say custom;s"));
        assertTrue(migrated.contains("SurvivorRewards:"));
        assertTrue(migrated.contains("Boss:"));
    }

    @Test void migrationIsIdempotent() {
        String once = ConfigMigrator.migrateContent("ConfigVersion: 1.0.1\nBloodMoonInterval: 5\n");
        assertEquals(once, ConfigMigrator.migrateContent(once));
    }

    @Test void semanticComparisonIsNotLexicographic() {
        assertTrue(SemanticVersion.parse("1.10.0").compareTo(SemanticVersion.parse("1.9.9")) > 0);
        assertEquals(new SemanticVersion(1, 0, 0), SemanticVersion.parse("1"));
    }
}
