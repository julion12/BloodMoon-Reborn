# BloodMoon-Reborn

BloodMoon-Reborn is a conservative continuation of SpectralMemories' BloodMoon plugin. It keeps the existing per-world events, enhanced mobs, hordes, vanilla zombie boss, WorldGuard support, commands, and configuration model while adding optional 1.1.0 reward features.

Original author: **SpectralMemories**

Modernization and maintenance: **JulioN12**

## Version 1.1.0

- One Java 21 JAR targeting Paper/Purpur 1.21.4 through 26.2.
- Existing `CommandsOnStart` and `CommandsOnEnd` now support safe parsing and internal placeholders.
- Optional survivor command rewards with UUID-based death disqualification and once-per-session protection.
- Optional command rewards for the existing vanilla boss killer.
- Optional MythicMobs boss mode through its public API; vanilla remains the default and fallback.
- Contextual Mythic boss names without reusing the vanilla `ZombieBossName`.
- Configurable vanilla-only BossBar with nearby/world/all audiences and health placeholders.
- Complete English and Spanish catalogs with English fallback and legacy `locales.yml` overrides.
- Optional internal PlaceholderAPI expansion for event, boss, time, participation, survivor state, and O(1) per-world session statistics.
- Narrative per-session boss state (`NOT_SPAWNED`, `ALIVE`, `DEFEATED`) with localized public output.
- Three portable, localized boss display lines for PlaceholderAPI consumers, bringing the public total to 41.
- Administrator guides and all seven examples bundled in the JAR and installed without overwriting local edits.
- Atomic server-wide historical statistics in `statistics.yml`, without personal player history.
- Timestamped, idempotent per-world configuration migration from 1.0.1.
- Crash markers discard incomplete sessions without granting uncertain rewards.

All new rewards default to disabled. PlaceholderAPI, MythicMobs, WorldGuard, and Multiverse-Core are soft dependencies. TAB is not a dependency.

Active Blood Moon sessions expose total deaths, unique dead-player UUIDs, registered participants, and current survivors through `%bloodmoon_death_count%`, `%bloodmoon_unique_deaths%`, `%bloodmoon_participants_current%`, and `%bloodmoon_survivors_current%`. These counters are isolated per world, reset with each event, and are not persisted as history.

Completed events contribute aggregate server history and a last-event snapshot. Boss state and history are available through the same optional `%bloodmoon_*%` expansion without disk access on placeholder requests.

## Installation and update

1. Stop the server and back up `plugins/BloodMoon/` and the worlds.
2. Replace the old JAR with `BloodMoon-Reborn-1.1.0.jar`.
3. Use Java 21 for Minecraft 1.21.x or Java 25 for Minecraft 26.1+.
4. Start the server. Existing per-world `config.yml` files receive only missing 1.1 sections; a timestamped backup is created first.
5. Leave rewards disabled until their commands have been reviewed.

Select `Language: en` or `Language: es` in `plugins/BloodMoon/locales.yml`. See [migration](docs/MIGRATION.md), [configuration](docs/CONFIGURATION.md), and [compatibility](docs/COMPATIBILITY.md).

On startup, missing administrator files are copied from the JAR to `plugins/BloodMoon/README.txt`,
`plugins/BloodMoon/EXAMPLES/`, and `plugins/BloodMoon/docs/`. Existing files are never overwritten;
upgrades add only newly introduced or manually deleted files.

## Commands

| Command | Permission | Purpose |
| --- | --- | --- |
| `/bloodmoon show` or `status` | `bloodmoon.show` | Show current/next event state |
| `/bloodmoon start` | `bloodmoon.start` | Start in the player's world |
| `/bloodmoon stop` | `bloodmoon.stop` | Stop in the player's world |
| `/bloodmoon reload` | `bloodmoon.reload` | Validate and reload config without recreating tasks |
| `/bloodmoon survivors` | `bloodmoon.show` | Show current eligible participant count |
| `/bloodmoon spawnzombieboss` | `bloodmoon.spawnzombieboss` | Spawn the currently configured Blood Moon boss |
| `/bloodmoon killbosses [rewards]` | `bloodmoon.killbosses` | Remove active vanilla and Mythic bosses |
| `/bloodmoon spawnhorde [player]` | `bloodmoon.spawnhorde` | Spawn a configured horde |

Console commands require a world argument except `reload`. Tab completion is included.

`spawnzombieboss` keeps its historical name and permission for compatibility. It resolves `Boss.Mode` before creating or announcing anything: `VANILLA` creates the built-in zombie and its configured Bukkit BossBar, `MYTHICMOBS` creates only the configured MythicMob and leaves its BossBar to MythicMobs, and `NONE` creates nothing. A successful Mythic fallback produces one vanilla boss and one final vanilla announcement.

## Documentation

- [Configuration](docs/CONFIGURATION.md)
- [Survivor rewards](docs/SURVIVOR_REWARDS.md)
- [MythicMobs](docs/MYTHICMOBS.md)
- [Placeholders](docs/PLACEHOLDERS.md)
- [PlaceholderAPI and TAB](docs/PLACEHOLDERAPI.md)
- [Ready-to-copy examples](docs/EXAMPLES.md)
- [Migration](docs/MIGRATION.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Test matrix](docs/TEST_MATRIX.md)
- [Manual release checklist](docs/MANUAL_TEST_CHECKLIST.md)
- [Changelog](CHANGELOG.md)

## License

The original license and attribution are preserved in [LICENSE](LICENSE).
