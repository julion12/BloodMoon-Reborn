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
- Timestamped, idempotent per-world configuration migration from 1.0.1.
- Crash markers discard incomplete sessions without granting uncertain rewards.

All new rewards default to disabled. MythicMobs, WorldGuard, and Multiverse-Core are soft dependencies.

## Installation and update

1. Stop the server and back up `plugins/BloodMoon/` and the worlds.
2. Replace the old JAR with `BloodMoon-Reborn-1.1.0.jar`.
3. Use Java 21 for Minecraft 1.21.x or Java 25 for Minecraft 26.1+.
4. Start the server. Existing per-world `config.yml` files receive only missing 1.1 sections; a timestamped backup is created first.
5. Leave rewards disabled until their commands have been reviewed.

Select `Language: en` or `Language: es` in `plugins/BloodMoon/locales.yml`. See [migration](docs/MIGRATION.md), [configuration](docs/CONFIGURATION.md), and [compatibility](docs/COMPATIBILITY.md).

## Commands

| Command | Permission | Purpose |
| --- | --- | --- |
| `/bloodmoon show` or `status` | `bloodmoon.show` | Show current/next event state |
| `/bloodmoon start` | `bloodmoon.start` | Start in the player's world |
| `/bloodmoon stop` | `bloodmoon.stop` | Stop in the player's world |
| `/bloodmoon reload` | `bloodmoon.reload` | Validate and reload config without recreating tasks |
| `/bloodmoon survivors` | `bloodmoon.show` | Show current eligible participant count |
| `/bloodmoon spawnzombieboss` | `bloodmoon.spawnzombieboss` | Spawn the existing vanilla boss |
| `/bloodmoon killbosses [rewards]` | `bloodmoon.killbosses` | Remove active vanilla bosses |
| `/bloodmoon spawnhorde [player]` | `bloodmoon.spawnhorde` | Spawn a configured horde |

Console commands require a world argument except `reload`. Tab completion is included.

## Documentation

- [Configuration](docs/CONFIGURATION.md)
- [Survivor rewards](docs/SURVIVOR_REWARDS.md)
- [MythicMobs](docs/MYTHICMOBS.md)
- [Placeholders](docs/PLACEHOLDERS.md)
- [Migration](docs/MIGRATION.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Test matrix](docs/TEST_MATRIX.md)
- [Manual release checklist](docs/MANUAL_TEST_CHECKLIST.md)
- [Changelog](CHANGELOG.md)

## License

The original license and attribution are preserved in [LICENSE](LICENSE).
