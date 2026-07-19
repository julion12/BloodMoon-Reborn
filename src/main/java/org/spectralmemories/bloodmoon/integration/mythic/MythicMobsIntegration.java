package org.spectralmemories.bloodmoon.integration.mythic;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.integration.MythicMobsBridge;
import org.spectralmemories.bloodmoon.integration.SpawnedMythicMob;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

/** Loaded only when MythicMobs is present. Uses MythicMobs' public API and death event. */
public final class MythicMobsIntegration implements MythicMobsBridge, Listener {
    private final Map<UUID, TrackedMob> trackedMobs = new HashMap<>();

    public MythicMobsIntegration(Bloodmoon plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override public boolean available() { return true; }

    @Override
    public Optional<SpawnedMythicMob> spawn(String internalName, Location location, boolean useMythicRewards,
                                            BiConsumer<LivingEntity, Player> onDeath) {
        Optional<MythicMob> type = MythicBukkit.inst().getMobManager().getMythicMob(internalName);
        if (type.isEmpty()) return Optional.empty();
        ActiveMob active = type.get().spawn(BukkitAdapter.adapt(location), 1.0);
        Entity entity = active.getEntity().getBukkitEntity();
        if (!(entity instanceof LivingEntity living)) return Optional.empty();
        trackedMobs.put(living.getUniqueId(), new TrackedMob(onDeath, useMythicRewards));
        String entityDisplayName = living.getCustomName();
        String configuredDisplayName = active.getDisplayName();
        if ((configuredDisplayName == null || configuredDisplayName.isBlank()) && type.get().getDisplayName() != null) {
            configuredDisplayName = type.get().getDisplayName().get(active);
        }
        return Optional.of(new SpawnedMythicMob(living, entityDisplayName, configuredDisplayName,
                type.get().getInternalName()));
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        TrackedMob tracked = trackedMobs.remove(entity.getUniqueId());
        if (tracked == null || !(entity instanceof LivingEntity living)) return;
        if (!tracked.useMythicRewards()) event.setDrops(new java.util.ArrayList<>());
        Player killer = event.getKiller() instanceof Player player ? player : living.getKiller();
        tracked.callback().accept(living, killer);
    }

    @Override
    public void remove(UUID entityId) {
        trackedMobs.remove(entityId);
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null) entity.remove();
    }

    @Override public void close() {
        for (UUID entityId : trackedMobs.keySet().toArray(UUID[]::new)) remove(entityId);
        trackedMobs.clear();
    }

    private record TrackedMob(BiConsumer<LivingEntity, Player> callback, boolean useMythicRewards) { }
}
