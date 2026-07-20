package org.spectralmemories.bloodmoon.placeholder.papi;

import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderIntegration;

public final class PlaceholderApiIntegration implements PlaceholderIntegration {
    private final BloodMoonPlaceholderExpansion expansion;
    private final boolean registered;

    public PlaceholderApiIntegration(Bloodmoon plugin) {
        expansion = new BloodMoonPlaceholderExpansion(plugin);
        registered = expansion.register();
    }

    @Override public boolean registered() { return registered; }

    @Override public void close() {
        if (registered) expansion.unregister();
    }
}
