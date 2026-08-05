package org.spectralmemories.bloodmoon.snapshot;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.ConfigReader;
import org.spectralmemories.bloodmoon.placeholder.HistoricalPlaceholderState;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderContext;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderLabels;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderStateService;
import org.spectralmemories.bloodmoon.session.SessionCoordinator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaceholderSnapshotPublisherTest {
    @Test void registeredNormalWorldCapturesWithoutResolvingAgain() {
        Fixture fixture = fixture("world");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(fixture.config);

        PlaceholderContext captured = fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none());

        assertFalse(captured.active());
        verify(fixture.plugin, never()).resolveConfigReader(any());
    }

    @Test void worldLoadedAfterStartupIsResolvedAndRegisteredForCapture() {
        Fixture fixture = fixture("multiverse_void");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(null);
        when(fixture.plugin.resolveConfigReader(fixture.world)).thenReturn(fixture.config);

        PlaceholderContext captured = fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none());

        assertFalse(captured.active());
        verify(fixture.plugin).resolveConfigReader(fixture.world);
    }

    @Test void playerInWorldWithoutConfigGetsSafeInactiveSnapshot() {
        Fixture fixture = fixture("missing_config");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(null);
        when(fixture.plugin.resolveConfigReader(fixture.world)).thenReturn(null);

        PlaceholderContext captured = fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none());

        assertAll(() -> assertFalse(captured.active()),
                () -> assertEquals("", captured.world()),
                () -> assertEquals(0, captured.remainingSeconds()),
                () -> assertFalse(captured.player().participating()),
                () -> assertEquals(0, captured.session().deathCount()));
    }

    @Test void unloadedWorldUsesSafeSnapshotWhenResolutionCannotFindIt() {
        Fixture fixture = fixture("unloaded");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(null);
        when(fixture.plugin.resolveConfigReader(fixture.world)).thenReturn(null);

        assertDoesNotThrow(() -> fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none()));
    }

    @Test void invalidPlayerDoesNotPreventOtherPlayersFromBeingPublished() {
        Fixture valid = fixture("valid");
        Fixture invalid = fixture("invalid");
        PlaceholderSnapshotPublisher publisher = valid.publisher;
        when(valid.plugin.getConfigReader(valid.world)).thenReturn(valid.config);
        when(valid.plugin.getConfigReader(invalid.world)).thenThrow(new IllegalStateException("missing registry"));
        when(invalid.player.getUniqueId()).thenReturn(UUID.randomUUID());
        PlaceholderContext inactive = PlaceholderContext.inactive(valid.labels);

        Map<UUID, PlaceholderContext> captured = publisher.capturePlayers(
                List.of(invalid.player, valid.player), valid.labels,
                HistoricalPlaceholderState.none(), inactive);

        assertAll(() -> assertEquals(2, captured.size()),
                () -> assertSame(inactive, captured.get(invalid.player.getUniqueId())),
                () -> assertNotNull(captured.get(valid.player.getUniqueId())));
    }

    @Test void rapidWorldChangeUsesOneConsistentWorldPerCapture() {
        Fixture fixture = fixture("first");
        World second = world("second", UUID.randomUUID());
        ConfigReader secondConfig = config();
        when(fixture.player.getWorld()).thenReturn(fixture.world, second);
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(fixture.config);
        when(fixture.plugin.getConfigReader(second)).thenReturn(secondConfig);

        assertDoesNotThrow(() -> fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none()));
        assertDoesNotThrow(() -> fixture.publisher.capturePlayer(
                fixture.player, fixture.labels, HistoricalPlaceholderState.none()));

        verify(fixture.plugin).getConfigReader(fixture.world);
        verify(fixture.plugin).getConfigReader(second);
    }

    @Test void missingConfigurationWarningDoesNotSpamEveryRefresh() {
        Fixture fixture = fixture("deduplicated");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(null);
        when(fixture.plugin.resolveConfigReader(fixture.world)).thenReturn(null);

        fixture.publisher.capturePlayer(fixture.player, fixture.labels, HistoricalPlaceholderState.none());
        fixture.publisher.capturePlayer(fixture.player, fixture.labels, HistoricalPlaceholderState.none());

        verify(fixture.logger, times(1)).warning(contains("configuration-unavailable"));
    }

    @Test void validConfigurationClearsTheMissingWorldWarning() {
        Fixture fixture = fixture("temporary");
        when(fixture.plugin.getConfigReader(fixture.world)).thenReturn(null, fixture.config, null);
        when(fixture.plugin.resolveConfigReader(fixture.world)).thenReturn(null);

        fixture.publisher.capturePlayer(fixture.player, fixture.labels, HistoricalPlaceholderState.none());
        fixture.publisher.capturePlayer(fixture.player, fixture.labels, HistoricalPlaceholderState.none());
        fixture.publisher.capturePlayer(fixture.player, fixture.labels, HistoricalPlaceholderState.none());

        verify(fixture.logger, times(2)).warning(contains("configuration-unavailable"));
    }

    private static Fixture fixture(String worldName) {
        Bloodmoon plugin = mock(Bloodmoon.class);
        Player player = mock(Player.class);
        World world = world(worldName, UUID.randomUUID());
        ConfigReader config = config();
        SessionCoordinator sessions = mock(SessionCoordinator.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getWorld()).thenReturn(world);
        when(plugin.getSessionCoordinator()).thenReturn(sessions);
        when(sessions.current(any(World.class))).thenReturn(Optional.empty());
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
        PlaceholderLabels labels = labels();
        PlaceholderSnapshotPublisher publisher = new PlaceholderSnapshotPublisher(plugin,
                new PlaceholderStateService(PlaceholderContext.inactive(labels)));
        return new Fixture(plugin, player, world, config, labels, publisher, logger);
    }

    private static World world(String name, UUID id) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        when(world.getUID()).thenReturn(id);
        when(world.getTime()).thenReturn(1_000L);
        return world;
    }

    private static ConfigReader config() {
        ConfigReader config = mock(ConfigReader.class);
        when(config.GetPermanentBloodMoonConfig()).thenReturn(false);
        return config;
    }

    private static PlaceholderLabels labels() {
        return new PlaceholderLabels("Active", "Inactive", "None", "Eligible",
                "Disqualified", "Not participating", "No boss", "No event",
                "Not spawned", "Alive", "Defeated", "", "", "", "", "", "");
    }

    private record Fixture(Bloodmoon plugin, Player player, World world, ConfigReader config,
                           PlaceholderLabels labels, PlaceholderSnapshotPublisher publisher,
                           Logger logger) { }
}
