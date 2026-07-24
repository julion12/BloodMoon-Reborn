package org.spectralmemories.bloodmoon.session;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BloodMoonSessionTest {
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    @Test void survivorIsEligible() {
        UUID player = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(player, START);
        session.end(START.plusSeconds(60));
        assertTrue(session.isEligible(player, 30, true, true, true, false, false));
    }

    @Test void deathPermanentlyDisqualifiesEvenAfterReconnect() {
        UUID player = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(player, START);
        session.die(player, true);
        session.disconnect(player, START.plusSeconds(10), false);
        session.join(player, START.plusSeconds(20));
        session.end(START.plusSeconds(60));
        assertFalse(session.isEligible(player, 0, true, true, true, false, false));
    }

    @Test void lateJoinerParticipationAndMinimumAreMeasured() {
        UUID player = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(player, START.plusSeconds(50));
        session.end(START.plusSeconds(60));
        assertFalse(session.isEligible(player, 11, true, true, true, false, false));
        assertTrue(session.isEligible(player, 10, true, true, true, false, false));
    }

    @Test void rewardCanOnlyBeMarkedOnce() {
        UUID player = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(player, START);
        assertTrue(session.markRewarded(player));
        assertFalse(session.markRewarded(player));
    }

    @Test void twoWorldSessionsNeverShareParticipants() {
        UUID player = UUID.randomUUID();
        BloodMoonSession first = session();
        BloodMoonSession second = new BloodMoonSession(UUID.randomUUID(), "other", START);
        first.join(player, START);
        assertTrue(first.participant(player).isPresent());
        assertTrue(second.participant(player).isEmpty());
        assertNotEquals(first.sessionId(), second.sessionId());
    }

    @Test void worldLeaveCanDisqualify() {
        UUID player = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(player, START);
        session.leaveWorld(player, START.plusSeconds(5), true);
        session.end(START.plusSeconds(10));
        assertFalse(session.isEligible(player, 0, false, false, false, false, false));
    }

    @Test void firstAndRepeatedDeathsTrackEventsAndUniquePlayersSeparately() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(first, START);
        session.join(second, START);

        session.die(first, true);
        assertAll(() -> assertEquals(1, session.totalDeathEvents()),
                () -> assertEquals(1, session.uniqueDeadPlayers()),
                () -> assertEquals(1, session.currentSurvivors()));

        session.die(first, true);
        assertAll(() -> assertEquals(2, session.totalDeathEvents()),
                () -> assertEquals(1, session.uniqueDeadPlayers()),
                () -> assertEquals(1, session.currentSurvivors()));

        session.die(second, false);
        assertAll(() -> assertEquals(3, session.totalDeathEvents()),
                () -> assertEquals(2, session.uniqueDeadPlayers()),
                () -> assertEquals(0, session.currentSurvivors()));
    }

    @Test void deathsOfUnregisteredPlayersStillCountDuringTheSession() {
        BloodMoonSession session = session();
        session.die(UUID.randomUUID(), true);
        assertAll(() -> assertEquals(1, session.totalDeathEvents()),
                () -> assertEquals(1, session.uniqueDeadPlayers()),
                () -> assertEquals(0, session.currentParticipants()),
                () -> assertEquals(0, session.currentSurvivors()));
    }

    @Test void worldSessionsKeepIndependentStatistics() {
        UUID player = UUID.randomUUID();
        BloodMoonSession first = session();
        BloodMoonSession second = new BloodMoonSession(UUID.randomUUID(), "other", START);
        first.join(player, START);
        second.join(player, START);
        first.die(player, true);

        assertAll(() -> assertEquals(1, first.totalDeathEvents()),
                () -> assertEquals(0, second.totalDeathEvents()),
                () -> assertEquals(0, first.currentSurvivors()),
                () -> assertEquals(1, second.currentSurvivors()));
    }

    @Test void aNewSessionStartsEveryCounterAtZero() {
        UUID world = UUID.randomUUID();
        BloodMoonSession old = new BloodMoonSession(world, "world", START);
        old.join(UUID.randomUUID(), START);
        old.die(UUID.randomUUID(), true);
        BloodMoonSession fresh = new BloodMoonSession(world, "world", START.plusSeconds(100));

        assertAll(() -> assertEquals(0, fresh.totalDeathEvents()),
                () -> assertEquals(0, fresh.uniqueDeadPlayers()),
                () -> assertEquals(0, fresh.currentParticipants()),
                () -> assertEquals(0, fresh.currentSurvivors()));
    }

    @Test void survivorCountHonorsDisqualificationOptionsAndNeverBecomesNegative() {
        UUID death = UUID.randomUUID();
        UUID leave = UUID.randomUUID();
        UUID disconnect = UUID.randomUUID();
        UUID staysEligible = UUID.randomUUID();
        BloodMoonSession session = session();
        session.join(death, START);
        session.join(leave, START);
        session.join(disconnect, START);
        session.join(staysEligible, START);

        session.die(death, false);
        session.die(death, false);
        session.leaveWorld(leave, START.plusSeconds(1), true);
        session.disconnect(disconnect, START.plusSeconds(1), true);
        session.disconnect(staysEligible, START.plusSeconds(1), false);
        assertEquals(1, session.currentSurvivors());

        session.die(staysEligible, true);
        session.die(staysEligible, true);
        assertEquals(0, session.currentSurvivors());
    }

    @Test void bossNarrativeStartsNotSpawnedAndSuccessfulVanillaOrMythicSpawnIsAlive() {
        BloodMoonSession session = session();
        assertEquals(BossSessionState.NOT_SPAWNED, session.bossState());

        UUID vanilla = UUID.randomUUID();
        assertTrue(session.bossSpawned(vanilla, "The Tough One", "VANILLA"));
        assertAll(() -> assertEquals(BossSessionState.ALIVE, session.bossState()),
                () -> assertEquals(vanilla, session.bossId().orElseThrow()),
                () -> assertEquals("The Tough One", session.lastBossName()),
                () -> assertEquals("VANILLA", session.lastBossType()));

        UUID mythic = UUID.randomUUID();
        assertTrue(session.bossSpawned(mythic, "Crimson King", "MYTHICMOBS"));
        assertAll(() -> assertEquals(BossSessionState.ALIVE, session.bossState()),
                () -> assertEquals(mythic, session.bossId().orElseThrow()),
                () -> assertEquals("Crimson King", session.lastBossName()),
                () -> assertEquals("MYTHICMOBS", session.lastBossType()));
    }

    @Test void naturalBossDeathIsIdempotentAndPreservesLastIdentity() {
        BloodMoonSession session = session();
        UUID boss = UUID.randomUUID();
        session.bossSpawned(boss, "Crimson King", "MYTHICMOBS");

        assertTrue(session.bossDefeated(boss));
        assertFalse(session.bossDefeated(boss));
        assertAll(() -> assertEquals(BossSessionState.DEFEATED, session.bossState()),
                () -> assertTrue(session.bossId().isEmpty()),
                () -> assertEquals("Crimson King", session.lastBossName()),
                () -> assertEquals("MYTHICMOBS", session.lastBossType()));
    }

    @Test void newSessionResetsBossNarrative() {
        UUID world = UUID.randomUUID();
        BloodMoonSession first = new BloodMoonSession(world, "world", START);
        UUID boss = UUID.randomUUID();
        first.bossSpawned(boss, "Boss", "VANILLA");
        first.bossDefeated(boss);

        BloodMoonSession second = new BloodMoonSession(world, "world", START.plusSeconds(60));
        assertAll(() -> assertEquals(BossSessionState.NOT_SPAWNED, second.bossState()),
                () -> assertEquals("", second.lastBossName()),
                () -> assertEquals("NONE", second.lastBossType()));
    }

    @Test void crashMarkerCycleRepresentsTheExactWorldNightWithoutPlayerData() {
        BloodMoonSession session = new BloodMoonSession(
                UUID.randomUUID(), "world", START, 42L);
        assertEquals(42L, session.nightCycle());
        assertTrue(session.participants().isEmpty());
    }

    private BloodMoonSession session() { return new BloodMoonSession(UUID.randomUUID(), "world", START); }
}
