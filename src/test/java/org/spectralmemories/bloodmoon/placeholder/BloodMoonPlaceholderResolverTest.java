package org.spectralmemories.bloodmoon.placeholder;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodMoonPlaceholderResolverTest {
    private static final PlaceholderLabels EN = new PlaceholderLabels("Active", "Inactive", "None", "Eligible",
            "Disqualified", "Not participating", "Not spawned yet");

    @Test void activeAndWorldAreContextual() {
        assertAll(() -> assertEquals("true", resolve(active(), "active")),
                () -> assertEquals("Active", resolve(active(), "active_formatted")),
                () -> assertEquals("world", resolve(active(), "world")),
                () -> assertEquals("false", resolve(inactive(), "active")),
                () -> assertEquals("Inactive", resolve(inactive(), "active_formatted")),
                () -> assertEquals("None", resolve(inactive(), "world")));
    }

    @Test void timeNeverGoesNegativeAndFormatsHours() {
        assertAll(() -> assertEquals(0, BloodMoonPlaceholderResolver.remainingSeconds(false, false, 13000)),
                () -> assertEquals(0, BloodMoonPlaceholderResolver.remainingSeconds(true, true, 13000)),
                () -> assertEquals(600, BloodMoonPlaceholderResolver.remainingSeconds(true, false, 12000)),
                () -> assertEquals("00:00", BloodMoonPlaceholderResolver.formatDuration(-10)),
                () -> assertEquals("01:01", BloodMoonPlaceholderResolver.formatDuration(61)),
                () -> assertEquals("01:01:01", BloodMoonPlaceholderResolver.formatDuration(3661)));
    }

    @Test void vanillaBossUsesConfiguredNameTypeAndRealHealthValues() {
        PlaceholderContext context = withBoss(new BossPlaceholderState(true, "The Tough One", "VANILLA", 75, 100));
        assertAll(() -> assertEquals("true", resolve(context, "boss_alive")),
                () -> assertEquals("The Tough One", resolve(context, "boss_name")),
                () -> assertEquals("VANILLA", resolve(context, "boss_type")),
                () -> assertEquals("75", resolve(context, "boss_health")),
                () -> assertEquals("100", resolve(context, "boss_max_health")),
                () -> assertEquals("75", resolve(context, "boss_health_percent")),
                () -> assertEquals("75%", resolve(context, "boss_health_formatted")));
    }

    @Test void mythicBossUsesResolvedDisplayAndType() {
        PlaceholderContext context = withBoss(new BossPlaceholderState(true, "Crimson King", "MYTHICMOBS", 333.33, 800));
        assertAll(() -> assertEquals("Crimson King", resolve(context, "boss_name")),
                () -> assertEquals("MYTHICMOBS", resolve(context, "boss_type")),
                () -> assertEquals("333.33", resolve(context, "boss_health")),
                () -> assertEquals("42", resolve(context, "boss_health_percent")));
    }

    @Test void bossPercentIsClampedAndRejectsInvalidNumbers() {
        assertAll(() -> assertEquals(100, BloodMoonPlaceholderResolver.healthPercent(500, 100)),
                () -> assertEquals(0, BloodMoonPlaceholderResolver.healthPercent(-5, 100)),
                () -> assertEquals(0, BloodMoonPlaceholderResolver.healthPercent(5, 0)),
                () -> assertEquals(0, BloodMoonPlaceholderResolver.healthPercent(Double.NaN, 100)));
    }

    @Test void absentBossReturnsSafeLocalizedValues() {
        assertAll(() -> assertEquals("false", resolve(inactive(), "boss_alive")),
                () -> assertEquals("Not spawned yet", resolve(inactive(), "boss_name")),
                () -> assertEquals("NONE", resolve(inactive(), "boss_type")),
                () -> assertEquals("0", resolve(inactive(), "boss_health")),
                () -> assertEquals("0", resolve(inactive(), "boss_max_health")),
                () -> assertEquals("0", resolve(inactive(), "boss_health_percent")),
                () -> assertEquals("0%", resolve(inactive(), "boss_health_formatted")));
    }

    @Test void participationAndEligibilityAreContextual() {
        PlaceholderContext eligible = withPlayer(new PlayerPlaceholderState(true, 125, true, false));
        PlaceholderContext disqualified = withPlayer(new PlayerPlaceholderState(true, 10, false, true));
        assertAll(() -> assertEquals("true", resolve(eligible, "participating")),
                () -> assertEquals("125", resolve(eligible, "participation_seconds")),
                () -> assertEquals("02:05", resolve(eligible, "participation_formatted")),
                () -> assertEquals("true", resolve(eligible, "survivor_eligible")),
                () -> assertEquals("Eligible", resolve(eligible, "survivor_status")),
                () -> assertEquals("Disqualified", resolve(disqualified, "survivor_status")),
                () -> assertEquals("Not participating", resolve(inactive(), "survivor_status")));
    }

    @Test void unknownIdentifierReturnsNull() {
        assertNull(resolve(active(), "does_not_exist"));
    }

    @Test void everyDocumentedIdentifierResponds() {
        List<String> identifiers = List.of("active", "active_formatted", "world", "time_remaining_seconds",
                "time_remaining_formatted", "boss_alive", "boss_name", "boss_type", "boss_health",
                "boss_max_health", "boss_health_percent", "boss_health_formatted", "participating",
                "participation_seconds", "participation_formatted", "survivor_eligible", "survivor_status");
        identifiers.forEach(identifier -> assertNotNull(resolve(active(), identifier), identifier));
    }

    private static PlaceholderContext active() {
        return new PlaceholderContext(true, "world", 90,
                new BossPlaceholderState(true, "Boss", "VANILLA", 50, 100),
                new PlayerPlaceholderState(true, 30, true, false), EN);
    }

    private static PlaceholderContext inactive() { return PlaceholderContext.inactive(EN); }
    private static PlaceholderContext withBoss(BossPlaceholderState boss) {
        return new PlaceholderContext(true, "world", 90, boss, PlayerPlaceholderState.none(), EN);
    }
    private static PlaceholderContext withPlayer(PlayerPlaceholderState player) {
        return new PlaceholderContext(true, "world", 90, BossPlaceholderState.none(), player, EN);
    }
    private static String resolve(PlaceholderContext context, String identifier) {
        return BloodMoonPlaceholderResolver.resolve(context, identifier);
    }
}
