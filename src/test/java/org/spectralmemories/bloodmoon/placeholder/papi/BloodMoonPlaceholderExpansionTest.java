package org.spectralmemories.bloodmoon.placeholder.papi;

import org.junit.jupiter.api.Test;
import org.bukkit.OfflinePlayer;
import org.spectralmemories.bloodmoon.placeholder.*;
import org.spectralmemories.bloodmoon.session.BossSessionState;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodMoonPlaceholderExpansionTest {
    private static BloodMoonPlaceholderExpansion expansion() {
        PlaceholderLabels labels = new PlaceholderLabels("Active", "Inactive", "None", "Eligible",
                "Disqualified", "Not participating", "Not spawned yet",
                "No active event", "Not spawned yet", "Alive", "Defeated");
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
                () -> assertEquals("0", expansion().onRequest(null, "death_count")),
                () -> assertEquals("0", expansion().onRequest(null, "unique_deaths")),
                () -> assertEquals("0", expansion().onRequest(null, "participants_current")),
                () -> assertEquals("0", expansion().onRequest(null, "survivors_current")),
                () -> assertNull(expansion().onRequest(null, "unknown")));
    }

    @Test void nonNullOfflinePlayerNeverDereferencesAnOnlinePlayer() {
        OfflinePlayer offline = (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(), new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> method.getName().equals("isOnline") ? false : null);
        assertAll(() -> assertEquals("0", expansion().onRequest(offline, "death_count")),
                () -> assertEquals("0", expansion().onRequest(offline, "unique_deaths")),
                () -> assertEquals("0", expansion().onRequest(offline, "participants_current")),
                () -> assertEquals("0", expansion().onRequest(offline, "survivors_current")));
    }

    @Test void everyPublicPlaceholderResolvesThroughPlaceholderApiAdapter() {
        PlaceholderLabels labels = new PlaceholderLabels("Active", "Inactive", "None", "Eligible",
                "Disqualified", "Not participating", "Not spawned yet",
                "No active event", "Not spawned yet", "Alive", "Defeated");
        PlaceholderContext context = new PlaceholderContext(true, "world", 60,
                new BossPlaceholderState(true, "Boss", "VANILLA", 20, 40),
                BossSessionState.ALIVE, new PlayerPlaceholderState(true, 30, true, false),
                new SessionPlaceholderState(5, 3, 8, 6), labels);
        BloodMoonPlaceholderExpansion expansion = new BloodMoonPlaceholderExpansion("1.1.0", ignored -> context);
        List<String> identifiers = List.of("active", "active_formatted", "world", "time_remaining_seconds",
                "time_remaining_formatted", "boss_alive", "boss_name", "boss_type", "boss_health",
                "boss_max_health", "boss_health_percent", "boss_health_formatted", "participating",
                "participation_seconds", "participation_formatted", "survivor_eligible", "survivor_status",
                "death_count", "unique_deaths", "participants_current", "survivors_current",
                "boss_state", "boss_state_formatted", "total_events", "total_death_events",
                "total_unique_deaths", "total_bosses_spawned", "total_bosses_defeated",
                "last_event_world", "last_event_duration_seconds", "last_event_duration_formatted",
                "last_event_death_count", "last_event_unique_deaths", "last_event_participants",
                "last_event_survivors", "last_boss_name", "last_boss_type", "last_event_ended_at");

        identifiers.forEach(identifier ->
                assertNotNull(expansion.onRequest(null, identifier), "%bloodmoon_" + identifier + "%"));
    }
}
