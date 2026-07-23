# Survivor rewards

A session is created per affected world when the existing actuator starts a Blood Moon. Initial players and allowed late joiners are tracked by UUID.

A player is eligible when they participated, were not disqualified, are not an NPC, are not a spectator at completion, meet `MinimumParticipationSeconds`, satisfy online/world rules, and have not already been rewarded in that session.

With defaults:

- Death permanently disqualifies the UUID even after respawn, disconnect, reconnect, world change, or name change.
- Disconnect and world leave pause counted presence but do not disqualify.
- The player must be online and in the affected world at the end.
- Late joiners are included.
- Rewards are marked before command execution to prevent re-entry duplicates.

Set `DisqualifyOnWorldLeave` or `DisqualifyOnDisconnect` to make those actions permanent disqualifiers. When `RequireOnlineAtEnd` is false, offline participants can be eligible logically, but player-targeted commands require an online Bukkit player and are therefore not dispatched until an external/offline-capable command design is added.

Natural and manual complete endings evaluate rewards. Plugin disable, crash recovery, or uncertain/incomplete state does not grant rewards. `sessions.yml` is a crash marker; an incomplete file found at startup is archived as `sessions.discarded-<timestamp>.yml` without rewards.

The active `%bloodmoon_survivors_current%` counter is updated immediately when a registered participant first dies or is disqualified. At normal completion, its final value becomes `lastEventSurvivors` in `statistics.yml`; aborted/incomplete sessions never replace the last completed-event snapshot.

Use the disabled-by-default [`examples/SurvivorRewards.yml`](examples/SurvivorRewards.yml) as a reviewed starting point.
