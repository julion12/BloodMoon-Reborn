package org.spectralmemories.bloodmoon.placeholder.papi;

import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderIntegration;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderStateService;
import org.spectralmemories.bloodmoon.snapshot.PlaceholderSnapshotPublisher;
import org.bukkit.scheduler.BukkitTask;

public final class PlaceholderApiIntegration implements PlaceholderIntegration {
    private final BloodMoonPlaceholderExpansion expansion;
    private final BukkitTask refreshTask;
    private final boolean registered;

    public PlaceholderApiIntegration(Bloodmoon plugin) {
        PlaceholderStateService snapshots = new PlaceholderStateService(
                PlaceholderSnapshotPublisher.initialInactive(plugin));
        PlaceholderSnapshotPublisher publisher = new PlaceholderSnapshotPublisher(plugin, snapshots);
        publisher.refreshOnMainThread();
        expansion = new BloodMoonPlaceholderExpansion(plugin.getDescription().getVersion(), snapshots::snapshot);
        registered = expansion.register();
        refreshTask = registered
                ? plugin.GetScheduler().runTaskTimer(plugin, publisher::refreshOnMainThread, 1L, 5L)
                : null;
    }

    @Override public boolean registered() { return registered; }

    @Override public void close() {
        if (refreshTask != null) refreshTask.cancel();
        if (registered) expansion.unregister();
    }
}
