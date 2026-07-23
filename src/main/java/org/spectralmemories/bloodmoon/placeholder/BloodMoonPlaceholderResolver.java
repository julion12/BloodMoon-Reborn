package org.spectralmemories.bloodmoon.placeholder;

import java.util.Locale;
import java.util.Set;
import org.spectralmemories.bloodmoon.session.BossSessionState;

/** Pure, allocation-light resolver shared by the PAPI adapter and unit tests. */
public final class BloodMoonPlaceholderResolver {
    private static final Set<String> IDENTIFIERS = Set.of(
            "active", "active_formatted", "world", "time_remaining_seconds", "time_remaining_formatted",
            "boss_alive", "boss_name", "boss_type", "boss_health", "boss_max_health",
            "boss_health_percent", "boss_health_formatted", "boss_state", "boss_state_formatted",
            "participating", "participation_seconds", "participation_formatted",
            "survivor_eligible", "survivor_status", "death_count", "unique_deaths",
            "participants_current", "survivors_current", "total_events", "total_death_events",
            "total_unique_deaths", "total_bosses_spawned", "total_bosses_defeated",
            "last_event_world", "last_event_duration_seconds", "last_event_duration_formatted",
            "last_event_death_count", "last_event_unique_deaths", "last_event_participants",
            "last_event_survivors", "last_boss_name", "last_boss_type", "last_event_ended_at");

    private BloodMoonPlaceholderResolver() { }

    public static Set<String> identifiers() { return IDENTIFIERS; }

    public static String resolve(PlaceholderContext context, String identifier) {
        if (context == null || identifier == null) return null;
        BossPlaceholderState boss = context.boss() == null ? BossPlaceholderState.none() : context.boss();
        PlayerPlaceholderState player = context.player() == null ? PlayerPlaceholderState.none() : context.player();
        SessionPlaceholderState session = context.session() == null
                ? SessionPlaceholderState.none() : context.session();
        BossSessionState bossState = context.bossState() == null ? BossSessionState.NONE : context.bossState();
        HistoricalPlaceholderState history = context.history() == null
                ? HistoricalPlaceholderState.none() : context.history();
        PlaceholderLabels labels = context.labels();
        if (labels == null) return null;
        return switch (identifier.toLowerCase(Locale.ROOT)) {
            case "active" -> Boolean.toString(context.active());
            case "active_formatted" -> context.active() ? labels.active() : labels.inactive();
            case "world" -> context.active() ? safe(context.world(), labels.none()) : labels.none();
            case "time_remaining_seconds" -> Long.toString(Math.max(0, context.remainingSeconds()));
            case "time_remaining_formatted" -> formatDuration(context.active() ? context.remainingSeconds() : 0);
            case "boss_alive" -> Boolean.toString(bossState == BossSessionState.ALIVE && boss.alive());
            case "boss_name" -> hasBossIdentity(bossState) ? safe(boss.name(), labels.noBoss()) : labels.noBoss();
            case "boss_type" -> hasBossIdentity(bossState) ? safe(boss.type(), "NONE") : "NONE";
            case "boss_health" -> number(bossState == BossSessionState.ALIVE && boss.alive() ? boss.health() : 0);
            case "boss_max_health" -> number(bossState == BossSessionState.ALIVE && boss.alive() ? boss.maximumHealth() : 0);
            case "boss_health_percent" -> Integer.toString(bossState == BossSessionState.ALIVE && boss.alive()
                    ? healthPercent(boss.health(), boss.maximumHealth()) : 0);
            case "boss_health_formatted" -> (bossState == BossSessionState.ALIVE && boss.alive()
                    ? healthPercent(boss.health(), boss.maximumHealth()) : 0) + "%";
            case "boss_state" -> bossState.name();
            case "boss_state_formatted" -> formattedBossState(bossState, labels);
            case "participating" -> Boolean.toString(player.participating());
            case "participation_seconds" -> Long.toString(Math.max(0, player.participationSeconds()));
            case "participation_formatted" -> formatDuration(player.participationSeconds());
            case "survivor_eligible" -> Boolean.toString(player.eligible());
            case "survivor_status" -> survivorStatus(player, labels);
            case "death_count" -> Long.toString(Math.max(0, session.deathCount()));
            case "unique_deaths" -> Integer.toString(Math.max(0, session.uniqueDeaths()));
            case "participants_current" -> Integer.toString(Math.max(0, session.currentParticipants()));
            case "survivors_current" -> Integer.toString(Math.max(0, session.currentSurvivors()));
            case "total_events" -> Long.toString(Math.max(0, history.totalEvents()));
            case "total_death_events" -> Long.toString(Math.max(0, history.totalDeathEvents()));
            case "total_unique_deaths" -> Long.toString(Math.max(0, history.totalUniqueDeaths()));
            case "total_bosses_spawned" -> Long.toString(Math.max(0, history.totalBossesSpawned()));
            case "total_bosses_defeated" -> Long.toString(Math.max(0, history.totalBossesDefeated()));
            case "last_event_world" -> history.hasCompletedEvent()
                    ? safe(history.lastEventWorld(), labels.none()) : labels.none();
            case "last_event_duration_seconds" -> Long.toString(Math.max(0, history.lastEventDurationSeconds()));
            case "last_event_duration_formatted" -> formatDuration(history.lastEventDurationSeconds());
            case "last_event_death_count" -> Long.toString(Math.max(0, history.lastEventDeathCount()));
            case "last_event_unique_deaths" -> Integer.toString(Math.max(0, history.lastEventUniqueDeaths()));
            case "last_event_participants" -> Integer.toString(Math.max(0, history.lastEventParticipants()));
            case "last_event_survivors" -> Integer.toString(Math.max(0, history.lastEventSurvivors()));
            case "last_boss_name" -> history.hasCompletedEvent()
                    ? safe(history.lastBossName(), labels.noBoss()) : labels.noBoss();
            case "last_boss_type" -> history.hasCompletedEvent()
                    ? safe(history.lastBossType(), "NONE") : "NONE";
            case "last_event_ended_at" -> history.hasCompletedEvent()
                    ? safe(history.lastEventEndedAt(), labels.none()) : labels.none();
            default -> null;
        };
    }

    public static long remainingSeconds(boolean active, boolean permanent, long worldTime) {
        if (!active || permanent) return 0;
        long normalized = Math.floorMod(worldTime, 24000L);
        return Math.max(0, (long) Math.ceil((24000L - normalized) / 20.0));
    }

    public static String formatDuration(long seconds) {
        long safe = Math.max(0, seconds);
        long hours = safe / 3600;
        long minutes = (safe % 3600) / 60;
        long remaining = safe % 60;
        return hours > 0
                ? String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, remaining)
                : String.format(Locale.ROOT, "%02d:%02d", minutes, remaining);
    }

    public static int healthPercent(double health, double maximum) {
        if (!Double.isFinite(health) || !Double.isFinite(maximum) || maximum <= 0) return 0;
        return (int) Math.round(Math.max(0, Math.min(100, health * 100.0 / maximum)));
    }

    private static String survivorStatus(PlayerPlaceholderState player, PlaceholderLabels labels) {
        if (!player.participating()) return labels.notParticipating();
        if (player.disqualified()) return labels.disqualified();
        return player.eligible() ? labels.eligible() : labels.none();
    }

    private static String formattedBossState(BossSessionState state, PlaceholderLabels labels) {
        return switch (state) {
            case NOT_SPAWNED -> labels.bossStateNotSpawned();
            case ALIVE -> labels.bossStateAlive();
            case DEFEATED -> labels.bossStateDefeated();
            case NONE -> labels.bossStateNone();
        };
    }

    private static boolean hasBossIdentity(BossSessionState state) {
        return state == BossSessionState.ALIVE || state == BossSessionState.DEFEATED;
    }

    private static String number(double value) {
        if (!Double.isFinite(value) || value <= 0) return "0";
        double rounded = Math.round(value * 100.0) / 100.0;
        if (rounded == Math.rint(rounded)) return Long.toString((long) rounded);
        return Double.toString(rounded);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
