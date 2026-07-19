package org.spectralmemories.bloodmoon.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaBossBarTest {
    @Test void createsOnlyForEnabledVanillaBossAndReloadDoesNotDuplicate() {
        VanillaBossBarLifecycle lifecycle = new VanillaBossBarLifecycle();
        assertEquals(VanillaBossBarLifecycle.Transition.CREATE, lifecycle.refresh(true, "VANILLA"));
        assertEquals(VanillaBossBarLifecycle.Transition.KEEP, lifecycle.refresh(true, "VANILLA"));
        assertTrue(lifecycle.active());
        assertEquals(VanillaBossBarLifecycle.Transition.REMOVE, lifecycle.refresh(false, "VANILLA"));
        assertFalse(lifecycle.active());
        assertEquals(VanillaBossBarLifecycle.Transition.NONE, lifecycle.refresh(true, "MYTHICMOBS"));
    }

    @Test void deathAndEventCleanupRemoveTheBarIdempotently() {
        VanillaBossBarLifecycle lifecycle = new VanillaBossBarLifecycle();
        lifecycle.refresh(true, "VANILLA");
        assertEquals(VanillaBossBarLifecycle.Transition.REMOVE, lifecycle.close());
        assertEquals(VanillaBossBarLifecycle.Transition.NONE, lifecycle.close());
    }

    @Test void healthProgressIsAlwaysClamped() {
        assertEquals(1.0, VanillaBossBarValues.progress(150, 100));
        assertEquals(0.5, VanillaBossBarValues.progress(50, 100));
        assertEquals(0.0, VanillaBossBarValues.progress(-5, 100));
        assertEquals(0.0, VanillaBossBarValues.progress(10, 0));
        assertEquals(0.0, VanillaBossBarValues.progress(Double.NaN, 100));
    }

    @Test void healthPlaceholdersAreReadable() {
        var values = VanillaBossBarValues.placeholders("The Tough One", 25.04, 50);
        assertEquals("The Tough One", values.get("boss_name"));
        assertEquals("25", values.get("boss_health"));
        assertEquals("50", values.get("boss_max_health"));
        assertEquals(50L, values.get("boss_health_percent"));
    }
}
