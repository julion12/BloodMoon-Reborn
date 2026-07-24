package org.spectralmemories.bloodmoon.snapshot;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.BloodmoonActuator;
import org.spectralmemories.bloodmoon.ConfigReader;
import org.spectralmemories.bloodmoon.placeholder.BloodMoonPlaceholderResolver;
import org.spectralmemories.bloodmoon.placeholder.BossPlaceholderSnapshot;
import org.spectralmemories.bloodmoon.placeholder.HistoricalPlaceholderState;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderContext;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderLabels;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderStateService;
import org.spectralmemories.bloodmoon.placeholder.PlayerPlaceholderState;
import org.spectralmemories.bloodmoon.placeholder.SessionPlaceholderState;
import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Captures Bukkit state on the primary thread and atomically publishes immutable contexts. */
public final class PlaceholderSnapshotPublisher {
    private final Bloodmoon plugin;
    private final PlaceholderStateService snapshots;

    public PlaceholderSnapshotPublisher(Bloodmoon plugin, PlaceholderStateService snapshots) {
        this.plugin = plugin;
        this.snapshots = snapshots;
    }

    public static PlaceholderContext initialInactive(Bloodmoon plugin) {
        PlaceholderLabels labels = labels(plugin);
        return PlaceholderContext.inactive(labels, HistoricalPlaceholderState.from(
                plugin.getStatisticsService().snapshot()));
    }

    public void refreshOnMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Placeholder snapshots must be refreshed on the primary thread");
        }
        BloodmoonActuator.refreshAllBossPlaceholderSnapshotsOnMainThread();
        PlaceholderLabels labels = labels(plugin);
        HistoricalPlaceholderState history = HistoricalPlaceholderState.from(
                plugin.getStatisticsService().snapshot());
        PlaceholderContext inactive = PlaceholderContext.inactive(labels, history);
        Map<UUID, PlaceholderContext> players = new LinkedHashMap<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            players.put(player.getUniqueId(), capturePlayer(player, labels, history));
        }
        snapshots.publishFromMainThread(players, inactive);
    }

    private PlaceholderContext capturePlayer(Player player, PlaceholderLabels labels,
                                             HistoricalPlaceholderState history) {
        World world = player.getWorld();
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
        boolean active = actuator != null && actuator.isInProgress();
        ConfigReader config = plugin.getConfigReader(world);
        long remaining = BloodMoonPlaceholderResolver.remainingSeconds(active,
                config.GetPermanentBloodMoonConfig(), world.getTime());
        BossPlaceholderSnapshot boss = active && actuator != null
                ? actuator.getBossPlaceholderSnapshot() : BossPlaceholderSnapshot.none();
        Optional<BloodMoonSession> current = plugin.getSessionCoordinator().current(world);
        PlayerPlaceholderState participant = playerState(player, world, config, current);
        SessionPlaceholderState session = active && current.isPresent()
                ? sessionState(current.get()) : SessionPlaceholderState.none();
        return new PlaceholderContext(active, world.getName(), remaining, boss.boss(), boss.state(),
                participant, session, history, labels);
    }

    private PlayerPlaceholderState playerState(Player player, World world, ConfigReader config,
                                               Optional<BloodMoonSession> current) {
        if (current.isEmpty()) return PlayerPlaceholderState.none();
        BloodMoonSession session = current.get();
        Optional<BloodMoonSession.Participant> found = session.participant(player.getUniqueId());
        if (found.isEmpty()) return PlayerPlaceholderState.none();
        BloodMoonSession.Participant participant = found.get();
        boolean spectator = player.getGameMode() == GameMode.SPECTATOR;
        boolean npc = player.hasMetadata("NPC") || player.getScoreboardTags().contains("NPC");
        boolean eligible = session.isEligible(player.getUniqueId(),
                config.GetSurvivorMinimumParticipationSeconds(),
                config.GetSurvivorRequireOnlineAtEnd(), true, player.getWorld().equals(world),
                spectator, npc);
        return new PlayerPlaceholderState(true, participant.participationSeconds(Instant.now()),
                eligible, participant.disqualified());
    }

    private SessionPlaceholderState sessionState(BloodMoonSession session) {
        return new SessionPlaceholderState(session.totalDeathEvents(), session.uniqueDeadPlayers(),
                session.currentParticipants(), session.currentSurvivors());
    }

    private static PlaceholderLabels labels(Bloodmoon plugin) {
        return new PlaceholderLabels(locale(plugin, "PlaceholderActive"),
                locale(plugin, "PlaceholderInactive"),
                locale(plugin, "PlaceholderNone"), locale(plugin, "PlaceholderEligible"),
                locale(plugin, "PlaceholderDisqualified"), locale(plugin, "PlaceholderNotParticipating"),
                locale(plugin, "PlaceholderNoBoss"), locale(plugin, "PlaceholderBossStateNone"),
                locale(plugin, "PlaceholderBossStateNotSpawned"), locale(plugin, "PlaceholderBossStateAlive"),
                locale(plugin, "PlaceholderBossStateDefeated"),
                locale(plugin, "PlaceholderBossDisplayNotSpawned"),
                locale(plugin, "PlaceholderBossDisplayName"),
                locale(plugin, "PlaceholderBossDisplayDefeatedName"),
                locale(plugin, "PlaceholderBossDisplayType"),
                locale(plugin, "PlaceholderBossDisplayHealth"),
                locale(plugin, "PlaceholderBossDisplayDefeated"));
    }

    private static String locale(Bloodmoon plugin, String key) {
        return plugin.getLocaleReader().GetLocalePlainString(key);
    }
}
