package org.spectralmemories.bloodmoon.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.session.BloodMoonSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** Executes configured commands independently so one failure never stops the remaining list. */
public final class CommandRunner {
    private final Bloodmoon plugin;

    public CommandRunner(Bloodmoon plugin) {
        this.plugin = plugin;
    }

    public void run(String[] configured, CommandExecutionMode defaultMode, World world,
                    BloodMoonSession session, Collection<? extends Player> targets, Map<String, ?> extras) {
        if (!plugin.isEnabled()) return;
        if (!Bukkit.isPrimaryThread()) {
            List<Player> snapshot = new ArrayList<>(targets);
            Bukkit.getScheduler().runTask(plugin, () -> run(configured, defaultMode, world, session, snapshot, extras));
            return;
        }
        for (String raw : configured) {
            CommandParser.parse(raw, defaultMode).ifPresentOrElse(
                    parsed -> execute(parsed, world, session, targets, extras),
                    () -> plugin.getLogger().warning("Ignoring an empty configured command in world " + world.getName()));
        }
    }

    private void execute(ParsedCommand parsed, World world, BloodMoonSession session,
                         Collection<? extends Player> targets, Map<String, ?> extras) {
        if (parsed.mode() == CommandExecutionMode.SERVER_ONCE) {
            dispatchConsole(render(parsed.command(), world, session, null, extras));
            return;
        }
        for (Player player : targets) {
            if (player == null || !player.isOnline()) continue;
            String command = render(parsed.command(), world, session, player, extras);
            try {
                if (parsed.mode() == CommandExecutionMode.PLAYER_FOR_EACH_PLAYER) player.performCommand(command);
                else dispatchConsole(command);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.SEVERE, "Configured command failed for " + player.getUniqueId() + ": " + command, exception);
            }
        }
    }

    private void dispatchConsole(String command) {
        try {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)) {
                plugin.getLogger().warning("Configured command returned false: " + command);
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE, "Configured command failed: " + command, exception);
        }
    }

    public static String render(String template, World world, BloodMoonSession session, Player player, Map<String, ?> extras) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("world", world == null ? "" : world.getName());
        values.put("world_name", world == null ? "" : world.getName());
        values.put("world_uuid", world == null ? "" : world.getUID());
        values.put("player", player == null ? "" : player.getName());
        values.put("player_name", player == null ? "" : player.getName());
        values.put("player_uuid", player == null ? "" : player.getUniqueId());
        if (session != null) {
            values.put("session_uuid", session.sessionId());
            values.put("start_time", session.startedAt());
            values.put("end_time", session.endedAt().map(Instant::toString).orElse(""));
            values.put("duration_seconds", session.durationSeconds());
            values.put("participant_count", session.currentParticipants());
            values.put("death_count", session.deathCount());
            if (player != null) session.participant(player.getUniqueId()).ifPresent(participant -> {
                long seconds = participant.participationSeconds(session.endedAt().orElse(Instant.now()));
                values.put("participation_seconds", seconds);
                values.put("participation_percent", session.durationSeconds() == 0 ? 100 : Math.min(100, seconds * 100 / session.durationSeconds()));
                values.put("died", participant.died());
                values.put("survived", !participant.died() && !participant.disqualified());
            });
        }
        if (extras != null) values.putAll(extras);
        String rendered = PlaceholderEngine.replace(template, values);
        return PlaceholderEngine.replaceLegacy(rendered, values);
    }
}
