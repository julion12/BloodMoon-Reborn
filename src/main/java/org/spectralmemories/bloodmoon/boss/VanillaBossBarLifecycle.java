package org.spectralmemories.bloodmoon.boss;

/** Small state machine used to prevent duplicate vanilla boss bars across refreshes. */
public final class VanillaBossBarLifecycle {
    private boolean active;

    public Transition refresh(boolean enabled, String bossType) {
        boolean wanted = enabled && "VANILLA".equalsIgnoreCase(bossType);
        if (wanted && !active) {
            active = true;
            return Transition.CREATE;
        }
        if (wanted) return Transition.KEEP;
        if (active) {
            active = false;
            return Transition.REMOVE;
        }
        return Transition.NONE;
    }

    public Transition close() {
        if (!active) return Transition.NONE;
        active = false;
        return Transition.REMOVE;
    }

    public boolean active() { return active; }

    public enum Transition { CREATE, KEEP, REMOVE, NONE }
}
