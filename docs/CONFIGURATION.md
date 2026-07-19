# Configuration

Configuration remains per world at `plugins/BloodMoon/<world>/config.yml`. All 1.0.1 keys remain valid. The lifecycle lists keep their original top-level names and suffixes:

```yaml
CommandsOnStart:
  - "broadcast Blood Moon in %world%;s"
CommandsOnEnd:
  - "broadcast Blood Moon ended in %world%;s"
```

- `;s`: console once.
- `;f`: console once for each player in the affected world; player placeholders are available.
- `;p`: each affected player executes the command.
- With no suffix, lifecycle commands default to console once. Survivor and boss reward commands default to console per target.
- A leading `/` is removed. Empty entries are ignored and logged. One failure does not stop later commands.

New defaults added during migration:

```yaml
SurvivorRewards:
  Enabled: false
  RequireOnlineAtEnd: true
  IncludeLateJoiners: true
  MinimumParticipationSeconds: 0
  DisqualifyOnDeath: true
  DisqualifyOnWorldLeave: false
  DisqualifyOnDisconnect: false
  RewardOncePerSession: true
  Messages: []
  Commands: []
Boss:
  Mode: VANILLA
  Rewards:
    Enabled: false
    Mode: KILLER
    RequirePlayerKiller: true
    RewardOnce: true
    Commands: []
  MythicMobs:
    Enabled: false
    InternalName: BloodMoonBoss
    UseMythicMobsRewards: true
    RunBloodMoonRewardCommands: false
    FallbackToVanilla: true
```

`Boss.Mode` accepts `VANILLA`, `MYTHICMOBS`, or `NONE`. `VANILLA` preserves `EnableZombieBoss`; a Mythic fallback explicitly spawns the existing vanilla boss. `Boss.Rewards.Mode` currently implements the documented initial `KILLER` mode.

Example rewards:

```yaml
SurvivorRewards:
  Enabled: true
  RequireOnlineAtEnd: true
  MinimumParticipationSeconds: 300
  Commands:
    - "give %player% emerald 3"
    - "xp add %player% 250 points"
Boss:
  Mode: VANILLA
  Rewards:
    Enabled: true
    RequirePlayerKiller: true
    Commands:
      - "give %boss_killer% diamond 3"
```

Commands can integrate economy or permissions plugins without adding hard dependencies.
