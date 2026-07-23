package org.spectralmemories.bloodmoon.session;

/** Narrative state of the most recently spawned boss in one Blood Moon session. */
public enum BossSessionState {
    NONE,
    NOT_SPAWNED,
    ALIVE,
    DEFEATED
}
