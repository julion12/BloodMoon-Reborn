# Test matrix

Validation date: 2026-07-19. `PASSED` means the check was executed in an isolated server in this workspace. `MANUAL REQUIRED` means the check needs a real connected player or an administrative legacy configuration that was not available. Server downloads came from the official Paper, Purpur, Adoptium, and Lumine endpoints.

## Automated regression

| Check | Result | Evidence |
| --- | --- | --- |
| Gradle clean compile | PASSED | Eclipse Temurin 21.0.8+9, Gradle 9.3.0 |
| JUnit suite | PASSED | 70 tests, 0 failures, including the original 19 |
| Release artifact task | PASSED | Final JDK 21 artifact is 500,323 bytes; SHA-256 `0a06a4030d4acdbf6da1f3ade1983e17a017b7f1775bae1a1f85906e2b49d1c1` |
| Legacy config and idempotent migration | PASSED | Unit tests |
| Command parsing, placeholders, and null handling | PASSED | Unit tests |
| Survival, death, reconnect, late join, minimum time, once-only reward, and two-world isolation | PASSED | Unit tests |
| Vanilla boss killer, no-killer, and duplicate death handling | PASSED | Unit tests |
| MythicMobs absent, fallback, and default reward policy | PASSED | Unit tests |
| Contextual vanilla/Mythic boss names and `$b`/modern placeholders | PASSED | Unit tests cover display order, InternalName fallback, nulls, legacy and modern tokens |
| Vanilla BossBar lifecycle and health values | PASSED | Unit tests cover creation policy, Mythic exclusion, reload idempotency, cleanup state, health rendering, and 0.0–1.0 clamping |
| English, Spanish, fallback, legacy override, and locale migration | PASSED | Complete YAML catalogs parsed; backup, custom `ZombieBossName`, missing-key merge, and idempotency tested |
| Documented YAML examples | PASSED | Config migration output and Mythic mob example parsed by SnakeYAML |

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
| Paper 1.21.8 + MythicMobs 5.12.1 | PARTIAL PASS | MythicMobs enabled; BloodMoon logged `MythicMobs integration enabled`; the documented `BloodMoonBoss` YAML loaded as the ninth mob; reload, status, and shutdown passed without class/link errors |
| Paper 26.2 + MythicMobs 5.12.1 | UPSTREAM BLOCKED | MythicMobs failed its own enable with an internal `ServerVersion.getNMS()` null result before BloodMoon could activate the bridge; BloodMoon core still enabled and stopped cleanly |
| BloodMoon without MythicMobs | PASSED | All six core server rows loaded without optional-dependency class errors |
| Actual Mythic boss spawn/death/reward isolation | MANUAL REQUIRED | The production spawn policy correctly requires a real player in the world; no player was available in the isolated console-only server |

## 1.1.0 boss-name, BossBar, and locale follow-up smoke

Paper 1.21.8 build 60 with Temurin 21.0.8+9 was rerun using the new 477,833-byte artifact.

| Check | Result | Evidence |
| --- | --- | --- |
| New English installation | PASSED | Plugin enabled, created `locales/en.yml` and `locales/es.yml`, added `Boss.VanillaBossBar`, reloaded, returned English status, and stopped cleanly |
| Spanish selection | PASSED | `Language: es` loaded after reload; status/session messages were Spanish and UTF-8 accents were verified in `latest.log` |
| Legacy customized `locales.yml` | PASSED | Real server migration created one backup, retained `ZombieBossName: "el duro"` and a customized status string, appended missing 1.1 keys, and produced no second backup after restart |
| MythicMobs documented example | PASSED | MythicMobs 5.12.1 accepted the exact documented boss YAML and reported nine loaded mobs |
| Vanilla boss message/bar/damage/removal/reward | MANUAL REQUIRED | No real Minecraft player was connected; the production boss intentionally does not spawn without one |
| Mythic display/death/no-vanilla-bar/reward isolation | MANUAL REQUIRED | Bridge and schema loaded, but the live boss lifecycle requires a real connected player |
| Reload without duplicate visible bars | MANUAL REQUIRED | Lifecycle state is unit-tested; visual confirmation requires a player observing an active boss |

## Contextual `spawnzombieboss` regression

The 2026-07-19 follow-up routes the historical command and automatic/permanent respawn through `SpawnConfiguredBoss`. Unit tests cover the actual-mode result, UUID/name/bar metadata, no vanilla supplier call after a successful Mythic spawn, one-shot fallback, disabled/failure results, preserved command/permission, shared entry point, and vanilla/Mythic cleanup state.

| Check | Result | Evidence |
| --- | --- | --- |
| VANILLA/MYTHICMOBS/NONE routing | PASSED | Pure coordinator tests; the unselected concrete spawner is never called |
| Mythic display and vanilla BossBar exclusion | PASSED | Result and source-boundary tests; Mythic method contains no `ZombieIBoss`, `ZombieBossName`, or vanilla bar access |
| Fallback true/false | PASSED | Tests cover successful vanilla result with one name/bar and failed result with no entity/bar |
| Historical command and permission | PASSED | `spawnzombieboss` and `bloodmoon.spawnzombieboss` remain registered; executor calls `SpawnConfiguredBoss` |
| `killbosses` cleanup | PASSED | Tests verify tracked Mythic UUID removal, map clear, and session boss-ID clear in addition to vanilla cleanup |
| Paper 1.21.8 + MythicMobs 5.12.1 startup | PASSED | Final plugin loaded, integration enabled, documented YAML raised loaded mob count from 8 to 9, reload/command/killbosses/shutdown completed cleanly |
| Console command without a player | PASSED | Returned localized `BossSpawnFailed`; no entity or BossBar can be created because the production policy requires a player |
| `Boss.Mode: NONE` command | PASSED | Exact final JAR returned localized `BossDisabled`, created no entity/bar, and shut down cleanly |
| VANILLA damage/bar/death/reward with player | MANUAL REQUIRED | No connected Minecraft player was used in this smoke |
| MYTHIC display/bar/death/reward with player | MANUAL REQUIRED | No connected Minecraft player was used in this smoke |
| Successful fallback entity/bar with player | MANUAL REQUIRED | No connected Minecraft player was used in this smoke |

## PlaceholderAPI expansion

| Check | Result | Evidence |
| --- | --- | --- |
| Resolver identifiers and fallbacks | PASSED | Unit tests cover all 17 public identifiers, unknown identifiers, null player, localized fallbacks, boolean stability, time formatting, and safe absent state |
| World/event/player context | PASSED | Unit tests cover active/inactive world snapshots, participation, eligibility, disqualification, and non-participation |
| Boss state | PASSED | Unit tests cover vanilla/Mythic names and types, real health values, no-boss values, rounding, and 0–100 clamping |
| Lifecycle architecture | PASSED | Tests verify optional `softdepend`, one registration site, no registration during BloodMoon reload, `persist() = true`, and explicit close |
| Request-path performance | PASSED | Source-boundary test excludes file/YAML access, global entity traversal, schedulers, and command dispatch; runtime uses tracked UUID/entity/session references |
| Paper 1.21.8 + PlaceholderAPI 2.12.3 | PARTIAL PASS | Exact final JAR registered `bloodmoon` once; `/papi list` and `info` succeeded; null-player parses returned safe localized values; BloodMoon reload and PAPI reload retained one expansion; clean shutdown |
| Existing bundled locale catalogs | PASSED | Real smoke added only the seven missing keys to existing English/Spanish catalogs, created backups, preserved existing values, and returned localized parses |
| Server without PlaceholderAPI | PASSED | Exact final JAR enabled, reloaded, and stopped on Paper 1.21.8 without missing-class errors or PlaceholderAPI warnings |
| Connected-player event/boss/world/TAB behavior | MANUAL REQUIRED | No Minecraft player or TAB plugin was connected during the smoke; visual and live-entity checks remain pending |

## Release gate

**NOT READY for publication.** Core runtime compatibility is demonstrated through Paper/Purpur 26.2, but the critical live Mythic spawn/death/reward test and the player reward scenarios remain manual. Paper 26.2 cannot currently complete the requested MythicMobs 5.12.1 test because that MythicMobs build fails before BloodMoon integration starts. Follow `docs/MANUAL_TEST_CHECKLIST.md` and attach the resulting logs before changing this gate.
