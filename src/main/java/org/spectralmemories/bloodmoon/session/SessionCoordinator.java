package org.spectralmemories.bloodmoon.session;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.ConfigReader;
import org.spectralmemories.bloodmoon.command.CommandExecutionMode;
import org.spectralmemories.bloodmoon.command.CommandRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SessionCoordinator {
    private final Bloodmoon plugin;
    private final CommandRunner commandRunner;
    private final SessionStore store;
    private final Map<UUID, BloodMoonSession> active = new LinkedHashMap<>();

    public SessionCoordinator(Bloodmoon plugin) {
        this.plugin = plugin;
        this.commandRunner = new CommandRunner(plugin);
        this.store = new SessionStore(plugin);
        store.discardIncompleteOnStartup();
    }

    public BloodMoonSession start(World world) {
        BloodMoonSession existing = active.get(world.getUID());
        if (existing != null) return existing;
        BloodMoonSession session = new BloodMoonSession(world.getUID(), world.getName(), Instant.now());
        world.getPlayers().forEach(player -> session.join(player.getUniqueId(), Instant.now()));
        active.put(world.getUID(), session);
        store.save(active.values());
        return session;
    }

    public Optional<BloodMoonSession> current(World world) { return Optional.ofNullable(active.get(world.getUID())); }

    public BloodMoonSession finish(World world, boolean complete) {
        BloodMoonSession session = active.get(world.getUID());
        if (session == null) return null;
        session.end(Instant.now());
        if (complete) rewardSurvivors(world, session);
        active.remove(world.getUID());
        store.save(active.values());
        return session;
    }

    private void rewardSurvivors(World world, BloodMoonSession session) {
        ConfigReader config = plugin.getConfigReader(world);
        if (!config.GetSurvivorRewardsEnabled()) return;
        List<Player> eligible = new ArrayList<>();
        for (BloodMoonSession.Participant participant : session.participants()) {
            Player player = plugin.getServer().getPlayer(participant.playerId());
            boolean online = player != null && player.isOnline();
            boolean sameWorld = online && player.getWorld().getUID().equals(world.getUID());
            boolean spectator = online && player.getGameMode() == GameMode.SPECTATOR;
            boolean npc = online && (player.hasMetadata("NPC") || player.getScoreboardTags().contains("NPC"));
            if (session.isEligible(participant.playerId(), config.GetSurvivorMinimumParticipationSeconds(),
                    config.GetSurvivorRequireOnlineAtEnd(), online, sameWorld, spectator, npc)) {
                if (config.GetSurvivorRewardOnce() && !session.markRewarded(participant.playerId())) continue;
                if (player != null) eligible.add(player);
            }
        }
        Map<String, Object> extras = Map.of("survivor_count", eligible.size());
        for (Player player : eligible) {
            org.spectralmemories.bloodmoon.LocaleReader.MessageLocale("SurvivorRewardReceived", null, null, player);
            for (String message : config.GetSurvivorMessages()) {
                player.sendMessage(CommandRunner.render(message, world, session, player, extras));
            }
            commandRunner.run(config.GetSurvivorCommands(), CommandExecutionMode.SERVER_FOR_EACH_PLAYER,
                    world, session, List.of(player), extras);
            plugin.getLogger().info("Survivor reward processed for " + player.getUniqueId() + " in session " + session.sessionId());
        }
        if (eligible.isEmpty()) {
            String message = plugin.getLocaleReader().GetLocalePlainString("NoEligibleSurvivors")
                    .replace("%world%", world.getName());
            plugin.getLogger().info(message);
        }
        store.save(active.values());
    }

    public void join(World world, Player player) {
        BloodMoonSession session = active.get(world.getUID());
        if (session == null) return;
        ConfigReader config = plugin.getConfigReader(world);
        if (!allowsRegistration(config.GetSurvivorIncludeLateJoiners(),
                session.participant(player.getUniqueId()).isPresent())) return;
        session.join(player.getUniqueId(), Instant.now());
        store.save(active.values());
    }

    static boolean allowsRegistration(boolean includeLateJoiners, boolean alreadyRegistered) {
        return includeLateJoiners || alreadyRegistered;
    }

    public void leave(World world, Player player) {
        BloodMoonSession session = active.get(world.getUID());
        if (session != null) {
            session.leaveWorld(player.getUniqueId(), Instant.now(), plugin.getConfigReader(world).GetSurvivorDisqualifyOnWorldLeave());
            store.save(active.values());
        }
    }

    public void disconnect(Player player) {
        for (BloodMoonSession session : active.values()) {
            if (session.participant(player.getUniqueId()).isPresent()) {
                World world = plugin.getServer().getWorld(session.worldId());
                session.disconnect(player.getUniqueId(), Instant.now(), world != null && plugin.getConfigReader(world).GetSurvivorDisqualifyOnDisconnect());
            }
        }
        store.save(active.values());
    }

    public void death(World world, Player player) {
        BloodMoonSession session = active.get(world.getUID());
        if (session != null) {
            boolean disqualify = plugin.getConfigReader(world).GetSurvivorDisqualifyOnDeath();
            session.die(player.getUniqueId(), disqualify);
            if (disqualify && plugin.getConfigReader(world).GetSurvivorRewardsEnabled()) {
                org.spectralmemories.bloodmoon.LocaleReader.MessageLocale("SurvivorDisqualifiedByDeath", null, null, player);
            }
            store.save(active.values());
        }
    }

    public CommandRunner commandRunner() { return commandRunner; }
    public Collection<BloodMoonSession> activeSessions() { return List.copyOf(active.values()); }
    public void abortAll() { active.clear(); store.save(active.values()); }
}
