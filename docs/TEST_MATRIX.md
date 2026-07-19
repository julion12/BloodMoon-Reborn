# Test matrix

`PASSED` means executed in this workspace. `BLOCKED` includes the observed blocker; no inferred runtime result is reported as passed.

## Automated

| Check | Result | Evidence |
| --- | --- | --- |
| Gradle clean compile | PASSED | Temurin 21.0.8, Gradle 9.3.0 |
| JUnit suite | PASSED | 19 tests, 0 failures |
| Reproducible JAR task | PASSED | `BloodMoon-Reborn-1.1.0.jar` |
| Legacy config + idempotent migration | PASSED | Unit tests |
| Command parsing/placeholders/nulls | PASSED | Unit tests |
| Survival/death/reconnect/late join/minimum/once/two worlds | PASSED | Unit tests |
| Vanilla boss killer/no-killer/duplicate | PASSED | Unit tests |
| Mythic absent/fallback/default | PASSED | Unit tests |

## Server smoke tests

| Platform | Result | Reason |
| --- | --- | --- |
| Paper 1.21.4 | BLOCKED | Both official Paper download APIs timed out from the workspace |
| Paper 1.21.8 | NOT RUN | Server binary unavailable locally |
| Paper 1.21.11 | NOT RUN | Server binary unavailable locally |
| Paper 26.2 | BLOCKED | Download API timeout and no installed JDK 25 |
| Purpur targets | NOT RUN | No server binaries downloaded |
| MythicMobs live integration | NOT RUN | No licensed/test server plugin binary configured |

Gameplay checks requiring real players (death, two simultaneous worlds, rewards, reload from player) remain manual. See the release checklist in this file's final section before publication:

1. Start each target server with its required Java.
2. Confirm plugin enable and per-world migration.
3. Exercise OnStart/OnEnd and a failed command followed by a valid command.
4. Test survivor/death, world leave, disconnect, late join, and two worlds.
5. Defeat vanilla boss with and without killer.
6. Install a compatible MythicMobs build and verify own rewards plus the opt-in additional command mode.
7. Reload from player and console; stop cleanly and inspect logs.
