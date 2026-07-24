package org.spectralmemories.bloodmoon.distribution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class AdministratorGuideInstallerTest {
    private static final Logger LOGGER = Logger.getLogger("guide-installer-test");

    @Test void finalJarContainsOnlyTheClosedAdministratorManifest() throws Exception {
        Path jar = Path.of("build/libs/BloodMoon-Reborn-1.1.0.jar");
        assertTrue(Files.isRegularFile(jar));
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (var resource : AdministratorGuideInstaller.manifest()) {
                assertNotNull(zip.getEntry(resource.resourcePath()), resource.resourcePath());
            }
            assertNotNull(zip.getEntry("distribution/README.txt"));
            assertNotNull(zip.getEntry("distribution/EXAMPLES/TAB-scoreboards.yml"));
            assertNull(zip.getEntry("distribution/docs/TEST_MATRIX.md"));
            assertNull(zip.getEntry("distribution/docs/RELEASE_NOTES_1.1.0.md"));
        }
    }

    @Test void firstInstallCreatesEveryFileAndDirectoryUnderDataFolder(@TempDir Path directory) {
        Path data = directory.resolve("plugins").resolve("BloodMoon");
        var result = installer(data).installMissing();

        assertAll(() -> assertEquals(AdministratorGuideInstaller.manifest().size(), result.createdCount()),
                () -> assertTrue(result.failed().isEmpty()),
                () -> assertTrue(Files.isRegularFile(data.resolve("README.txt"))),
                () -> assertTrue(Files.isDirectory(data.resolve("EXAMPLES"))),
                () -> assertTrue(Files.isDirectory(data.resolve("docs"))));
        AdministratorGuideInstaller.manifest().forEach(resource ->
                assertTrue(Files.isRegularFile(data.resolve(resource.targetPath())), resource.targetPath()));
    }

    @Test void existingFilesAreNeverOverwrittenOrRetimestamped(@TempDir Path directory) throws Exception {
        Path data = directory.resolve("BloodMoon");
        installer(data).installMissing();
        Path tab = data.resolve("EXAMPLES/TAB-scoreboards.yml");
        Files.writeString(tab, "# administrator customization\n", StandardCharsets.UTF_8);
        FileTime timestamp = FileTime.from(Instant.parse("2026-01-01T00:00:00Z"));
        Files.setLastModifiedTime(tab, timestamp);

        var result = installer(data).installMissing();

        assertAll(() -> assertEquals(0, result.createdCount()),
                () -> assertEquals("# administrator customization\n", Files.readString(tab)),
                () -> assertEquals(timestamp, Files.getLastModifiedTime(tab)));
    }

    @Test void onlyAMissingFileIsRestoredAndSecondRunDoesNothing(@TempDir Path directory) throws Exception {
        Path data = directory.resolve("BloodMoon");
        installer(data).installMissing();
        Map<Path, FileTime> before = timestamps(data);
        Path missing = data.resolve("EXAMPLES/CommandsOnEnd.yml");
        Files.delete(missing);

        var restored = installer(data).installMissing();
        Map<Path, FileTime> afterRestore = timestamps(data);
        var unchanged = installer(data).installMissing();

        assertAll(() -> assertEquals(List.of("EXAMPLES/CommandsOnEnd.yml"), restored.created()),
                () -> assertTrue(Files.isRegularFile(missing)),
                () -> assertEquals(0, unchanged.createdCount()),
                () -> before.forEach((path, time) -> {
                    if (!path.equals(missing)) assertEquals(time, afterRestore.get(path), path.toString());
                }));
    }

    @Test void installationWorksInAPathWithSpacesAndPreservesUtf8(@TempDir Path directory) throws Exception {
        Path data = directory.resolve("Server with spaces").resolve("plugins").resolve("BloodMoon");
        installer(data).installMissing();
        String tab = Files.readString(data.resolve("EXAMPLES/TAB-scoreboards.yml"), StandardCharsets.UTF_8);
        String mythic = Files.readString(data.resolve("EXAMPLES/MythicMobs-BloodMoonBoss.yml"),
                StandardCharsets.UTF_8);
        assertAll(() -> assertTrue(tab.contains("Participación")),
                () -> assertTrue(tab.contains("Caídos")),
                () -> assertTrue(mythic.contains("Luna Carmesí")));
    }

    @Test void copyFailureIsReportedWithoutEscapingTheInstaller(@TempDir Path directory) throws Exception {
        Path data = directory.resolve("BloodMoon");
        Files.createDirectories(data);
        Files.writeString(data.resolve("blocked"), "not a directory");
        var resource = new AdministratorGuideInstaller.ResourceFile(
                "distribution/README.txt", "blocked/README.txt");
        var installer = new AdministratorGuideInstaller(data,
                path -> new ByteArrayInputStream("guide".getBytes(StandardCharsets.UTF_8)),
                LOGGER, List.of(resource));

        var result = assertDoesNotThrow(installer::installMissing);

        assertAll(() -> assertTrue(result.created().isEmpty()),
                () -> assertEquals(List.of("blocked/README.txt"), result.failed()));
    }

    @Test void traversalTargetsAndInvalidResourcePathsAreRejected(@TempDir Path directory) {
        Path data = directory.resolve("BloodMoon");
        List<AdministratorGuideInstaller.ResourceFile> malicious = List.of(
                new AdministratorGuideInstaller.ResourceFile("distribution/README.txt", "../outside.txt"),
                new AdministratorGuideInstaller.ResourceFile("../secret", "inside.txt"));
        var installer = new AdministratorGuideInstaller(data,
                path -> new ByteArrayInputStream("bad".getBytes(StandardCharsets.UTF_8)),
                LOGGER, malicious);

        var result = assertDoesNotThrow(installer::installMissing);

        assertAll(() -> assertEquals(2, result.failed().size()),
                () -> assertFalse(Files.exists(directory.resolve("outside.txt"))),
                () -> assertFalse(Files.exists(data.resolve("inside.txt"))));
    }

    @Test void extractedFilesMatchEmbeddedBytesAndEveryExtractedYamlLoads(@TempDir Path directory) throws Exception {
        Path data = directory.resolve("BloodMoon");
        installer(data).installMissing();
        ClassLoader loader = AdministratorGuideInstaller.class.getClassLoader();
        for (var resource : AdministratorGuideInstaller.manifest()) {
            try (InputStream input = loader.getResourceAsStream(resource.resourcePath())) {
                assertNotNull(input, resource.resourcePath());
                assertArrayEquals(input.readAllBytes(), Files.readAllBytes(data.resolve(resource.targetPath())),
                        resource.targetPath());
            }
            if (resource.targetPath().startsWith("EXAMPLES/") && resource.targetPath().endsWith(".yml")) {
                Object loaded = new Yaml().load(Files.readString(data.resolve(resource.targetPath())));
                assertInstanceOf(Map.class, loaded, resource.targetPath());
            }
        }
    }

    private AdministratorGuideInstaller installer(Path data) {
        return new AdministratorGuideInstaller(data,
                AdministratorGuideInstaller.class.getClassLoader(), LOGGER);
    }

    private Map<Path, FileTime> timestamps(Path data) throws Exception {
        Map<Path, FileTime> result = new LinkedHashMap<>();
        for (var resource : AdministratorGuideInstaller.manifest()) {
            Path file = data.resolve(resource.targetPath());
            result.put(file, Files.getLastModifiedTime(file));
        }
        return result;
    }
}
