package org.spectralmemories.bloodmoon.placeholder.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.placeholder.BloodMoonPlaceholderResolver;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderStateService;

import java.util.function.Function;

public final class BloodMoonPlaceholderExpansion extends PlaceholderExpansion {
    private final String version;
    private final Function<Player, org.spectralmemories.bloodmoon.placeholder.PlaceholderContext> snapshots;

    public BloodMoonPlaceholderExpansion(Bloodmoon plugin) {
        PlaceholderStateService state = new PlaceholderStateService(plugin);
        this.version = plugin.getDescription().getVersion();
        this.snapshots = state::snapshot;
    }

    BloodMoonPlaceholderExpansion(String version,
                                  Function<Player, org.spectralmemories.bloodmoon.placeholder.PlaceholderContext> snapshots) {
        this.version = version;
        this.snapshots = snapshots;
    }

    @Override public @NotNull String getIdentifier() { return "bloodmoon"; }
    @Override public @NotNull String getAuthor() { return "SpectralMemories, JulioN12"; }
    @Override public @NotNull String getVersion() { return version; }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        Player player = offlinePlayer != null && offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;
        return BloodMoonPlaceholderResolver.resolve(snapshots.apply(player), params);
    }
}
