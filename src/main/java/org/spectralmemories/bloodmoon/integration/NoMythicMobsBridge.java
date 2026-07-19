package org.spectralmemories.bloodmoon.integration;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class NoMythicMobsBridge implements MythicMobsBridge {
    @Override public boolean available() { return false; }
    @Override public Optional<SpawnedMythicMob> spawn(String name, Location location, boolean useRewards, BiConsumer<LivingEntity, Player> callback) { return Optional.empty(); }
    @Override public void remove(UUID entityId) { }
    @Override public void close() { }
}
