package org.spectralmemories.bloodmoon.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BossNameResolverTest {
    @Test void vanillaAlwaysUsesLegacyLocaleName() {
        var result = BossNameResolver.resolve("VANILLA", "Wrong", "Wrong", "Wrong", "The Tough One", "Boss");
        assertEquals("The Tough One", result.name());
        assertEquals("VANILLA", result.type());
    }

    @Test void mythicUsesLiveEntityDisplayBeforeConfiguredDisplay() {
        var result = BossNameResolver.resolve("MYTHICMOBS", "&4&lCrimson King", "Configured", "BloodMoonBoss",
                "The Tough One", "Mythic Boss");
        assertEquals("Crimson King", result.name());
        assertEquals("MYTHICMOBS", result.type());
        assertFalse(result.name().contains("The Tough One"));
    }

    @Test void mythicUsesConfiguredDisplayThenInternalName() {
        assertEquals("Crimson Moon King", BossNameResolver.resolve("MYTHICMOBS", null,
                "<dark_red><bold>Crimson Moon King</bold></dark_red>", "BloodMoonBoss", "The Tough One", "Boss").name());
        assertEquals("BloodMoonBoss", BossNameResolver.resolve("MYTHICMOBS", null, null,
                "BloodMoonBoss", "The Tough One", "Boss").name());
    }

    @Test void nullNamesAreSafeAndNoneIsEmpty() {
        assertEquals("Localized Boss", BossNameResolver.resolve("MYTHICMOBS", null, null, null,
                null, "Localized Boss").name());
        assertEquals("", BossNameResolver.resolve("NONE", null, null, null, null, null).name());
    }
}
