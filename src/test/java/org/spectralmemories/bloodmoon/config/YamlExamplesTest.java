package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.spectralmemories.bloodmoon.placeholder.BloodMoonPlaceholderResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    @Test void everyExampleYamlLoadsAsOneValidDocument() throws Exception {
        try (var files = Files.list(Path.of("docs/examples"))) {
            List<Path> yamlFiles = files.filter(path -> path.getFileName().toString().endsWith(".yml")).toList();
            assertEquals(6, yamlFiles.size());
            for (Path file : yamlFiles) {
                Object loaded = new Yaml().load(Files.readString(file));
                assertInstanceOf(Map.class, loaded, file.toString());
            }
        }
    }

    @Test void tabExampleContainsAllReadyToCopyDesignsAndDynamicConditions() throws Exception {
        Path file = Path.of("docs/examples/TAB-scoreboards.yml");
        String source = Files.readString(file);
        Map<?, ?> root = assertInstanceOf(Map.class, new Yaml().load(source));
        Map<?, ?> scoreboard = assertInstanceOf(Map.class, root.get("scoreboard"));
        Map<?, ?> designs = assertInstanceOf(Map.class, scoreboard.get("scoreboards"));
        assertAll(() -> assertTrue(designs.containsKey("normal")),
                () -> assertTrue(designs.containsKey("blood-moon-full")),
                () -> assertTrue(designs.containsKey("blood-moon-compact")),
                () -> assertTrue(designs.containsKey("blood-moon-history")),
                () -> assertTrue(source.contains("display-condition: \"%bloodmoon_active%=true\"")),
                () -> assertTrue(source.contains("%bloodmoon_boss_state%")),
                () -> assertTrue(source.contains("%bloodmoon_death_count%")),
                () -> assertTrue(source.contains("%bloodmoon_unique_deaths%")),
                () -> assertTrue(source.contains("%bloodmoon_participants_current%")),
                () -> assertTrue(source.contains("%bloodmoon_survivors_current%")),
                () -> assertTrue(source.contains("%bloodmoon_total_events%")),
                () -> assertTrue(source.contains("%bloodmoon_last_event_world%")));
    }

    @Test void placeholderApiTextExampleContainsEveryPublicPlaceholder() throws Exception {
        String source = Files.readString(Path.of("docs/examples/PlaceholderAPI-examples.txt"));
        assertTrue(source.contains("/papi info bloodmoon"));
        BloodMoonPlaceholderResolver.identifiers().forEach(identifier ->
                assertTrue(source.contains("%bloodmoon_" + identifier + "%"), identifier));
    }
}
