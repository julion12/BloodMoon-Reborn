package org.spectralmemories.bloodmoon.distribution;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseArtifactAuditTest {
    private final Path jar = Path.of(System.getProperty("user.dir"), "build", "libs",
            "BloodMoon-Reborn-1.1.0.jar");

    @Test void releaseJarContainsRequiredLicensesAndNoDevelopmentClasses() throws IOException {
        assertTrue(Files.isRegularFile(jar), "Gradle must build the release JAR before tests");
        try (var zip = FileSystems.newFileSystem(jar, Map.of())) {
            assertAll(
                    () -> assertTrue(Files.isRegularFile(zip.getPath("/META-INF/BloodMoon-Reborn-LICENSE.txt"))),
                    () -> assertTrue(Files.isRegularFile(zip.getPath("/META-INF/APACHE-LICENSE-2.0.txt"))),
                    () -> assertTrue(Files.isRegularFile(zip.getPath("/META-INF/THIRD-PARTY-NOTICES.txt"))),
                    () -> assertFalse(Files.exists(zip.getPath("/org/spectralmemories/bloodmoon/TestCommandExecutor.class"))),
                    () -> assertFalse(Files.exists(zip.getPath("/org/jetbrains/annotations/TestOnly.class"))),
                    () -> assertFalse(Files.exists(zip.getPath("/docs"))),
                    () -> assertFalse(Files.exists(zip.getPath("/src"))));
        }
    }

    @Test void releaseDescriptorExposesOnlyThePublicCommand() throws IOException {
        try (var zip = FileSystems.newFileSystem(jar, Map.of())) {
            String descriptor = Files.readString(zip.getPath("/plugin.yml"));
            assertAll(
                    () -> assertTrue(descriptor.contains("version: 1.1.0")),
                    () -> assertTrue(descriptor.contains("website: https://github.com/julion12/BloodMoon-Reborn")),
                    () -> assertTrue(descriptor.contains("  bloodmoon:")),
                    () -> assertFalse(descriptor.contains("testsuite")));
        }
    }
}
