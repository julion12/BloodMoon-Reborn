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

    private BloodMoonSession session() { return new BloodMoonSession(UUID.randomUUID(), "world", START); }
}
