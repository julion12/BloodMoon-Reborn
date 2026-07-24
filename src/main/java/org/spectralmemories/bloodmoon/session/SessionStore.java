package org.spectralmemories.bloodmoon.session;

import org.bukkit.configuration.file.YamlConfiguration;
import org.spectralmemories.bloodmoon.Bloodmoon;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.logging.Level;

/** Crash marker store. Incomplete sessions are deliberately discarded without rewards. */
public final class SessionStore {
    private final Bloodmoon plugin;
    private final File file;

    public SessionStore(Bloodmoon plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "sessions.yml");
    }

    public void discardIncompleteOnStartup() {
        if (!file.isFile()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getKeys(false).isEmpty()) {
            File discarded = new File(file.getParentFile(), "sessions.discarded-" + Instant.now().toEpochMilli() + ".yml");
            try {
                java.nio.file.Files.move(file.toPath(), discarded.toPath());
                plugin.getLogger().warning("Discarded incomplete Blood Moon session state without rewards: " + discarded.getName());
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not archive incomplete session state; rewards remain disabled for it", exception);
            }
        }
    }

    public void save(Collection<BloodMoonSession> sessions) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (BloodMoonSession session : sessions) {
            String root = session.worldId().toString();
            yaml.set(root + ".session", session.sessionId().toString());
            yaml.set(root + ".world", session.worldName());
            yaml.set(root + ".started", session.startedAt().toString());
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not persist Blood Moon session markers", exception);
        }
    }
}
