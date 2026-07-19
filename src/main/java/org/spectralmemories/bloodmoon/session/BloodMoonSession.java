package org.spectralmemories.bloodmoon.session;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Lightweight, world-scoped state for one Blood Moon. */
public final class BloodMoonSession {
    private final UUID sessionId;
    private final UUID worldId;
    private final String worldName;
    private final Instant startedAt;
    private Instant endedAt;
    private UUID bossId;
    private final Map<UUID, Participant> participants = new LinkedHashMap<>();

    public BloodMoonSession(UUID worldId, String worldName, Instant startedAt) {
        this(UUID.randomUUID(), worldId, worldName, startedAt);
    }

    public BloodMoonSession(UUID sessionId, UUID worldId, String worldName, Instant startedAt) {
        this.sessionId = sessionId;
        this.worldId = worldId;
        this.worldName = worldName == null ? "" : worldName;
        this.startedAt = startedAt;
    }

    public Participant join(UUID playerId, Instant when) {
        Participant participant = participants.computeIfAbsent(playerId, id -> new Participant(id, when));
        participant.presentSince = when;
        participant.connected = true;
        participant.inWorld = true;
        return participant;
    }

    public void leaveWorld(UUID playerId, Instant when, boolean disqualify) {
        Participant participant = participants.get(playerId);
        if (participant == null) return;
        participant.addPresenceUntil(when);
        participant.inWorld = false;
        if (disqualify) participant.disqualified = true;
    }

    public void disconnect(UUID playerId, Instant when, boolean disqualify) {
        Participant participant = participants.get(playerId);
        if (participant == null) return;
        participant.addPresenceUntil(when);
        participant.connected = false;
        if (disqualify) participant.disqualified = true;
    }

    public void die(UUID playerId, boolean disqualify) {
        Participant participant = participants.get(playerId);
        if (participant != null) {
            participant.died = true;
            if (disqualify) participant.disqualified = true;
        }
    }

    public void end(Instant when) {
        if (endedAt != null) return;
        endedAt = when;
        participants.values().forEach(participant -> participant.addPresenceUntil(when));
    }

    public boolean markRewarded(UUID playerId) {
        Participant participant = participants.get(playerId);
        return participant != null && !participant.rewarded && (participant.rewarded = true);
    }

    public boolean isEligible(UUID playerId, long minimumSeconds, boolean requireOnlineAtEnd,
                              boolean online, boolean inWorld, boolean spectator, boolean npc) {
        Participant participant = participants.get(playerId);
        if (participant == null || participant.rewarded || participant.disqualified || npc || spectator) return false;
        if (requireOnlineAtEnd && !online) return false;
        if (requireOnlineAtEnd && !inWorld) return false;
        Instant until = endedAt == null ? Instant.now() : endedAt;
        return participant.participationSeconds(until) >= Math.max(0, minimumSeconds);
    }

    public UUID sessionId() { return sessionId; }
    public UUID worldId() { return worldId; }
    public String worldName() { return worldName; }
    public Instant startedAt() { return startedAt; }
    public Optional<Instant> endedAt() { return Optional.ofNullable(endedAt); }
    public Optional<UUID> bossId() { return Optional.ofNullable(bossId); }
    public void bossId(UUID bossId) { this.bossId = bossId; }
    public Collection<Participant> participants() { return Collections.unmodifiableCollection(participants.values()); }
    public Optional<Participant> participant(UUID playerId) { return Optional.ofNullable(participants.get(playerId)); }
    public long durationSeconds() {
        return Math.max(0, (endedAt == null ? Instant.now() : endedAt).getEpochSecond() - startedAt.getEpochSecond());
    }
    public long deathCount() { return participants.values().stream().filter(Participant::died).count(); }

    public static final class Participant {
        private final UUID playerId;
        private final Instant firstJoinedAt;
        private Instant presentSince;
        private long accumulatedSeconds;
        private boolean died;
        private boolean rewarded;
        private boolean disqualified;
        private boolean connected = true;
        private boolean inWorld = true;

        private Participant(UUID playerId, Instant joinedAt) {
            this.playerId = playerId;
            this.firstJoinedAt = joinedAt;
            this.presentSince = joinedAt;
        }

        private void addPresenceUntil(Instant when) {
            if (presentSince == null) return;
            accumulatedSeconds += Math.max(0, when.getEpochSecond() - presentSince.getEpochSecond());
            presentSince = null;
        }

        public long participationSeconds(Instant until) {
            return accumulatedSeconds + (presentSince == null ? 0 : Math.max(0, until.getEpochSecond() - presentSince.getEpochSecond()));
        }

        public UUID playerId() { return playerId; }
        public Instant firstJoinedAt() { return firstJoinedAt; }
        public boolean died() { return died; }
        public boolean rewarded() { return rewarded; }
        public boolean disqualified() { return disqualified; }
        public boolean connected() { return connected; }
        public boolean inWorld() { return inWorld; }
    }
}
