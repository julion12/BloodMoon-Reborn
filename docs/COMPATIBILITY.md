# Compatibility

BloodMoon-Reborn 1.1.1 ships as one Java 21 artifact compiled against `paper-api:1.21.4-R0.1-SNAPSHOT`, with Bukkit `api-version: 1.21`. It uses shared Bukkit/Paper APIs and no NMS or CraftBukkit internals.

| Minecraft | Platform | Runtime Java | Status |
| --- | --- | ---: | --- |
| 1.21.x | Paper / Purpur | 21 | SUPPORTED / TESTED |
| 26.2 | Paper / Purpur | 25 | SUPPORTED / TESTED |

Paper's official requirements list Java 21 for 1.20 through 1.21.11 and Java 25 for 26.1 and later. See <https://docs.papermc.io/paper/getting-started/> and <https://docs.papermc.io/paper/dev/project-setup/>.

## Optional integrations

PlaceholderAPI, TAB, Multiverse-Core, WorldGuard, and MythicMobs are optional. They are not
required for the core Blood Moon lifecycle.

MythicMobs 5.12.1 was not compatible with the previously tested Minecraft 26.2 server line.
MythicMobs 5.13.0 has since been validated with BloodMoon-Reborn 1.1.1 on Minecraft 26.2,
including boss spawn, display, state updates, death, and reward separation. Always use a
MythicMobs release that explicitly declares support for the target Minecraft version.

Build facts:

- Gradle wrapper: 9.3.0
- Build JDK: Eclipse Temurin 21.0.8+9
- Minecraft 26.2 runtime JDK: Java 25
- Paper compile API: 1.21.4-R0.1-SNAPSHOT
- MythicMobs compile-only API: Mythic-Dist 5.12.1
- PlaceholderAPI compile-only API: 2.12.3
- TAB: no direct dependency
- Shaded YAML runtime: SnakeYAML 2.6 with `SafeConstructor`
- Declared Bukkit `api-version`: 1.21
- Artifact count: one

Paper and Purpur 26.2 have both completed runtime validation with Java 25. The validation covered
the core lifecycle, rewards, statistics, restart recovery, optional placeholder/scoreboard support,
and late-loaded Multiverse worlds.
