package org.spectralmemories.bloodmoon.session;

import org.bukkit.configuration.file.YamlConfiguration;
import org.spectralmemories.bloodmoon.Bloodmoon;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** Crash marker store. Incomplete sessions are deliberately discarded without rewards. */
public final class SessionStore {
    private final Bloodmoon plugin;
    private final File file;

    public SessionStore(Bloodmoon plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "sessions.yml");
    }

    public Map<UUID, Long> discardIncompleteOnStartup() {
        Map<UUID, Long> worlds = new LinkedHashMap<>();
        if (!file.isFile()) return worlds;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            try {
                worlds.put(UUID.fromString(key), yaml.getLong(key + ".night-cycle", -1));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Ignoring invalid world identity in incomplete session marker");
            }
        }
        if (!yaml.getKeys(false).isEmpty()) {
            File discarded = new File(file.getParentFile(), "sessions.discarded-" + Instant.now().toEpochMilli() + ".yml");
            try {
                java.nio.file.Files.move(file.toPath(), discarded.toPath());
                plugin.getLogger().warning("Discarded incomplete Blood Moon session state without rewards: " + discarded.getName());
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not archive incomplete session state; rewards remain disabled for it", exception);
            }
        }
        return worlds;
    }

    public void save(Collection<BloodMoonSession> sessions) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (BloodMoonSession session : sessions) {
            String root = session.worldId().toString();
            yaml.set(root + ".session", session.sessionId().toString());
            yaml.set(root + ".world", session.worldName());
            yaml.set(root + ".started", session.startedAt().toString());
            yaml.set(root + ".night-cycle", session.nightCycle());
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not persist Blood Moon session markers", exception);
        }
    }
}
