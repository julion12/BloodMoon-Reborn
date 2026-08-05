# BloodMoon-Reborn 1.1.1 hotfix release notes

BloodMoon-Reborn 1.1.1 fixes a production error in the main-thread snapshot publisher used by
PlaceholderAPI consumers such as TAB.

## Fixed

- Players in a late-loaded, reloaded, unsupported, or temporarily unconfigured world now receive
  safe inactive placeholder values instead of stopping the snapshot refresh with a
  `NullPointerException`.
- A failure for one player no longer prevents snapshots for other online players from updating.
- Normal worlds loaded through Multiverse after startup can register their existing or default
  per-world configuration safely.
- World configuration lookups now survive Bukkit replacing a `World` object for the same UUID.
- Repeated failures produce at most one warning per world and cause. Resolution attempts are
  throttled, and the warning state clears when configuration becomes valid again.
- Unloaded worlds no longer retain a self-rescheduling BloodMoon night check.
- Unloaded-world actuators are detached from Bukkit events before their configuration is removed.
- Placeholder snapshot scheduling is canceled before plugin shutdown cleanup.

## Compatibility

- The same 41 `%bloodmoon_*%` placeholders are available with unchanged meanings and output rules.
- PlaceholderAPI requests remain constant-time reads from immutable snapshots. They do not call
  Bukkit APIs or read configuration files.
- Existing BloodMoon 1.1.0 configuration files remain compatible; no new key is required.
- Paper and Purpur versions supported by 1.1.0 remain in scope.

## Validation

- 167 automated tests passed with no failures or skipped tests.
- An isolated smoke passed on Paper 1.21.11 build 69 with Java 21, PlaceholderAPI 2.12.3,
  TAB 6.1.1, Multiverse-Core 5.7.3 and MythicMobs 5.12.1.
- The smoke covered an inactive default world, an active event, a normal Multiverse fixture created
  after startup, plugin reload, rapid player world changes, world unload/reload and clean shutdown.
- Placeholder values and TAB continued updating; no `NullPointerException`, AsyncCatcher report,
  repeated snapshot warning or scheduler exception occurred.

## Installation or upgrade

1. Stop the server.
2. Back up `plugins/BloodMoon/`.
3. Replace the previous BloodMoon JAR with `BloodMoon-Reborn-1.1.1.jar`.
4. Start the server and confirm that BloodMoon, PlaceholderAPI and scoreboard integrations enable.
5. If Multiverse is installed, enter or load each additional world and confirm the scoreboard keeps
   updating without repeated warnings.

## Release artifact

- File: `BloodMoon-Reborn-1.1.1.jar`
- Size: 576,455 bytes
- SHA-256: `02A2AE77FC8136B44C09245093806BD4D1667B88F902AEEDC41DEC3246F33B48`
- Reproducibility: two clean builds and an additional no-daemon JDK 21 build were byte-identical.

## GitHub release text

BloodMoon-Reborn 1.1.1 is a focused compatibility hotfix for PlaceholderAPI/TAB snapshot updates.
It prevents missing per-world configuration state from producing repeated scheduler exceptions,
keeps other players updating when one world is unavailable, safely resolves late-loaded normal
worlds, and cleans up world tasks on unload. Existing 1.1.0 configurations and all 41 placeholders
remain compatible. The release JAR is built reproducibly; verify its SHA-256 against the artifact
section above after downloading.

## Spigot update text

Version 1.1.1 fixes repeated PlaceholderAPI snapshot errors that could occur when a player was in a
late-loaded, reloaded, or temporarily unconfigured world. Affected players now receive safe inactive
values while all other scoreboards continue updating. Multiverse world registration, unload cleanup
and warning deduplication were hardened without changing configuration keys or placeholder output.
Before uploading the JAR, verify its filename, 576,455-byte size and SHA-256 listed above.
