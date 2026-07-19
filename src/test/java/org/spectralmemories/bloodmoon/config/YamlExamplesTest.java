package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlExamplesTest {
    @Test void documentedMythicMobExampleIsValidYaml() throws Exception {
        Object loaded = new Yaml().load(Files.readString(Path.of("docs/examples/MythicMobs-BloodMoonBoss.yml")));
        Map<?, ?> root = assertInstanceOf(Map.class, loaded);
        Map<?, ?> boss = assertInstanceOf(Map.class, root.get("BloodMoonBoss"));
        assertEquals("&4&lRey de la Luna Carmesí", boss.get("Display"));
        assertTrue(assertInstanceOf(Map.class, boss.get("BossBar")).containsKey("Enabled"));
    }

    @Test void migratedDefaultSectionsRemainValidYaml() {
        String migrated = ConfigMigrator.migrateContent("ConfigVersion: 1.0.1\nBloodMoonInterval: 5\n");
        Map<?, ?> root = assertInstanceOf(Map.class, new Yaml().load(migrated));
        assertInstanceOf(Map.class, root.get("SurvivorRewards"));
        assertInstanceOf(Map.class, root.get("Boss"));
    }
}
