package org.spectralmemories.bloodmoon.session;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Lightweight, world-scoped state for one Blood Moon. */
public final class BloodMoonSession {
    private final UUID sessionId;
    private final UUID worldId;
    private final String worldName;
    private final Instant startedAt;
    private final long nightCycle;
    private Instant endedAt;
    private UUID bossId;
    private volatile BossSessionState bossState = BossSessionState.NOT_SPAWNED;
    private volatile String lastBossName = "";
    private volatile String lastBossType = "NONE";
    private final Set<UUID> spawnedBosses = new HashSet<>();
    private final Set<UUID> defeatedBosses = new HashSet<>();
    private final Map<UUID, Participant> participants = new LinkedHashMap<>();
    private final Set<UUID> uniqueDeadPlayers = new HashSet<>();
    private volatile long totalDeathEvents;
    private volatile int uniqueDeathCount;
    private volatile int currentParticipants;
    private volatile int currentSurvivors;

    public BloodMoonSession(UUID worldId, String worldName, Instant startedAt) {
        this(UUID.randomUUID(), worldId, worldName, startedAt, -1);
    }

    public BloodMoonSession(UUID sessionId, UUID worldId, String worldName, Instant startedAt) {
        this(sessionId, worldId, worldName, startedAt, -1);
    }

    public BloodMoonSession(UUID worldId, String worldName, Instant startedAt, long nightCycle) {
        this(UUID.randomUUID(), worldId, worldName, startedAt, nightCycle);
    }

    public BloodMoonSession(UUID sessionId, UUID worldId, String worldName, Instant startedAt, long nightCycle) {
        this.sessionId = sessionId;
        this.worldId = worldId;
        this.worldName = worldName == null ? "" : worldName;
        this.startedAt = startedAt;
        this.nightCycle = nightCycle;
    }

    public Participant join(UUID playerId, Instant when) {
        Participant participant = participants.get(playerId);
        if (participant == null) {
            participant = new Participant(playerId, when);
            participants.put(playerId, participant);
            currentParticipants++;
            currentSurvivors++;
        }
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
        if (disqualify) disqualify(participant);
    }

    public void disconnect(UUID playerId, Instant when, boolean disqualify) {
        Participant participant = participants.get(playerId);
        if (participant == null) return;
        participant.addPresenceUntil(when);
        participant.connected = false;
        if (disqualify) disqualify(participant);
    }

    public void die(UUID playerId, boolean disqualify) {
        totalDeathEvents++;
        if (uniqueDeadPlayers.add(playerId)) uniqueDeathCount++;
        Participant participant = participants.get(playerId);
        if (participant != null) {
            if (!participant.died && !participant.disqualified) decrementSurvivors();
            participant.died = true;
            if (disqualify) participant.disqualified = true;
        }
    }

    private void disqualify(Participant participant) {
        if (!participant.disqualified && !participant.died) decrementSurvivors();
        participant.disqualified = true;
    }

    private void decrementSurvivors() {
        currentSurvivors = Math.max(0, currentSurvivors - 1);
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
    public long nightCycle() { return nightCycle; }
    public Optional<Instant> endedAt() { return Optional.ofNullable(endedAt); }
    public Optional<UUID> bossId() { return Optional.ofNullable(bossId); }
    public BossSessionState bossState() { return bossState; }
    public String lastBossName() { return lastBossName; }
    public String lastBossType() { return lastBossType; }
    public boolean bossSpawned(UUID entityId, String name, String type) {
        if (entityId == null) return false;
        boolean firstSpawn = spawnedBosses.add(entityId);
        bossId = entityId;
        lastBossName = name == null ? "" : name;
        lastBossType = type == null || type.isBlank() ? "NONE" : type;
        bossState = BossSessionState.ALIVE;
        return firstSpawn;
    }
    public boolean bossDefeated(UUID entityId) {
        if (entityId == null || !spawnedBosses.contains(entityId) || !defeatedBosses.add(entityId)) return false;
        if (entityId.equals(bossId)) {
            bossId = null;
            bossState = BossSessionState.DEFEATED;
        }
        return true;
    }
    public void bossRemoved(UUID entityId) {
        if (entityId != null && entityId.equals(bossId)) {
            bossId = null;
            bossState = BossSessionState.DEFEATED;
        }
    }
    public void clearBossReference() { bossId = null; }
    public Collection<Participant> participants() { return Collections.unmodifiableCollection(participants.values()); }
    public Optional<Participant> participant(UUID playerId) { return Optional.ofNullable(participants.get(playerId)); }
    public long totalDeathEvents() { return totalDeathEvents; }
    public int uniqueDeadPlayers() { return uniqueDeathCount; }
    public int currentParticipants() { return currentParticipants; }
    public int currentSurvivors() { return currentSurvivors; }
    public long durationSeconds() {
        return Math.max(0, (endedAt == null ? Instant.now() : endedAt).getEpochSecond() - startedAt.getEpochSecond());
    }
    public long deathCount() { return totalDeathEvents; }

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
