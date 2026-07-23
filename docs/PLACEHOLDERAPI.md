# PlaceholderAPI integration

BloodMoon-Reborn contains an internal PlaceholderAPI expansion with identifier `bloodmoon`. PlaceholderAPI is optional; no eCloud expansion is required. If PlaceholderAPI is absent, BloodMoon-Reborn follows the same startup and runtime paths without warnings. TAB, DeluxeMenus, hologram plugins, and other consumers are not BloodMoon-Reborn dependencies.

The expansion registers once during `onEnable`, returns `persist() = true` so `/papi reload` retains it, and is not re-registered by `/bloodmoon reload`. Values are read from the current world session, its constant-time counters, the tracked boss UUID/entity, and the existing participant record. Requests do not read files, parse YAML, iterate participants, scan world entities, schedule work, or modify state.

## Public placeholders

Examples assume an active event in `world`, a vanilla boss named `The Tough One` at 75/100 health, and an eligible player with 125 participation seconds.

| Placeholder | Type | Example | Without event/boss | Context | Recommended frequency |
| --- | --- | --- | --- | --- | --- |
| `%bloodmoon_active%` | Boolean | `true` | `false` | Player world | Up to 20/s |
| `%bloodmoon_active_formatted%` | Localized text | `Active` | `Inactive` | Player world | Up to 20/s |
| `%bloodmoon_world%` | Text | `world` | Localized `PlaceholderNone` | Player world | Up to 20/s |
| `%bloodmoon_time_remaining_seconds%` | Integer | `600` | `0` | Player world | Up to 20/s |
| `%bloodmoon_time_remaining_formatted%` | Time | `10:00` | `00:00` | Player world | Up to 20/s |
| `%bloodmoon_boss_alive%` | Boolean | `true` | `false` | Tracked boss in player world | Up to 20/s |
| `%bloodmoon_boss_name%` | Text | `The Tough One` | Localized `PlaceholderNoBoss` | Tracked boss in player world | Up to 20/s |
| `%bloodmoon_boss_type%` | Enum | `VANILLA` | `NONE` | Tracked boss in player world | Up to 20/s |
| `%bloodmoon_boss_health%` | Number | `75` | `0` | Live Bukkit entity | Up to 20/s |
| `%bloodmoon_boss_max_health%` | Number | `100` | `0` | Live Bukkit entity | Up to 20/s |
| `%bloodmoon_boss_health_percent%` | Integer | `75` | `0` | Live Bukkit entity | Up to 20/s |
| `%bloodmoon_boss_health_formatted%` | Text | `75%` | `0%` | Live Bukkit entity | Up to 20/s |
| `%bloodmoon_participating%` | Boolean | `true` | `false` | Player/session | Up to 20/s |
| `%bloodmoon_participation_seconds%` | Integer | `125` | `0` | Player/session | Up to 20/s |
| `%bloodmoon_participation_formatted%` | Time | `02:05` | `00:00` | Player/session | Up to 20/s |
| `%bloodmoon_survivor_eligible%` | Boolean | `true` | `false` | Player/session/policy | Up to 20/s |
| `%bloodmoon_survivor_status%` | Localized text | `Eligible` | `Not participating` | Player/session/policy | Up to 20/s |
| `%bloodmoon_death_count%` | Integer | `7` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_unique_deaths%` | Integer | `4` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_participants_current%` | Integer | `12` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_survivors_current%` | Integer | `8` | `0` | Active session in player world | Up to 20/s |

Boolean placeholders deliberately return lowercase `true` or `false` for conditions. Time uses `MM:SS`, switching to `HH:MM:SS` beyond one hour, and never becomes negative. Health percentage is rounded and clamped to 0–100. Vanilla health includes its real absorption health; Mythic health comes from the tracked Bukkit entity and its current maximum-health attribute, never from either BossBar.

Session counters start at zero for every new Blood Moon and exist only in memory while that world's session is active. `death_count` counts every player death event during the Blood Moon; a repeated death increments it again. `unique_deaths` counts distinct player UUIDs. `participants_current` counts registered participants, including initial players and permitted late joiners. `survivors_current` counts registered participants that have neither died nor been disqualified and is clamped at zero. Deaths by players excluded through `IncludeLateJoiners: false` still count as event deaths, but do not add a participant or remove a survivor. Disconnects and world exits update survivor state only when their existing disqualification option is enabled.

## Validation commands

```text
/papi list
/papi parse me %bloodmoon_active%
/papi parse me %bloodmoon_active_formatted%
/papi parse me %bloodmoon_world%
/papi parse me %bloodmoon_time_remaining_formatted%
/papi parse me %bloodmoon_boss_alive%
/papi parse me %bloodmoon_boss_name%
/papi parse me %bloodmoon_boss_type%
/papi parse me %bloodmoon_boss_health_formatted%
/papi parse me %bloodmoon_survivor_status%
/papi parse me %bloodmoon_death_count%
/papi parse me %bloodmoon_unique_deaths%
/papi parse me %bloodmoon_participants_current%
/papi parse me %bloodmoon_survivors_current%
```

An offline or missing player receives safe inactive/no-boss/not-participating values. Unknown `%bloodmoon_*%` identifiers return `null` to PlaceholderAPI.

## TAB scoreboard example

Add these entries inside TAB's existing `scoreboard.scoreboards` map. TAB evaluates designs from top to bottom, so the conditional Blood Moon design must precede the normal fallback. The current TAB syntax uses `display-condition`.

```yaml
scoreboard:
  enabled: true
  scoreboards:
    blood-moon:
      display-condition: "%bloodmoon_active%=true"
      title: "&4&lBlood Moon"
      lines:
        - "&7World: &f%bloodmoon_world%"
        - "&7Remaining: &f%bloodmoon_time_remaining_formatted%"
        - ""
        - "&cBoss: &f%bloodmoon_boss_name%"
        - "&cType: &f%bloodmoon_boss_type%"
        - "&cHealth: &f%bloodmoon_boss_health%&7/&f%bloodmoon_boss_max_health%"
        - "&cPercent: &f%bloodmoon_boss_health_formatted%"
        - ""
        - "&7Deaths: &f%bloodmoon_death_count% &8(&f%bloodmoon_unique_deaths% unique&8)"
        - "&7Players: &f%bloodmoon_survivors_current%&7/&f%bloodmoon_participants_current% &7alive"
        - ""
        - "&7Survivor: &f%bloodmoon_survivor_status%"
        - "&7Participation: &f%bloodmoon_participation_formatted%"
    normal:
      title: "&6My Server"
      lines:
        - "&7World: &f%world%"
        - "&7Online: &f%online%"
        - ""
        - "&aNo Blood Moon"
```

TAB and PlaceholderAPI must be installed separately by the administrator. BloodMoon-Reborn neither downloads nor controls TAB. See the official [PlaceholderAPI internal-expansion guide](https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/) and [TAB scoreboard guide](https://github.com/NEZNAMY/TAB/wiki/Feature-guide%3A-Scoreboard).
