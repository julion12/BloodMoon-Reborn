# MythicMobs integration

MythicMobs is optional (`softdepend`). The core plugin does not load Mythic classes unless MythicMobs is enabled. Integration uses the public 5.12.1 API to resolve an internal mob name, spawn it, track the exact entity UUID, and receive `MythicMobDeathEvent`.

Boss identity remains UUID-based. The visible name is display-only and resolves in this order: active entity display, configured Mythic `Display`, BloodMoon `InternalName`, then the localized `MythicBossFallbackName`. Legacy/MiniMessage formatting codes are removed from the inserted name so raw markup is never shown. `ZombieBossName` is never used for a successfully spawned Mythic boss.

Every successful Mythic spawn changes the active session's narrative state to `ALIVE`. A natural Mythic death changes it to `DEFEATED`, retains the resolved display and `MYTHICMOBS` type with zero health, and increments the global defeated-boss total exactly once. A later successful spawn becomes the new narrative subject. Administrative `killbosses` closes the narrative but intentionally does not count as a historical victory.

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
  Display: '&4&lRey de la Luna Carmesí'
  Health: 800
  Damage: 18
  BossBar:
    Enabled: true
    Title: '<mob.name>'
    Range: 40
    Color: RED
    Style: SEGMENTED_10
  Drops:
    - diamond 3 1
    - exp 250 1
```

The example is also stored as [`docs/examples/MythicMobs-BloodMoonBoss.yml`](examples/MythicMobs-BloodMoonBoss.yml) and parsed by the automated test suite. MythicMobs documents drop entries as `<drop> <amount> <chance>` and BossBar styles such as `SEGMENTED_10`; the final `1` above means a 100% chance. See the official [Drops](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/drops/Drops) and [BossBar](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/Mobs/BossBar) documentation.

BloodMoon does not create its vanilla BossBar in `MYTHICMOBS` mode. The `BossBar` block above belongs to MythicMobs, avoiding duplicate bars.

The compatible administrative command is `/bloodmoon spawnzombieboss`. Despite its historical name, it goes through the same configured-mode entry point as automatic spawning. A successful Mythic result is tracked by its exact entity UUID and resolved display name, emits one Mythic arrival message, and never constructs `ZombieIBoss` or a Bukkit BossBar. The permission remains `bloodmoon.spawnzombieboss`.

If MythicMobs is absent, incompatible, or the name is unknown, a warning is logged. `FallbackToVanilla: true` uses the existing `ZombieIBoss`, its `ZombieBossName`, and its vanilla BossBar with one final announcement; otherwise no boss is spawned. `killbosses`, event-end, and disable cleanup remove every exact tracked Mythic UUID without running death rewards.

The bridge-load test does not replace the player-driven spawn/death test. Before release, follow `docs/MANUAL_TEST_CHECKLIST.md` to verify the configured internal name, exact-entity tracking, Mythic-owned rewards, optional BloodMoon commands, and fallback behavior on the actual deployment build.

The separate [`examples/BossRewards.yml`](examples/BossRewards.yml) shows the reward-authority switches, while [`examples/MythicMobs-BloodMoonBoss.yml`](examples/MythicMobs-BloodMoonBoss.yml) remains the copy-ready Mythic mob definition.
