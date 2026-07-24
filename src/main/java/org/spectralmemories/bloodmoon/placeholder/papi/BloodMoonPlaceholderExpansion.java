package org.spectralmemories.bloodmoon.placeholder.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spectralmemories.bloodmoon.placeholder.BloodMoonPlaceholderResolver;

import java.util.function.Function;
import java.util.UUID;

public final class BloodMoonPlaceholderExpansion extends PlaceholderExpansion {
    private final String version;
    private final Function<UUID, org.spectralmemories.bloodmoon.placeholder.PlaceholderContext> snapshots;

    BloodMoonPlaceholderExpansion(String version,
                                  Function<UUID, org.spectralmemories.bloodmoon.placeholder.PlaceholderContext> snapshots) {
        this.version = version;
        this.snapshots = snapshots;
    }

    @Override public @NotNull String getIdentifier() { return "bloodmoon"; }
    @Override public @NotNull String getAuthor() { return "SpectralMemories, JulioN12"; }
    @Override public @NotNull String getVersion() { return version; }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        UUID playerId = offlinePlayer == null ? null : offlinePlayer.getUniqueId();
        return BloodMoonPlaceholderResolver.resolve(snapshots.apply(playerId), params);
    }
}
