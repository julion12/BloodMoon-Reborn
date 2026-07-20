package org.spectralmemories.bloodmoon.placeholder.papi;

import org.junit.jupiter.api.Test;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderContext;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderLabels;

import static org.junit.jupiter.api.Assertions.*;

class BloodMoonPlaceholderExpansionTest {
    private static BloodMoonPlaceholderExpansion expansion() {
        PlaceholderLabels labels = new PlaceholderLabels("Active", "Inactive", "None", "Eligible",
                "Disqualified", "Not participating", "Not spawned yet");
        return new BloodMoonPlaceholderExpansion("1.1.0", player -> PlaceholderContext.inactive(labels));
    }

    @Test void identifierAndVersionAreStable() {
        assertAll(() -> assertEquals("bloodmoon", expansion().getIdentifier()),
                () -> assertEquals("1.1.0", expansion().getVersion()));
    }

    @Test void internalExpansionPersistsAcrossPapiReload() {
        assertTrue(expansion().persist());
    }

    @Test void nullOfflinePlayerIsSafe() {
        assertAll(() -> assertEquals("false", expansion().onRequest(null, "active")),
                () -> assertEquals("Not participating", expansion().onRequest(null, "survivor_status")),
                () -> assertNull(expansion().onRequest(null, "unknown")));
    }
}
