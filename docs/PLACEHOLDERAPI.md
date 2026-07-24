# PlaceholderAPI integration

BloodMoon-Reborn contains an internal PlaceholderAPI expansion with identifier `bloodmoon`. PlaceholderAPI is optional; no eCloud expansion is required. If PlaceholderAPI is absent, BloodMoon-Reborn follows the same startup and runtime paths without warnings. TAB, DeluxeMenus, hologram plugins, and other consumers are not BloodMoon-Reborn dependencies.

The expansion registers once during `onEnable`, returns `persist() = true` so `/papi reload` retains
it, and is not re-registered by `/bloodmoon reload`. All 41 values are resolved from one immutable
context previously published for the player's UUID. Requests are safe from asynchronous consumers
such as TAB: they do not access Bukkit players, worlds, chunks, entities, BossBars, MythicMobs,
configuration, files, schedulers, or session collections.

BloodMoon publishes complete contexts atomically from one synchronous refresh task every 5 ticks.
Boss spawn, death, removal, and event boundaries also publish their narrative transition on the
main thread. Damage and healing schedule a next-tick health capture because Bukkit damage events
run before final health is applied; the periodic refresh additionally captures absorption and
maximum-health changes. No task is created per player, placeholder, or request.

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
| `%bloodmoon_boss_state%` | Enum | `ALIVE` | `NONE` | Last boss in active session | Up to 20/s |
| `%bloodmoon_boss_state_formatted%` | Localized text | `Alive` | `No active event` | Last boss in active session | Up to 20/s |
| `%bloodmoon_boss_display_line_1%` | Legacy-color text | `&7Boss: &cThe Tough One` | Empty | Boss narrative snapshot | Up to 20/s |
| `%bloodmoon_boss_display_line_2%` | Legacy-color text | `&7Type: &fVANILLA` | Empty | Boss narrative snapshot | Up to 20/s |
| `%bloodmoon_boss_display_line_3%` | Legacy-color text | `&7Health: &c75%` | Empty | Boss narrative snapshot | Up to 20/s |
| `%bloodmoon_participating%` | Boolean | `true` | `false` | Player/session | Up to 20/s |
| `%bloodmoon_participation_seconds%` | Integer | `125` | `0` | Player/session | Up to 20/s |
| `%bloodmoon_participation_formatted%` | Time | `02:05` | `00:00` | Player/session | Up to 20/s |
| `%bloodmoon_survivor_eligible%` | Boolean | `true` | `false` | Player/session/policy | Up to 20/s |
| `%bloodmoon_survivor_status%` | Localized text | `Eligible` | `Not participating` | Player/session/policy | Up to 20/s |
| `%bloodmoon_death_count%` | Integer | `7` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_unique_deaths%` | Integer | `4` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_participants_current%` | Integer | `12` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_survivors_current%` | Integer | `8` | `0` | Active session in player world | Up to 20/s |
| `%bloodmoon_total_events%` | Integer | `9` | `0` | Server history | Up to 20/s |
| `%bloodmoon_total_death_events%` | Integer | `30` | `0` | Server history | Up to 20/s |
| `%bloodmoon_total_unique_deaths%` | Integer | `18` | `0` | Server history | Up to 20/s |
| `%bloodmoon_total_bosses_spawned%` | Integer | `7` | `0` | Server history | Up to 20/s |
| `%bloodmoon_total_bosses_defeated%` | Integer | `5` | `0` | Server history | Up to 20/s |
| `%bloodmoon_last_event_world%` | Text | `world` | Localized `PlaceholderNone` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_duration_seconds%` | Integer | `600` | `0` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_duration_formatted%` | Time | `10:00` | `00:00` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_death_count%` | Integer | `7` | `0` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_unique_deaths%` | Integer | `4` | `0` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_participants%` | Integer | `12` | `0` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_survivors%` | Integer | `8` | `0` | Last completed event | Up to 20/s |
| `%bloodmoon_last_boss_name%` | Text | `The Tough One` | Localized `PlaceholderNoBoss` | Last completed event | Up to 20/s |
| `%bloodmoon_last_boss_type%` | Enum | `VANILLA` | `NONE` | Last completed event | Up to 20/s |
| `%bloodmoon_last_event_ended_at%` | ISO-8601 UTC | `2026-07-23T12:00:00Z` | Localized `PlaceholderNone` | Last completed event | Up to 20/s |

Boolean placeholders deliberately return lowercase `true` or `false` for conditions. Time uses `MM:SS`, switching to `HH:MM:SS` beyond one hour, and never becomes negative. Health percentage is rounded and clamped to 0–100. Vanilla health includes its real absorption health; Mythic health comes from the tracked Bukkit entity and its current maximum-health attribute, never from either BossBar.

Session counters start at zero for every new Blood Moon and exist only in memory while that world's session is active. `death_count` counts every player death event during the Blood Moon; a repeated death increments it again. `unique_deaths` counts distinct player UUIDs. `participants_current` counts registered participants, including initial players and permitted late joiners. `survivors_current` counts registered participants that have neither died nor been disqualified and is clamped at zero. Deaths by players excluded through `IncludeLateJoiners: false` still count as event deaths, but do not add a participant or remove a survivor. Disconnects and world exits update survivor state only when their existing disqualification option is enabled.

`boss_alive` answers only whether the last main-thread snapshot marked the current tracked entity
alive. `boss_state` tells the story of the most recently spawned boss in the session:
`NOT_SPAWNED`, `ALIVE`, or `DEFEATED`; outside an event it is `NONE`. A successful later spawn
replaces the narrative subject. `DEFEATED` preserves the last name/type and returns zero health.
Administrative removal and an entity that disappears without a death event close the active
narrative as `DEFEATED`, but never increment `total_bosses_defeated`. Event end clears it to `NONE`.

The three display lines are already resolved and preserve legacy `&` colors. `NONE` returns three
empty strings; `NOT_SPAWNED` returns only the localized first line; `ALIVE` returns localized
name, type, and current health lines; `DEFEATED` returns a green retained name plus localized
status and an empty third line. They can be used by any PlaceholderAPI consumer that supports
legacy colors (for example TAB, FeatherBoard, CMI, hologram plugins, or DeluxeMenus); compatibility
with untested consumers is not guaranteed. All templates come from the selected in-memory locale.

Historical counters are server-wide and are loaded from `plugins/BloodMoon/statistics.yml` into memory. `total_events` counts only normally completed events; abort, world unload, plugin disable, and crash-discard do not count. Death totals are added only when an event completes. Boss totals count every boss actually generated by BloodMoon (including a successful historical spawn command) and every natural boss defeat; failed spawns and administrative removals do not count. No individual player identity, IP, coordinates, or inventory is stored.

## Validation commands

```text
/papi list
/papi parse me %bloodmoon_active%
/papi parse me %bloodmoon_active_formatted%
/papi parse me %bloodmoon_world%
/papi parse me %bloodmoon_time_remaining_formatted%
/papi parse me %bloodmoon_boss_alive%
/papi parse me %bloodmoon_boss_state%
/papi parse me %bloodmoon_boss_state_formatted%
/papi parse me %bloodmoon_boss_name%
/papi parse me %bloodmoon_boss_type%
/papi parse me %bloodmoon_boss_health_formatted%
/papi parse me %bloodmoon_boss_display_line_1%
/papi parse me %bloodmoon_boss_display_line_2%
/papi parse me %bloodmoon_boss_display_line_3%
/papi parse me %bloodmoon_survivor_status%
/papi parse me %bloodmoon_death_count%
/papi parse me %bloodmoon_unique_deaths%
/papi parse me %bloodmoon_participants_current%
/papi parse me %bloodmoon_survivors_current%
/papi parse me %bloodmoon_total_events%
/papi parse me %bloodmoon_last_event_world%
/papi parse me %bloodmoon_last_event_duration_formatted%
```

An offline or missing player receives safe inactive/no-boss/not-participating values while global historical placeholders remain available from the in-memory snapshot. Unknown `%bloodmoon_*%` identifiers return `null` to PlaceholderAPI. The complete copy/paste validation list is [`examples/PlaceholderAPI-examples.txt`](examples/PlaceholderAPI-examples.txt).

## TAB scoreboard example

Use the real, YAML-validated [`examples/TAB-scoreboards.yml`](examples/TAB-scoreboards.yml). Its recommended full and compact designs use the three direct display lines and require no global TAB conditions. An advanced optional condition remains separate. Merge maps into TAB's existing configuration; do not duplicate global sections.

TAB and PlaceholderAPI must be installed separately by the administrator. BloodMoon-Reborn neither downloads nor controls TAB. See the official [PlaceholderAPI internal-expansion guide](https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/) and [TAB scoreboard guide](https://github.com/NEZNAMY/TAB/wiki/Feature-guide%3A-Scoreboard).
