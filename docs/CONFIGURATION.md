# Configuration

World settings remain in `plugins/BloodMoon/<world>/config.yml`. All 1.0.1 keys remain valid. New rewards are disabled by default.

## Bundled administrator files

At startup the plugin creates only missing files under `plugins/BloodMoon/`: `README.txt`, all
seven files in `EXAMPLES/`, and the administrator guides in `docs/`. Resources come from
`distribution/` inside the installed JAR; runtime never depends on the source repository.
Existing files and timestamps are left unchanged. A future upgrade therefore adds new guide files
but preserves every local edit. `docs/VERSION.txt` identifies the bundled documentation generation.
An individual copy failure logs a warning and does not prevent the plugin from enabling.

## Language

New installations receive `locales/en.yml` and `locales/es.yml`. Select a language in the compatibility file `plugins/BloodMoon/locales.yml`:

```yaml
LocalesVersion: 1.1.0
Language: en # Use es for Spanish.
UseBundledLocales: true
```

The bundled [`en.yml`](../src/main/resources/locales/en.yml) and [`es.yml`](../src/main/resources/locales/es.yml) files are complete English and Spanish examples. They are copied once and never overwritten. Keys placed directly in an old `locales.yml` remain higher-priority custom overrides. Missing selected-language keys fall back to English.

## Lifecycle commands

```yaml
CommandsOnStart:
  - "say [BloodMoon] The Blood Moon started in %world%;s"
  - "effect give %player% minecraft:night_vision 10 0 true;f"
  - "playsound minecraft:entity.wither.spawn master %player%;f"
  - "me hears the Blood Moon rising;p"
CommandsOnEnd:
  - "say [BloodMoon] The Blood Moon ended in %world%;s"
  - "weather clear;s"
  - "effect clear $p minecraft:night_vision;f"
```

- `;s`: console once. It has world/session placeholders but no per-player `%player%` value.
- `;f`: console once for every target player. `$p` and player placeholders are available.
- `;p`: every target player executes the command. `$p` and player placeholders are available.
- `$w` and `$p` are the legacy world/player placeholders. `%world%` and `%player%` are their modern equivalents.
- With no suffix, lifecycle commands default to console once. Survivor and boss rewards default to console per target.
- OnStart and OnEnd `;s` commands run once per completed session, not once per player. A crash-recovered incomplete session does not run OnEnd.
- A leading `/` is removed. Empty entries are ignored and one failed command does not stop later commands.

Commands can invoke effects, sounds, economy, permissions, or other plugins. Those plugins remain optional; for example, `eco` works only when a compatible economy command is installed.

Ready-to-merge examples: [`CommandsOnStart.yml`](examples/CommandsOnStart.yml) and [`CommandsOnEnd.yml`](examples/CommandsOnEnd.yml).

## Survivor rewards

```yaml
SurvivorRewards:
  Enabled: false
  RequireOnlineAtEnd: true
  IncludeLateJoiners: true
  MinimumParticipationSeconds: 60
  DisqualifyOnDeath: true
  DisqualifyOnWorldLeave: false
  DisqualifyOnDisconnect: false
  RewardOncePerSession: true
  Messages:
    - "&aYou survived the Blood Moon."
  Commands:
    - "give %player% minecraft:diamond 1;f"
    - "experience add %player% 100 points;f"
    - "eco give %player% 500;f"
```

These commands run from the console for each eligible player because they use `;f`. Players who die are disqualified when `DisqualifyOnDeath` is true. UUID tracking prevents reconnects, world changes, reloads, or name changes from duplicating a reward.

Ready-to-merge example: [`SurvivorRewards.yml`](examples/SurvivorRewards.yml).

## Boss selection, vanilla bar, and rewards

```yaml
Boss:
  Mode: VANILLA # VANILLA, MYTHICMOBS, or NONE
  VanillaBossBar:
    Enabled: true
    Title: "%boss_name% &c%boss_health%&7/&c%boss_max_health%"
    Color: RED
    Style: SEGMENTED_10
    Audience: NEARBY # NEARBY, WORLD, or ALL
    ViewDistance: 64
    ShowHealthNumbers: true
  Rewards:
    Enabled: false
    Mode: KILLER
    RequirePlayerKiller: true
    RewardOnce: true
    Commands:
      - "give %boss_killer% minecraft:diamond 3;s"
      - "experience add %boss_killer% 250 points;s"
      - "say %boss_killer% defeated %boss_name%;s"
  MythicMobs:
    Enabled: false
    InternalName: BloodMoonBoss
    UseMythicMobsRewards: true
    RunBloodMoonRewardCommands: false
    FallbackToVanilla: true
```

The vanilla bar is created only for the built-in zombie boss. It follows effective health, refreshes its audience every 10 ticks, and is removed on death, administrative removal, event end, world unload, plugin disable, or session cleanup. Reload updates the existing bar and never creates a second one. `ShowHealthNumbers: false` reduces the title to `%boss_name%`.

Both automatic spawning and `/bloodmoon spawnzombieboss` use this same `Boss` selection. The command name is historical: in `MYTHICMOBS` mode it creates only `InternalName`, announces the resolved Mythic display, and never creates the vanilla BossBar. In `NONE` mode the localized `BossDisabled` message is returned. If a Mythic spawn fails, `FallbackToVanilla` determines whether the result is one fully initialized vanilla boss or a localized `BossSpawnFailed` response.

Audience modes:

- `NEARBY`: same-world players within `ViewDistance` blocks.
- `WORLD`: every player in the boss world.
- `ALL`: every online player.

Vanilla reward commands are additional to the boss's legacy item/experience drops. Rewards remain off until `Boss.Rewards.Enabled` is explicitly set to `true`.

Ready-to-merge example: [`BossRewards.yml`](examples/BossRewards.yml).

For MythicMobs, `InternalName` must exactly match the mob ID. The Mythic entity's resolved `Display` is used in arrival/death messages; `ZombieBossName` remains exclusive to the vanilla boss. `UseMythicMobsRewards` retains Mythic drops, while `RunBloodMoonRewardCommands` adds the commands above. Enabling both can intentionally duplicate value. `FallbackToVanilla` uses the old boss when MythicMobs or the configured mob is unavailable.

## PlaceholderAPI labels

PlaceholderAPI support has no per-world enable switch: installing the optional plugin registers the internal `bloodmoon` expansion. The formatted values come from `PlaceholderActive`, `PlaceholderInactive`, `PlaceholderNone`, `PlaceholderEligible`, `PlaceholderDisqualified`, `PlaceholderNotParticipating`, `PlaceholderNoBoss`, the four `PlaceholderBossState*` keys, and `PlaceholderBossDisplayNotSpawned`, `PlaceholderBossDisplayName`, `PlaceholderBossDisplayDefeatedName`, `PlaceholderBossDisplayType`, `PlaceholderBossDisplayHealth`, and `PlaceholderBossDisplayDefeated`. English fallback and customized legacy `locales.yml` overrides apply immediately after `/bloodmoon reload`. Boolean and technical state placeholders remain unlocalized. See [PLACEHOLDERAPI.md](PLACEHOLDERAPI.md).

## Historical statistics

`plugins/BloodMoon/statistics.yml` is a server-wide, versioned runtime data file; it is not part of any world's `config.yml`. It is loaded once during enable and written only on boss lifecycle changes, completed events, or pending disable flushes. Writes use `statistics.yml.tmp` followed by atomic replacement where the filesystem supports it. Invalid data is copied to `statistics.corrupt-<timestamp>.yml`, a warning is logged, and safe defaults are installed without preventing startup.

Only aggregate counts and the last completed event are stored. No player UUID/name, IP, coordinate, inventory, or detailed death record is retained. `/bloodmoon reload` does not recreate this service and therefore neither loses nor duplicates the snapshot.
