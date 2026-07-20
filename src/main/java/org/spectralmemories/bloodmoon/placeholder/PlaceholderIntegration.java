package org.spectralmemories.bloodmoon.placeholder;

public interface PlaceholderIntegration extends AutoCloseable {
    boolean registered();
    @Override void close();
}
