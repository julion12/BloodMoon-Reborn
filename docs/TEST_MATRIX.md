# Test matrix

Validation date: 2026-07-19. `PASSED` means the check was executed in an isolated server in this workspace. `MANUAL REQUIRED` means the check needs a real connected player or an administrative legacy configuration that was not available. Server downloads came from the official Paper, Purpur, Adoptium, and Lumine endpoints.

## Automated regression

| Check | Result | Evidence |
| --- | --- | --- |
| Gradle clean compile | PASSED | Eclipse Temurin 21.0.8+9, Gradle 9.3.0 |
| JUnit suite | PASSED | 19 tests, 0 failures |
| Reproducible JAR task | PASSED | JDK 21 and JDK 25 produced the same 449,774-byte JAR; SHA-256 `5636435d39d3a9bce27ce69e82be53030e13f0a715c67b2f504c47324d477bee` |
| Legacy config and idempotent migration | PASSED | Unit tests |
| Command parsing, placeholders, and null handling | PASSED | Unit tests |
| Survival, death, reconnect, late join, minimum time, once-only reward, and two-world isolation | PASSED | Unit tests |
| Vanilla boss killer, no-killer, and duplicate death handling | PASSED | Unit tests |
| MythicMobs absent, fallback, and default reward policy | PASSED | Unit tests |

## Real server startup and command smoke tests

The same production JAR was used for every row. Each server reached `Done`, enabled BloodMoon 1.1.0, accepted `plugins`, `version BloodMoon`, `bloodmoon reload`, and `bloodmoon status world`, then disabled cleanly through `stop`. No BloodMoon class-link, API, scheduler, or shutdown exception was observed.

| Platform | Runtime | Official build | Result |
| --- | --- | ---: | --- |
| Paper 1.21.4 | Temurin 21.0.8+9 | 232 | PASSED |
| Paper 1.21.8 | Temurin 21.0.8+9 | 60 | PASSED |
| Paper 1.21.11 | Temurin 21.0.8+9 | 132 | PASSED |
| Paper 26.2 | Temurin 25.0.3+9 LTS | 62 beta | PASSED |
| Purpur 1.21.8 | Temurin 21.0.8+9 | 2497 | PASSED |
| Purpur 26.2 | Temurin 25.0.3+9 LTS | 2610 | PASSED |

The portable JDK 25 archive SHA-256 was verified against Adoptium (`709312cd0420296d9b9de917fe6e28a5b979e875ee5ab91783fb79bcd5857235`). It was used only inside the ignored smoke-test directory; no global Java installation or environment variable was changed.

## Real functional checks on Paper 1.21.4

| Check | Result | Observed evidence |
| --- | --- | --- |
| Start/end lifecycle | PASSED | One OnStart and one OnEnd marker, same session UUID, duration 10 seconds |
| Reload during active session | PASSED | Session UUID remained stable; commands and tasks were not duplicated |
| Clean completion | PASSED | `sessions.yml` empty after stop; clean plugin disable |
| Crash recovery | PASSED | Active session persisted, exact test process was killed, next start archived it as `sessions.discarded-*.yml`, and no OnEnd/reward command ran |
| Two normal worlds | PASSED | A temporary POSTWORLD fixture created `world2`; both sessions ran, stopping `world` left `world2` active, and `world2` then stopped independently after its 40-tick check cycle |
| Generated-config migration | PASSED WITH LIMITATION | First load created exactly one timestamped backup; repeated restarts created no second backup and no duplicate 1.1.0 sections |
| Real 1.0.1 administrative config | MANUAL REQUIRED | No user-owned 1.0.1 config was present in the repository, history, or attachments; synthetic coverage is not reported as a real migration |
| Player eligibility and survivor reward delivery | MANUAL REQUIRED | Requires connected players |
| Vanilla boss killer/no-killer reward delivery | MANUAL REQUIRED | Requires a connected player to spawn near and defeat the boss |

## MythicMobs 5.12.1 live checks

The tested official `Mythic-Dist-5.12.1.jar` was 20,242,096 bytes with SHA-256 `3781927033898c75b0c4e21a8eee1756ca822d80160430c3da9de760c9137cd1`.

| Platform | Result | Evidence |
| --- | --- | --- |
| Paper 1.21.8 + MythicMobs 5.12.1 | PARTIAL PASS | MythicMobs enabled; BloodMoon logged `MythicMobs integration enabled`; reload, status, and shutdown passed without class/link errors |
| Paper 26.2 + MythicMobs 5.12.1 | UPSTREAM BLOCKED | MythicMobs failed its own enable with an internal `ServerVersion.getNMS()` null result before BloodMoon could activate the bridge; BloodMoon core still enabled and stopped cleanly |
| BloodMoon without MythicMobs | PASSED | All six core server rows loaded without optional-dependency class errors |
| Actual Mythic boss spawn/death/reward isolation | MANUAL REQUIRED | The production spawn policy correctly requires a real player in the world; no player was available in the isolated console-only server |

## Release gate

**NOT READY for publication.** Core runtime compatibility is demonstrated through Paper/Purpur 26.2, but the critical live Mythic spawn/death/reward test and the player reward scenarios remain manual. Paper 26.2 cannot currently complete the requested MythicMobs 5.12.1 test because that MythicMobs build fails before BloodMoon integration starts. Follow `docs/MANUAL_TEST_CHECKLIST.md` and attach the resulting logs before changing this gate.
