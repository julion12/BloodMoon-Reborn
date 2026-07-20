# Configuration

World settings remain in `plugins/BloodMoon/<world>/config.yml`. All 1.0.1 keys remain valid. New rewards are disabled by default.

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

For MythicMobs, `InternalName` must exactly match the mob ID. The Mythic entity's resolved `Display` is used in arrival/death messages; `ZombieBossName` remains exclusive to the vanilla boss. `UseMythicMobsRewards` retains Mythic drops, while `RunBloodMoonRewardCommands` adds the commands above. Enabling both can intentionally duplicate value. `FallbackToVanilla` uses the old boss when MythicMobs or the configured mob is unavailable.

## PlaceholderAPI labels

PlaceholderAPI support has no per-world enable switch: installing the optional plugin registers the internal `bloodmoon` expansion. The formatted values come from `PlaceholderActive`, `PlaceholderInactive`, `PlaceholderNone`, `PlaceholderEligible`, `PlaceholderDisqualified`, `PlaceholderNotParticipating`, and `PlaceholderNoBoss` in the selected locale. English fallback and customized legacy `locales.yml` overrides apply immediately after `/bloodmoon reload`. Boolean placeholders remain unlocalized `true`/`false`. See [PLACEHOLDERAPI.md](PLACEHOLDERAPI.md).
