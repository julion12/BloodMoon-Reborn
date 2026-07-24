package org.spectralmemories.bloodmoon.lifecycle;

/** Separates an explicit administrator override from ordinary scheduler starts. */
public final class AutomaticStartPolicy {
    private AutomaticStartPolicy() { }

    public static boolean mayStart(boolean schedulerEligible, boolean manualRequest,
                                   boolean sameNightSuppressed) {
        return schedulerEligible && (manualRequest || !sameNightSuppressed);
    }
}
