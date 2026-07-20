# Changelog

## 1.1.0 - 2026-07-19

### Added

- Per-world Blood Moon sessions with UUID participants, deaths, participation time, reward state, and associated boss.
- Disabled-by-default survivor command rewards and messages.
- Disabled-by-default command rewards for the existing vanilla boss killer.
- Optional MythicMobs boss selection, public-API spawn/death tracking, fallback, and reward separation.
- Internal lifecycle, participant, world, and boss placeholders.
- Idempotent 1.0.1 configuration migration with timestamped backup.
- Session crash markers and safe incomplete-session discard.
- Console-safe reload, status/survivor views, and tab completion.
- JUnit coverage for command parsing, placeholders, sessions, migration, boss rewards, and Mythic fallback.
- A configurable Bukkit BossBar for the vanilla boss with health and scoped audience updates.
- Complete English and Spanish locale catalogs, English fallback, and non-destructive legacy locale migration.
- Boss health placeholders and contextual `$b`, `%boss_name%`, and `%boss_type%` resolution.

### Changed

- Release version is 1.1.0; artifact is reproducible and named `BloodMoon-Reborn-1.1.0.jar`.
- Build remains against Paper API 1.21.4 and emits Java 21 bytecode for one cross-version JAR.
- Vanilla bosses now carry a PersistentDataContainer identity marker; legacy named bosses remain recognized during cleanup.
- Mythic boss messages now use the active/configured Mythic display name while UUID tracking remains unchanged.
- The historical `/bloodmoon spawnzombieboss` command now spawns the boss selected by `Boss.Mode`; its name and permission remain compatible.

### Fixed

- Console `/bloodmoon reload` null-world failure.
- Unsafe legacy command parsing when a suffix was missing or a command was empty.
- Concurrent modification while removing a defeated boss.
- Null death-message handling.
- Misspelled `Multiverse-Core` soft dependency.
- Mythic arrival/death text incorrectly reusing the vanilla `ZombieBossName`.
- Administrative boss spawning bypassing `Boss.Mode` and creating vanilla announcements/BossBars before a Mythic spawn.
- `killbosses` leaving tracked Mythic boss entities and session state behind.

No existing configuration key or vanilla boss default was removed or renamed.
