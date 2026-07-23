package org.spectralmemories.bloodmoon.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryHygieneTest {
    @Test void runtimeSmokeArtifactsAreNotTracked() throws Exception {
        Process process = new ProcessBuilder("git", "ls-files").redirectErrorStream(true).start();
        List<String> tracked;
        try (var output = process.inputReader()) {
            tracked = output.lines().toList();
        }
        assertEquals(0, process.waitFor());
        assertTrue(tracked.stream().noneMatch(RepositoryHygieneTest::isRuntimeArtifact),
                () -> "Tracked runtime artifacts: " + tracked.stream()
                        .filter(RepositoryHygieneTest::isRuntimeArtifact).toList());
    }

    @Test void requiredRuntimePathsAreIgnored() throws Exception {
        String ignore = Files.readString(Path.of(".gitignore"));
        assertAll(() -> assertTrue(ignore.contains("build/")),
                () -> assertTrue(ignore.contains(".gradle/")),
                () -> assertTrue(ignore.contains("exes/")),
                () -> assertTrue(ignore.contains("/plugins/")),
                () -> assertTrue(ignore.contains("/world/")),
                () -> assertTrue(ignore.contains("/eula.txt")),
                () -> assertTrue(ignore.contains("/server.properties")));
    }

    private static boolean isRuntimeArtifact(String path) {
        String normalized = path.replace('\\', '/').toLowerCase();
        return normalized.startsWith("build/")
                || normalized.startsWith(".gradle/")
                || normalized.startsWith("exes/")
                || normalized.startsWith("server/")
                || normalized.startsWith("smoke/")
                || normalized.startsWith("plugins/")
                || normalized.startsWith("world/")
                || normalized.startsWith("world_nether/")
                || normalized.startsWith("world_the_end/")
                || normalized.endsWith("/eula.txt")
                || normalized.endsWith("/server.properties")
                || normalized.endsWith("/latest.log");
    }
}
