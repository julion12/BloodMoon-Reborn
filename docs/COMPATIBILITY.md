# Compatibility

BloodMoon-Reborn 1.1.0 uses one artifact compiled against `paper-api:1.21.4-R0.1-SNAPSHOT`, with `api-version: 1.21` and Java release/bytecode 21. It uses Bukkit/Paper API shared by the target range and no NMS or CraftBukkit internals.

| Server | Runtime Java | Status | Notes |
| --- | ---: | --- | --- |
| Paper 1.21.4 | 21 | BLOCKED | Official download API timed out in this environment; compile target |
| Paper 1.21.8 | 21 | NOT RUN | Expected common API; requires real smoke test |
| Paper 1.21.11 | 21 | NOT RUN | Relevant last 1.21 release; requires real smoke test |
| Paper 26.2 | 25 | BLOCKED | Official download API timed out and no local JDK 25 was available |
| Purpur 1.21.4/1.21.8/1.21.11/26.2 | as above | NOT RUN | No binaries downloaded |

Paper's official requirements list Java 21 for 1.20–1.21.11 and Java 25 for 26.1+. Paper changed dependency version syntax in 26.1; compiling against the oldest target avoids linking new-only APIs. See <https://docs.papermc.io/paper/getting-started/> and <https://docs.papermc.io/paper/dev/project-setup/>.

- Gradle wrapper: 9.3.0
- Build JDK used: Eclipse Temurin 21.0.8+9
- Paper compile API: 1.21.4-R0.1-SNAPSHOT
- MythicMobs compile-only API: Mythic-Dist 5.12.1
- Declared Bukkit `api-version`: 1.21
- Artifact count: one

Until real server tests are completed, the full range is a compatibility target, not a fully confirmed runtime matrix.
