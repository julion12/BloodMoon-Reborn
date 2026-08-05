package org.spectralmemories.bloodmoon.config;

import org.bukkit.World;
import org.spectralmemories.bloodmoon.ConfigReader;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** UUID-keyed registry that remains stable when Bukkit replaces a loaded World instance. */
public final class WorldConfigRegistry {
    private final ConcurrentMap<UUID, ConfigReader> readers = new ConcurrentHashMap<>();

    public ConfigReader get(World world) {
        return world == null ? null : readers.get(world.getUID());
    }

    public ConfigReader put(World world, ConfigReader reader) {
        if (world == null || reader == null) return null;
        return readers.put(world.getUID(), reader);
    }

    public ConfigReader remove(World world) {
        return world == null ? null : readers.remove(world.getUID());
    }

    public Collection<ConfigReader> values() {
        return List.copyOf(readers.values());
    }

    public void clear() {
        readers.clear();
    }
}
