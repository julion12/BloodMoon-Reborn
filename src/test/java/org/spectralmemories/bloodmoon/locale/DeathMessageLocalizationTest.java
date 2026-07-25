package org.spectralmemories.bloodmoon.locale;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathMessageLocalizationTest {
    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test void deathSuffixPreservesTheClientTranslatableVanillaMessage() throws IOException {
        String actuator = Files.readString(root.resolve(
                "src/main/java/org/spectralmemories/bloodmoon/BloodmoonActuator.java"));
        int handler = actuator.indexOf("public void onPlayerDeath");
        String body = actuator.substring(handler, actuator.indexOf("if (configReader.GetExperienceLossConfig())", handler));

        assertAll(
                () -> assertTrue(body.contains("Component deathMessage = event.deathMessage()")),
                () -> assertTrue(body.contains("event.deathMessage(deathMessage.append(")),
                () -> assertTrue(body.contains("GetLocalePlainString(\"DeathSuffix\")")),
                () -> assertFalse(body.contains("event.getDeathMessage()")),
                () -> assertFalse(body.contains("event.setDeathMessage(")));
    }
}
