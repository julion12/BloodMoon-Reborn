package org.spectralmemories.bloodmoon.session;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SessionStatisticsArchitectureTest {
    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test void lateJoinerRegistrationFollowsConfigurationAndAllowsRejoins() {
        assertAll(() -> assertTrue(SessionCoordinator.allowsRegistration(true, false)),
                () -> assertTrue(SessionCoordinator.allowsRegistration(true, true)),
                () -> assertTrue(SessionCoordinator.allowsRegistration(false, true)),
                () -> assertFalse(SessionCoordinator.allowsRegistration(false, false)));
    }

    @Test void deathsOutsideAnActiveWorldSessionAreIgnored() throws IOException {
        String coordinator = read("src/main/java/org/spectralmemories/bloodmoon/session/SessionCoordinator.java");
        int method = coordinator.indexOf("public void death(World world, Player player)");
        String body = coordinator.substring(method, coordinator.indexOf("public CommandRunner commandRunner()", method));
        assertAll(() -> assertTrue(body.contains("active.get(world.getUID())")),
                () -> assertTrue(body.contains("if (session != null)")),
                () -> assertTrue(body.indexOf("if (session != null)") < body.indexOf("session.die(")));
    }

    @Test void reloadDoesNotReplaceOrDuplicateTheSessionCoordinator() throws IOException {
        String plugin = read("src/main/java/org/spectralmemories/bloodmoon/Bloodmoon.java");
        String commands = read("src/main/java/org/spectralmemories/bloodmoon/BloodmoonCommandExecutor.java");
        assertAll(() -> assertEquals(1, occurrences(plugin, "new SessionCoordinator(")),
                () -> assertFalse(commands.contains("new SessionCoordinator(")),
                () -> assertFalse(commands.contains("abortAll()")));
    }

    @Test void realTimeCountersAreNotWrittenAsHistoricalData() throws IOException {
        String store = read("src/main/java/org/spectralmemories/bloodmoon/session/SessionStore.java");
        assertAll(() -> assertFalse(store.contains("totalDeathEvents")),
                () -> assertFalse(store.contains("uniqueDeadPlayers")),
                () -> assertFalse(store.contains("currentParticipants")),
                () -> assertFalse(store.contains("currentSurvivors")));
    }

    private String read(String path) throws IOException {
        return Files.readString(root.resolve(path));
    }

    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = 0; (index = source.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
