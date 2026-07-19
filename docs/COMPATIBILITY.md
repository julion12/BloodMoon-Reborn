# Compatibility

BloodMoon-Reborn 1.1.0 ships as one Java 21 artifact compiled against `paper-api:1.21.4-R0.1-SNAPSHOT`, with Bukkit `api-version: 1.21`. It uses shared Bukkit/Paper APIs and no NMS or CraftBukkit internals.

| Server | Runtime Java | Validation status | Notes |
| --- | ---: | --- | --- |
| Paper 1.21.4 build 232 | 21 | PASSED | Compile baseline; real load/command/shutdown and functional lifecycle tests |
| Paper 1.21.8 build 60 | 21 | PASSED | Real load/command/shutdown; MythicMobs 5.12.1 bridge also enabled |
| Paper 1.21.11 build 132 | 21 | PASSED | Real load/command/shutdown |
| Paper 26.2 build 62 beta | 25 | PASSED (core) | Real load/command/shutdown with portable Temurin 25.0.3+9 |
| Purpur 1.21.8 build 2497 | 21 | PASSED | Real load/command/shutdown |
| Purpur 26.2 build 2610 | 25 | PASSED (core) | Real load/command/shutdown with portable Temurin 25.0.3+9 |

Paper's official requirements list Java 21 for 1.20 through 1.21.11 and Java 25 for 26.1 and later. See <https://docs.papermc.io/paper/getting-started/> and <https://docs.papermc.io/paper/dev/project-setup/>.

## MythicMobs limitation

MythicMobs remains optional. Version 5.12.1 enabled successfully with Paper 1.21.8 and BloodMoon activated its public-API bridge. On Paper 26.2 build 62, MythicMobs 5.12.1 failed during its own enable because its server-version/NMS resolver returned null. BloodMoon did not cause that failure and continued safely without the bridge.

Consequently, the core plugin is runtime-validated on 26.2, but **MythicMobs 5.12.1 integration is not compatible with the tested 26.2 build**. Do not claim full 26.2 + Mythic compatibility until a MythicMobs release that supports 26.2 can enable and the manual spawn/death checklist passes.

Build facts:

- Gradle wrapper: 9.3.0
- Build JDK: Eclipse Temurin 21.0.8+9
- 26.2 smoke-test JDK: Eclipse Temurin 25.0.3+9 LTS (portable, checksum verified)
- Paper compile API: 1.21.4-R0.1-SNAPSHOT
- MythicMobs compile-only API: Mythic-Dist 5.12.1
- Declared Bukkit `api-version`: 1.21
- Artifact count: one
