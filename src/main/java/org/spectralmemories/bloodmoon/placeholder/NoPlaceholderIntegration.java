package org.spectralmemories.bloodmoon.placeholder;

public final class NoPlaceholderIntegration implements PlaceholderIntegration {
    @Override public boolean registered() { return false; }
    @Override public void close() { }
}
