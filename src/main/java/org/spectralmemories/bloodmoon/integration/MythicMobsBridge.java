package org.spectralmemories.bloodmoon.integration;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

/** MythicMobs-free boundary used by the core plugin. */
public interface MythicMobsBridge {
    boolean available();
    Optional<LivingEntity> spawn(String internalName, Location location, boolean useMythicRewards,
                                 BiConsumer<LivingEntity, Player> onDeath);
    void remove(UUID entityId);
    void close();
}
