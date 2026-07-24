package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;
import org.spectralmemories.bloodmoon.placeholder.BloodMoonPlaceholderResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlExamplesTest {
    @Test void documentedMythicMobExampleIsValidYaml() throws Exception {
        Object loaded = strictYaml().load(Files.readString(
                Path.of("docs/examples/MythicMobs-BloodMoonBoss.yml")));
        Map<?, ?> root = assertInstanceOf(Map.class, loaded);
        Map<?, ?> english = assertInstanceOf(Map.class, root.get("BloodMoonBoss_EN"));
        Map<?, ?> spanish = assertInstanceOf(Map.class, root.get("BloodMoonBoss_ES"));
        assertAll(() -> assertEquals("&4&lBlood Moon King", english.get("Display")),
                () -> assertEquals("&4&lRey de la Luna Carmesí", spanish.get("Display")),
                () -> assertTrue(assertInstanceOf(Map.class, english.get("BossBar"))
                        .containsKey("Enabled")),
                () -> assertEquals(english.get("Type"), spanish.get("Type")),
                () -> assertEquals(english.get("Drops"), spanish.get("Drops")));
    }

    @Test void migratedDefaultSectionsRemainValidYaml() {
        String migrated = ConfigMigrator.migrateContent(
                "ConfigVersion: 1.0.1\nBloodMoonInterval: 5\n");
        Map<?, ?> root = assertInstanceOf(Map.class, strictYaml().load(migrated));
        assertInstanceOf(Map.class, root.get("SurvivorRewards"));
        assertInstanceOf(Map.class, root.get("Boss"));
    }

    @Test void everyExampleYamlIsValidAndFunctionalFilesLoadAsMaps() throws Exception {
        try (var files = Files.list(Path.of("docs/examples"))) {
            List<Path> yamlFiles = files
                    .filter(path -> path.getFileName().toString().endsWith(".yml")).toList();
            assertEquals(16, yamlFiles.size());
            for (Path file : yamlFiles) {
                Object loaded = strictYaml().load(Files.readString(file));
                if (!isIndex(file)) assertInstanceOf(Map.class, loaded, file.toString());
            }
        }
    }

    @Test void bilingualIndexesContainOnlyCommentsAndNameBothVariants() throws Exception {
        for (String base : List.of("TAB-scoreboards", "CommandsOnStart", "CommandsOnEnd",
                "SurvivorRewards", "BossRewards")) {
            String source = Files.readString(Path.of("docs/examples/" + base + ".yml"));
            assertAll(() -> assertTrue(source.lines()
                            .allMatch(line -> line.isBlank() || line.startsWith("#")), base),
                    () -> assertNull(strictYaml().load(source), base),
                    () -> assertTrue(source.contains(base + "-en.yml"), base),
                    () -> assertTrue(source.contains(base + "-es.yml"), base));
        }
    }

    @Test void tabVariantsAreFunctionalAndUseExactlyTheSamePlaceholders() throws Exception {
        String english = Files.readString(Path.of("docs/examples/TAB-scoreboards-en.yml"));
        String spanish = Files.readString(Path.of("docs/examples/TAB-scoreboards-es.yml"));
        Map<?, ?> englishRoot = assertInstanceOf(Map.class, strictYaml().load(english));
        Map<?, ?> spanishRoot = assertInstanceOf(Map.class, strictYaml().load(spanish));
        assertAll(() -> assertInstanceOf(Map.class, englishRoot.get("scoreboard")),
                () -> assertInstanceOf(Map.class, spanishRoot.get("scoreboard")),
                () -> assertEquals(placeholders(english), placeholders(spanish)),
                () -> assertTrue(english.contains("%bloodmoon_boss_display_line_1%")),
                () -> assertTrue(english.contains("%bloodmoon_boss_display_line_2%")),
                () -> assertTrue(english.contains("%bloodmoon_boss_display_line_3%")),
                () -> assertFalse(english.contains("%condition:")),
                () -> assertFalse(spanish.contains("%condition:")),
                () -> assertTrue(spanish.contains("LUNA DE SANGRE")),
                () -> assertTrue(spanish.contains("Participación")),
                () -> assertTrue(spanish.contains("Supervivientes")));
    }

    @Test void bilingualFunctionalPairsKeepTechnicalTokensUntranslated() throws Exception {
        for (String base : List.of("CommandsOnStart", "CommandsOnEnd",
                "SurvivorRewards", "BossRewards")) {
            String english = Files.readString(Path.of("docs/examples/" + base + "-en.yml"));
            String spanish = Files.readString(Path.of("docs/examples/" + base + "-es.yml"));
            assertAll(() -> assertInstanceOf(Map.class, strictYaml().load(english), base + "-en"),
                    () -> assertInstanceOf(Map.class, strictYaml().load(spanish), base + "-es"),
                    () -> assertEquals(placeholders(english), placeholders(spanish), base),
                    () -> assertEquals(materials(english), materials(spanish), base));
        }
    }

    @Test void placeholderApiTextExampleContainsEveryPublicPlaceholder() throws Exception {
        String source = Files.readString(Path.of("docs/examples/PlaceholderAPI-examples.txt"));
        assertTrue(source.contains("/papi info bloodmoon"));
        BloodMoonPlaceholderResolver.identifiers().forEach(identifier ->
                assertTrue(source.contains("%bloodmoon_" + identifier + "%"), identifier));
        assertAll(() -> assertTrue(source.contains("ENGLISH")),
                () -> assertTrue(source.contains("ESPAÑOL")),
                () -> assertTrue(source.contains(
                        "/papi parse me %bloodmoon_boss_display_line_1%")),
                () -> assertTrue(source.contains(
                        "/papi parse me %bloodmoon_boss_display_line_2%")),
                () -> assertTrue(source.contains(
                        "/papi parse me %bloodmoon_boss_display_line_3%")));
    }

    private static boolean isIndex(Path path) {
        String name = path.getFileName().toString();
        return Set.of("TAB-scoreboards.yml", "CommandsOnStart.yml", "CommandsOnEnd.yml",
                "SurvivorRewards.yml", "BossRewards.yml").contains(name);
    }

    private static Set<String> placeholders(String source) {
        Pattern pattern = Pattern.compile("%[A-Za-z0-9_:-]+%");
        Set<String> result = new TreeSet<>();
        pattern.matcher(source).results().forEach(match -> result.add(match.group()));
        return result;
    }

    private static Set<String> materials(String source) {
        Pattern pattern = Pattern.compile("minecraft:[a-z0-9_]+");
        Set<String> result = new TreeSet<>();
        pattern.matcher(source).results().forEach(match -> result.add(match.group()));
        return result;
    }

    private static Yaml strictYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }
}
