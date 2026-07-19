package org.spectralmemories.bloodmoon.integration;

import org.bukkit.entity.LivingEntity;

/** Mythic-free spawn result consumed by the core plugin. */
public record SpawnedMythicMob(LivingEntity entity, String entityDisplayName, String configuredDisplayName,
                               String internalName) { }
