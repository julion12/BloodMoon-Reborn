# MythicMobs integration

MythicMobs is optional (`softdepend`). The core plugin does not load Mythic classes unless MythicMobs is enabled. Integration uses the public 5.12.1 API to resolve an internal mob name, spawn it, track the exact entity UUID, and receive `MythicMobDeathEvent`.

Live validation on 2026-07-19 confirmed that MythicMobs 5.12.1 and the BloodMoon bridge enable on Paper 1.21.8. The same MythicMobs build fails during its own NMS/server-version initialization on Paper 26.2 build 62, before BloodMoon can activate the bridge. BloodMoon core continues safely without it. Treat 26.2 + MythicMobs 5.12.1 as unsupported and test a future 26.2-compatible MythicMobs build before deployment.

```yaml
Boss:
  Mode: MYTHICMOBS
  MythicMobs:
    Enabled: true
    InternalName: BloodMoonBoss
    UseMythicMobsRewards: true
    RunBloodMoonRewardCommands: false
    FallbackToVanilla: true
```

With the defaults above, MythicMobs owns drops, experience, skills, commands, money, and MythicItems. BloodMoon-Reborn excludes the tracked Mythic boss from ordinary enhanced-mob drops and does not run boss reward commands, preventing duplication.

Setting `UseMythicMobsRewards: false` suppresses the Mythic death-event item drop list. Mythic `~onDeath` skills are part of the mob definition and may still execute; remove reward commands from those skills when opting out.

Set `RunBloodMoonRewardCommands: true` only when additional commands under `Boss.Rewards.Commands` are desired. If the Mythic mob also has drops or death commands, enabling both can intentionally duplicate value.

Example MythicMobs file (place manually in MythicMobs; BloodMoon-Reborn never overwrites it):

```yaml
BloodMoonBoss:
  Type: ZOMBIE
  Display: '&4Blood Moon Herald'
  Health: 500
  Damage: 12
  Options:
    PreventOtherDrops: true
  Drops:
    - diamond 1-3 1
  Skills:
    - message{m="&cThe herald has fallen!"} @PlayersInWorld ~onDeath
```

If MythicMobs is absent, incompatible, or the name is unknown, a warning is logged. `FallbackToVanilla: true` uses the existing `ZombieIBoss`; otherwise no boss is spawned. Despawn/event-end/disable cleanup removes the exact tracked entity.

The bridge-load test does not replace the player-driven spawn/death test. Before release, follow `docs/MANUAL_TEST_CHECKLIST.md` to verify the configured internal name, exact-entity tracking, Mythic-owned rewards, optional BloodMoon commands, and fallback behavior on the actual deployment build.
