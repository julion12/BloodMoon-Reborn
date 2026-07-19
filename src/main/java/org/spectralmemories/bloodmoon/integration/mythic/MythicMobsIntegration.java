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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

/** Loaded only when MythicMobs is present. Uses MythicMobs' public API and death event. */
public final class MythicMobsIntegration implements MythicMobsBridge, Listener {
    private final Map<UUID, BiConsumer<LivingEntity, Player>> deathCallbacks = new HashMap<>();

    public MythicMobsIntegration(Bloodmoon plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override public boolean available() { return true; }

    @Override
    public Optional<LivingEntity> spawn(String internalName, Location location,
                                        BiConsumer<LivingEntity, Player> onDeath) {
        Optional<MythicMob> type = MythicBukkit.inst().getMobManager().getMythicMob(internalName);
        if (type.isEmpty()) return Optional.empty();
        ActiveMob active = type.get().spawn(BukkitAdapter.adapt(location), 1.0);
        Entity entity = active.getEntity().getBukkitEntity();
        if (!(entity instanceof LivingEntity living)) return Optional.empty();
        deathCallbacks.put(living.getUniqueId(), onDeath);
        return Optional.of(living);
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        BiConsumer<LivingEntity, Player> callback = deathCallbacks.remove(entity.getUniqueId());
        if (callback == null || !(entity instanceof LivingEntity living)) return;
        Player killer = event.getKiller() instanceof Player player ? player : living.getKiller();
        callback.accept(living, killer);
    }

    @Override
    public void remove(UUID entityId) {
        deathCallbacks.remove(entityId);
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null) entity.remove();
    }

    @Override public void close() {
        for (UUID entityId : deathCallbacks.keySet().toArray(UUID[]::new)) remove(entityId);
        deathCallbacks.clear();
    }
}
