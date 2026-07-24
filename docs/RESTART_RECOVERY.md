# Blood Moon restart recovery

## Reproduced 1.1.0 defect

On 2026-07-24 the pre-fix JAR was exercised on Paper 1.21.8 build 60 with
`IncludeLateJoiners: false`. A player was connected before the world naturally crossed from time
11,000 to 12,000 and `PeriodicNightCheck` started the event. The active marker contained one
participant. The server stopped during that night and restarted before the night ended. Before the
player reconnected, a different session marker appeared; PlaceholderAPI then returned
`active=true`, `participants_current=0`, and `participating=false`.

Ignored evidence is retained under `exes/rc-blocker-reproduction-before/evidence/`, including both
session markers, `cache.db`, statistics, timestamps, world time queries, complete console output,
and the assertion result.

## Root cause

`PeriodicNightCheck.Check13()` owns automatic starts. It starts whenever the world is at or after
time 12,000, `daysBeforeBloodMoon == 0`, and `world.getFullTime() >= checkupAfter`.

An automatic start normally advances the in-memory schedule to the next evening and resets the
interval. During shutdown, however, `PeriodicNightCheck.UpdateCacheDatabase()` replaces that state
when the actuator is active: it persists `days=0` and `checkAt=0`. The actuator then aborts and
cleans the active session without rewards. At the next same-night startup, `LoadCache()` restores
those zeroes. The scheduler runs immediately after plugin enable, all three `Check13()` conditions
are true, and a new session is created before Minecraft clients can reconnect.

The defect therefore affects an active event interrupted by shutdown or restart. An ordinary
nighttime startup is eligible only when its persisted countdown also permits it. State is tracked
per world in the legacy SQL cache, but no state currently distinguishes an already-consumed night
from an eligible night. `StartBloodMoon()` itself is idempotent while an actuator is active, so a
single scheduler cannot start the same in-memory event twice; separate worlds can independently
meet the faulty condition. Administrative `/bloodmoon start` currently reaches the same scheduler
by setting its countdown/check time and world time, so the correction must explicitly preserve a
manual override.

Clean shutdown calls `onDisable()`, updates the cache, aborts the actuator, removes boss bars and
boss references, clears the active session marker, and pays nothing. An unexpected process loss
leaves `sessions.yml`; startup archives it without payout. That crash marker contains world/session
metadata but no player UUIDs, and is sufficient to associate recovery with a loaded world.

## Approved 1.1.0 policy and design

An interrupted active Blood Moon is definitively aborted. It is never resumed and pays no survivor
or boss reward. Its world/night cycle is persisted in an internal, versioned
`aborted-nights.yml`, using only world UUID, world name, full-time cycle, cause, and timestamp.
Writes use atomic replacement.

Automatic starts are suppressed only when the loaded world is still in the recorded nighttime
cycle. The scheduler restores the normal post-event interval exactly once and does not log on every
poll. A different cycle makes the marker obsolete and removes it. Markers are independent per
world. Repeated restarts in the same night remain suppressed and do not extend the marker into a
future cycle.

An explicit administrative start remains available. It clears the current suppression and creates
a new empty/normal session; it does not restore participants, boss, duration, eligibility, or
rewards from the aborted event. Administrative stop retains its existing normal-completion policy.
Reload neither clears nor duplicates recovery state.

Boss cleanup during an abort is immediate: vanilla delayed effects, rewards, and permanent-mode
respawn are disabled, Mythic tracking is removed, and boss bars/references are cleared. This is
required because Bukkit no longer accepts newly scheduled tasks once plugin disable has begun.

## Validation

The original defect and the corrected policy were both exercised on Paper 1.21.8 build 60. The
corrected runs covered `IncludeLateJoiners: false` and true, two restarts in one blocked night,
startup with no players, reconnect, explicit manual start, abort without payout, the next eligible
cycle, one normal completion, one reward, one history increment, and marker expiry. The complete
147-test regression initially passed; a live shutdown fallback exposed the delayed-effect issue
above, after which the final regression contains 148 passing tests.
