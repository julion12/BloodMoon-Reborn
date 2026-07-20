package org.spectralmemories.bloodmoon.placeholder;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.BloodmoonActuator;
import org.spectralmemories.bloodmoon.ConfigReader;
import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.time.Instant;
import java.util.Optional;

/** Builds read-only snapshots from already tracked runtime state; it performs no disk or global entity scans. */
public final class PlaceholderStateService {
    private final Bloodmoon plugin;

    public PlaceholderStateService(Bloodmoon plugin) { this.plugin = plugin; }

    public PlaceholderContext snapshot(Player player) {
        PlaceholderLabels labels = labels();
        if (player == null || !player.isOnline()) return PlaceholderContext.inactive(labels);
        World world = player.getWorld();
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(world);
        boolean active = actuator != null && actuator.isInProgress();
        ConfigReader config = plugin.getConfigReader(world);
        long remaining = BloodMoonPlaceholderResolver.remainingSeconds(active,
                config.GetPermanentBloodMoonConfig(), world.getTime());
        BossPlaceholderState boss = actuator == null
                ? BossPlaceholderState.none() : actuator.GetBossPlaceholderState();
        PlayerPlaceholderState participant = playerState(player, world, config);
        return new PlaceholderContext(active, world.getName(), remaining, boss, participant, labels);
    }

    private PlayerPlaceholderState playerState(Player player, World world, ConfigReader config) {
        Optional<BloodMoonSession> current = plugin.getSessionCoordinator().current(world);
        if (current.isEmpty()) return PlayerPlaceholderState.none();
        BloodMoonSession session = current.get();
        Optional<BloodMoonSession.Participant> found = session.participant(player.getUniqueId());
        if (found.isEmpty()) return PlayerPlaceholderState.none();
        BloodMoonSession.Participant participant = found.get();
        boolean spectator = player.getGameMode() == GameMode.SPECTATOR;
        boolean npc = player.hasMetadata("NPC") || player.getScoreboardTags().contains("NPC");
        boolean eligible = session.isEligible(player.getUniqueId(), config.GetSurvivorMinimumParticipationSeconds(),
                config.GetSurvivorRequireOnlineAtEnd(), player.isOnline(), player.getWorld().equals(world),
                spectator, npc);
        return new PlayerPlaceholderState(true, participant.participationSeconds(Instant.now()),
                eligible, participant.disqualified());
    }

    private PlaceholderLabels labels() {
        return new PlaceholderLabels(locale("PlaceholderActive"), locale("PlaceholderInactive"),
                locale("PlaceholderNone"), locale("PlaceholderEligible"), locale("PlaceholderDisqualified"),
                locale("PlaceholderNotParticipating"), locale("PlaceholderNoBoss"));
    }

    private String locale(String key) { return plugin.getLocaleReader().GetLocalePlainString(key); }
}
