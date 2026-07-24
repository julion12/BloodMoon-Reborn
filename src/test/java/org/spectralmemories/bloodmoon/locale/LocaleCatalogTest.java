package org.spectralmemories.bloodmoon.locale;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleCatalogTest {
    @Test void bundledEnglishAndSpanishCatalogsAreCompleteYaml() throws Exception {
        Map<String, Object> english = loadResource("locales/en.yml");
        Map<String, Object> spanish = loadResource("locales/es.yml");
        assertTrue(english.keySet().containsAll(LocaleDefaults.english().keySet()));
        assertTrue(spanish.keySet().containsAll(LocaleDefaults.english().keySet()));
        assertEquals("Blood Moon", english.get("BloodMoonTitleBar"));
        assertEquals("Luna de Sangre", spanish.get("BloodMoonTitleBar"));
    }

    @Test void missingSpanishKeyFallsBackToEnglish() {
        Map<String, Object> english = Map.of("Present", "English", "Missing", "English fallback");
        Map<String, Object> spanish = Map.of("Present", "Español");
        LocaleCatalog catalog = new LocaleCatalog(Map.of("Language", "es"), spanish, english);
        assertEquals("Español", catalog.get("Present").orElseThrow());
        assertEquals("English fallback", catalog.get("Missing").orElseThrow());
    }

    @Test void legacyLocalesRemainHighestPriority() {
        LocaleCatalog catalog = new LocaleCatalog(Map.of(
                        "ZombieBossName", "el duro",
                        "PlaceholderBossDisplayDefeated", "&7Estado: &aVencido"),
                Map.of("ZombieBossName", "El Duro",
                        "PlaceholderBossDisplayDefeated", "&7Estado: &aDerrotado"),
                Map.of("ZombieBossName", "The Tough One",
                        "PlaceholderBossDisplayDefeated", "&7Status: &aDefeated"));
        assertEquals("el duro", catalog.get("ZombieBossName").orElseThrow());
        assertEquals("&7Estado: &aVencido",
                catalog.get("PlaceholderBossDisplayDefeated").orElseThrow());
    }

    private static Map<String, Object> loadResource(String name) throws Exception {
        return LocaleCatalog.load(Path.of(LocaleCatalogTest.class.getClassLoader().getResource(name).toURI()));
    }
}
