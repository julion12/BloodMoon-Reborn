# BloodMoon-Reborn 1.1.0 final pre-RC audit

Audit date: 2026-07-24  
Scope: Phase 1 only; no push, merge, tag, release, publication, stable-branch change, rebase, or squash.

## 1. Current branch

`feature/1.1.0-multiversion-rewards-mythicmobs`.

## 2. Git state

The worktree was clean before the audit. Four isolated correction commits were created, followed
by the documentation commit containing this report. Ignored smoke-test directories remain outside
the release artifact. There are no staged/untracked release files after final sign-off.

## 3. Comparison base

Local `main` and `origin/main`: `292dd40 Update BloodMoon-Reborn to v1.0.1`; historical tag
`v1.0.1`. The audit did not fetch, push, merge, or rewrite history.

## 4. Commits included in 1.1.0

There were 52 feature/audit-code commits in `main..f66d160` before this report commit:

```text
95e9298 build: prepare 1.1.0 multiversion release
7c79999 fix: preserve and migrate legacy world configuration
23dc8b4 feat: add safe lifecycle command placeholders
2811e88 feat: track participants and survivor eligibility
e8ea883 feat: add survivor boss and optional MythicMobs rewards
663a077 fix: harden reload console commands and completion
e5b987c fix: retain and report invalid reload configuration
aeb5a38 fix: separate MythicMobs and Blood Moon rewards
365b885 build: generate reproducible release reports
392cbc2 docs: document 1.1.0 features and compatibility
476149d docs: record real 1.1.0 validation matrix
b73466c fix: resolve configured MythicMob display name
fbd9e00 feat: add vanilla boss health bar
45786e2 feat: add English and Spanish locale support
0544665 docs: add reward and command configuration examples
1efb989 test: validate documented YAML examples
11a1201 docs: record boss locale and Mythic smoke results
951bef2 fix: route boss command through configured boss mode
d28ca47 test: cover contextual administrative boss spawning
0e2e3a8 fix: avoid false console success on boss spawn failure
1a004ba docs: clarify spawnzombieboss compatibility behavior
1d53095 docs: record contextual boss smoke results
aa2ad95 feat: expose Blood Moon state through PlaceholderAPI
00969b9 test: cover contextual PlaceholderAPI expansion
77adb28 build: update PlaceholderAPI API to 2.12.3
2e775fb docs: add TAB scoreboard integration example
24b8832 fix: migrate missing bundled locale placeholders
5b5fada docs: record PlaceholderAPI smoke results
10241dd feat: expose Blood Moon session statistics
6838c1a feat: track boss session state
1052cdc feat: persist Blood Moon historical statistics
fdbee93 docs: add ready-to-copy integration examples
28c0f2d test: validate statistics placeholders and examples
b4f59c6 chore: exclude runtime smoke files
d5fbe13 fix: exclude administrative boss removals from victories
3e70a07 docs: record final integration smoke results
4c40c81 fix: count successful out-of-session boss lifecycles
25da2f3 docs: confirm final artifact PlaceholderAPI smoke
a1db51a feat: distribute administrator guides on first startup
7902942 feat: expose dynamic boss display line placeholders
1292ab7 docs: simplify scoreboard integration examples
12d8adf test: validate bundled guides and display placeholders
c6787d3 fix: make placeholder snapshots async safe
5602d2d test: cover concurrent placeholder refreshes
447cd0a docs: document async safe placeholder resolution
767b834 refactor: limit bundled administrator files to examples
e483fbb docs: add bilingual example indexes and variants
753862a test: validate bilingual example distribution
9e8e8c4 fix: harden release artifact and YAML parsing
47fd3ae fix: release completed boss lifecycle state
3923b3f fix: minimize incomplete session markers
f66d160 fix: redact configured command arguments in logs
```

The final documentation commit follows `f66d160` and is also part of the candidate branch.

## 5. Version audit

Public version is consistently `1.1.0` in `build.gradle`, `plugin.yml`, `README.md`,
`CHANGELOG.md`, `docs/RELEASE_NOTES_1.1.0.md`, migration/configuration/compatibility documents,
the bundled `README.txt`, both locale catalogs, examples, migrators, console startup text, tests,
JAR name, and third-party notice. There is no `paper-plugin.yml`, no plugin snapshot version, and
no stale RC label.

References to `1.0.1` in changelog, migration docs, tests, and the Git base are legitimate
migration/history. `paper-api:1.21.4-R0.1-SNAPSHOT` is the Paper API coordinate, not the plugin
version. No illegitimate `1.0.0` reference was found.

## 6. Identity and credits

- Bukkit name: `BloodMoon`; public project: `BloodMoon-Reborn`.
- Original author: SpectralMemories; maintainer/contributor: JulioN12.
- Repository/website: `https://github.com/julion12/BloodMoon-Reborn`.
- Descriptor, README, startup logs, release notes, and zlib license preserve this distinction.

## 7. JAR inventory

Final JAR has 421 unique ZIP entries:

| Root | Entries | Contents |
| --- | ---: | --- |
| `distribution/` | 20 | directory entries, bilingual README, 17 examples |
| `locales/` | 3 | directory plus EN/ES catalogs |
| `META-INF/` | 19 | manifest, Maven metadata, project/Apache licenses, notices |
| `org/spectralmemories/` | 113 | plugin and required SQLAccess runtime classes/directories |
| `org/yaml/` | 264 | shaded SnakeYAML 2.6 runtime |
| `plugin.yml` | 1 | Bukkit descriptor |

No duplicate path exists. JetBrains annotations are compile-only and are not packaged.

## 8. Correct exclusions

The complete filename and embedded ASCII scan found no `.git`, `.github`, `.idea`, `.iml`,
reports, logs, databases, development `statistics.yml`, worlds, smoke folders, temporary files,
tests, Java sources, internal `docs/`, Markdown, Codex artifacts, `<user-home>/`,
`<user-home>/Documents`, `localhost`, `127.0.0.1`, password/API-key/webhook markers, or test paths.

## 9. Dependencies

| Dependency | Classification | Packaged | Required at runtime |
| --- | --- | --- | --- |
| Paper API 1.21.4 | compileOnly API | No | Paper/Purpur server |
| WorldGuard 7.0.9 | compileOnly/soft dependency | No | No |
| MythicMobs 5.12.1 | compileOnly/soft dependency | No | No |
| PlaceholderAPI 2.12.3 | compileOnly/soft dependency | No | No |
| Multiverse-Core | soft dependency only | No | No |
| TAB | integration consumer only | No | No |
| SnakeYAML 2.6 | shaded runtime | Yes | Yes |
| `libs/SQLAccess.jar` | shaded local runtime helper | Yes | Yes for the legacy cache layer |
| JetBrains annotations | compileOnly/testCompileOnly | No | No |
| JUnit 5.13.4, launcher, Paper/PAPI test APIs | test only | No | No |

Runtime classpath resolves only SnakeYAML plus the local file dependency. There are no external
API duplicates in the JAR. No relocation is configured; bundled packages do not overlap the
plugin namespace.

## 10. `plugin.yml`

Confirmed: name, version, main class, `api-version: 1.21`, original author, maintainer contributor,
description, official website, `POSTWORLD` load, four optional dependencies, one command,
usage, and defaults. The accidental production `testsuite` command and executor were removed.

## 11. Commands and permissions

| Subcommand | Permission |
| --- | --- |
| `show`, `status`, `survivors` | `bloodmoon.show` |
| `start` | `bloodmoon.start` |
| `stop` | `bloodmoon.stop` |
| `reload` | `bloodmoon.reload` |
| `spawnzombieboss` | `bloodmoon.spawnzombieboss` |
| `spawnhorde` | `bloodmoon.spawnhorde` |
| `killbosses` | `bloodmoon.killbosses` |

The base `/bloodmoon` command uses `bloodmoon.bloodmoon`. Every code permission is declared; no
orphan command, undeclared permission, unused test permission, or conflicting alias remains.
Console/RCON world argument behavior matches public documentation.

## 12. Configurations

Reviewed generated per-world config, `locales/en.yml`, `locales/es.yml`, statistics schema, legacy
locale selector/migration, and all 16 example YAML files. Automated parsing rejects duplicates.
Missing keys fall back/migrate non-destructively. Migration is versioned/idempotent and backs up
before writing. CommandsOnStart/End, SurvivorRewards, Boss rewards, Mythic mode, and fallback are
disabled or conservative by default; example reward commands are comments or harmless markers.
No development values or excessive enabled rewards are shipped.

## 13. Rewards

Static and automated checks cover UUID/world/session separation, death disqualification, late
join policy, disconnect/world-leave policy, minimum participation, completion-only survivor
rewards, once-per-session protection, abort/crash no-payout, boss UUID once protection,
administrative removal, Mythic/vanilla reward isolation, missing Mythic fallback, offline/null
players, and reload ownership. Commands are administrator-trusted Bukkit commands, not OS shell
commands. At the time of this Phase-1 snapshot, manual real-player reward delivery had not yet
been incorporated into the audit; the Phase-2 addendum below supersedes that pending classification.

## 14. Active and historical statistics

Active counters are per world/session, reset on new events, ignore out-of-event deaths, clamp
survivors, and are exposed from immutable O(1) snapshots. Historical data is a versioned,
fixed-size aggregate plus last completed event, saved by temporary-file atomic replacement;
aborted events do not contribute and corrupt input is preserved before defaults.

Crash markers now store only session ID, world name, and start time. They never store player UUID,
death, or reward lists and are archived without payout after an incomplete shutdown. The
in-memory `completedSessions` UUID guard grows by one entry per completed event until plugin
restart; this is a low-rate future improvement, not persistent personal history.

## 15. Inventory of 41 public placeholders

All identifiers use `%bloodmoon_<identifier>%`. Inactive/no-history values are stable zero,
`false`, `NONE`, `00:00`, or localized unavailable text as documented. Player-specific fields
return safe non-participant values for null/offline players.

| Group | Identifiers |
| --- | --- |
| Event (5) | `active`, `active_formatted`, `world`, `time_remaining_seconds`, `time_remaining_formatted` |
| Boss core (12) | `boss_alive`, `boss_name`, `boss_type`, `boss_health`, `boss_max_health`, `boss_health_percent`, `boss_health_formatted`, `boss_state`, `boss_state_formatted`, `boss_display_line_1`, `boss_display_line_2`, `boss_display_line_3` |
| Player/session (9) | `participating`, `participation_seconds`, `participation_formatted`, `survivor_eligible`, `survivor_status`, `death_count`, `unique_deaths`, `participants_current`, `survivors_current` |
| Totals (5) | `total_events`, `total_death_events`, `total_unique_deaths`, `total_bosses_spawned`, `total_bosses_defeated` |
| Last event (10) | `last_event_world`, `last_event_duration_seconds`, `last_event_duration_formatted`, `last_event_death_count`, `last_event_unique_deaths`, `last_event_participants`, `last_event_survivors`, `last_boss_name`, `last_boss_type`, `last_event_ended_at` |

The group labels total 41 identifiers (event 5 + boss 12 + player/session 9 + totals 5 + last event
10). Implementation, resolver identifier set, PAPI adapter tests, PAPI docs, general placeholder
docs, EN/ES locale values, command examples, and EN/ES TAB examples agree. Request resolution does
one UUID lookup and one atomic map lookup; it does not traverse players/entities or access disk.

## 16. Compatibility by version

| Platform | Status | Runtime/notes |
| --- | --- | --- |
| Paper 1.21.4 build 232 | Tested core | Java 21; compile baseline |
| Paper 1.21.8 build 60 | Tested core + integrations | Java 21; PAPI, TAB, Mythic 5.12.1 |
| Paper 1.21.11 build 132 | Tested core | Java 21 |
| Purpur 1.21.8 build 2497 | Tested core | Java 21 |
| Paper 26.2 build 62 beta | Tested core, experimental release line | Java 25 |
| Purpur 26.2 build 2610 | Tested core, experimental release line | Java 25 |
| MythicMobs 5.12.1 on 26.2 | Not supported in tested combination | Mythic fails its own NMS/version initialization |

One Java 21 artifact uses public Bukkit/Paper APIs and no NMS. Later untested builds are compatible
by design only, not claimed as tested. Paper/Purpur 26.2 manual deployment remains in the gate.

## 17. Public documentation

Reviewed README, changelog, license, compatibility, configuration, examples, manual checklist,
migration, MythicMobs, PlaceholderAPI, placeholders, survivor rewards, test matrix, release
notes, bilingual README, and every distributed example. Commands, permissions, 41 placeholders,
optional dependencies, migration, tested-version limitations, and extraction scope agree with
code. Only `README.txt` and `EXAMPLES/` are extracted; full Markdown docs stay in GitHub.

## 18. License and attribution

Root zlib license and original attribution are unchanged and copied into the JAR. SnakeYAML 2.6's
Apache 2.0 text and a third-party notice are included. **Legal blocker:** inherited
`libs/SQLAccess.jar` has no embedded license/notice or separate provenance document. It came with
the original BloodMoon source tree, but the audit does not invent a legal interpretation; obtain
rights-holder confirmation before RC.

## 19. Security and privacy

- Fixed: SnakeYAML 1.23 unrestricted construction; now 2.6 `SafeConstructor`, duplicate/recursive
  keys disabled. This addresses the unsafe-construction class described by CVE-2022-1471.
- Fixed: configured-command failure logs no longer print administrator-supplied arguments/tokens.
- Fixed: crash markers no longer persist player UUID lists.
- Confirmed: no filesystem path is constructed from player input; no path traversal was found.
- Confirmed: configurable commands execute through Bukkit dispatcher/player API, never an OS shell.
- Confirmed: async PlaceholderAPI consumers read immutable snapshots only.
- Confirmed: PAPI publisher, Mythic tracking, boss bars, session/statistics services, and plugin
  tasks are canceled/closed on disable; Mythic-only boss shutdown was corrected.
- False positive: binary `token` matches were SnakeYAML token-parser class names, not credentials.

## 20. Code findings

| Finding | Classification | Result |
| --- | --- | --- |
| Unsafe YAML construction/SnakeYAML 1.23 | Release blocker | Fixed and tested |
| Internal testsuite packaged | Correct before RC | Removed and tested |
| Boss lifecycle UUID retention/Mythic-only close | Correct before RC | Fixed and tested |
| Player UUIDs in crash markers | Privacy correction | Fixed and tested |
| Full configured command in failure logs | Security correction | Fixed and tested |
| SQLAccess license metadata absent | Release blocker | Pending external confirmation |
| Legacy `System.out`/`printStackTrace` | Future improvement | 18 occurrences; no temporary debug text |
| `completedSessions` uptime set | Future improvement | One UUID/completed event until restart |

No duplicate plugin classes, dead test executor, obvious unused release module, or temporary
developer logger remains.

## 21. TODO/FIXME/deprecated

No exact TODO, FIXME, HACK, or XXX comment was found. No project `@Deprecated` or
`@SuppressWarnings` annotation was found. Java compilation reports legacy deprecated API and
unchecked-operation notes; the transitive Paper graph also names a deprecated Bungee chat
artifact. These are future cleanup items because compilation and runtime validation pass and no
removed API is used on the tested matrix.

## 22. Automated tests

`gradlew clean test --no-daemon` on Temurin 21.0.11: **130 tests, 0 failures**. Coverage includes
config/locale migration, YAML/examples, sessions/rewards, real-time and historical statistics,
boss policy/lifecycle/bar, all 41 placeholders, async snapshot concurrency, optional integration
architecture, artifact contents, distribution, offline/null players, and command log redaction.

## 23. Reproducible builds

Two independent `gradlew clean releaseArtifacts --no-daemon` executions produced the same
filename, 559,077-byte size, SHA-256, and a byte-for-byte array comparison of `True`. A final
post-redaction rerun retained reproducible build settings (`preserveFileTimestamps=false`,
`reproducibleFileOrder=true`).

## 24. Clean installation

Paper 1.21.8 build 60, Java 21.0.11, new path containing spaces, only BloodMoon:

- first start reached `Done`, reported 1.1.0/credits/website, reload/status succeeded, clean stop;
- created README plus exactly 17 examples, no `docs/` and no Markdown;
- second start kept all 18 files byte-identical to JAR and stopped without selected errors;
- integration run loaded BloodMoon, PAPI 2.12.3, MythicMobs 5.12.1, TAB 5.3.2, registered the
  BloodMoon expansion once, reloaded TAB/BloodMoon, and stopped with zero selected error matches.

## 25. Phase-1 pending manual tests snapshot

At the close of Phase 1, the following were still pending: real survivor reward, dead-player exclusion, vanilla boss reward, Mythic boss
reward, duplicate prevention, completed-event history, history after restart, graphical TAB
review, complete English review, complete Spanish review, Paper/Purpur 26.2 deployment walkthrough,
and migration from an unmodified user-owned 1.0.1 installation. Preparation, steps, expected
result, evidence, and PASS/FAIL criteria are explicit in `MANUAL_TEST_CHECKLIST.md`. This historical
list is superseded by the Phase-2 addendum, including the owner's manual PASS results.

## 26. Blockers

1. Confirm the distribution license/provenance of inherited `libs/SQLAccess.jar`.
2. Execute and retain evidence for the mandatory manual matrix.
3. Do not claim MythicMobs 5.12.1 support on 26.2; use a supported Mythic build and revalidate.

## 27. Corrections made

Safe YAML parsing and SnakeYAML upgrade; release licenses/notices; removal of test command/classes
and unnecessary annotation shading; descriptor metadata; boss lifecycle cleanup; metadata-only
crash markers; configured-command argument redaction; regression tests; public docs/matrix/report.

## 28. New commits

- `9e8e8c4 fix: harden release artifact and YAML parsing`
- `47fd3ae fix: release completed boss lifecycle state`
- `3923b3f fix: minimize incomplete session markers`
- `f66d160 fix: redact configured command arguments in logs`
- final documentation audit commit (the commit containing this file)

## 29. JAR path

`<project-root>/build/libs/BloodMoon-Reborn-1.1.0.jar`

## 30. Size

559,077 bytes.

## 31. SHA-256

`576f0807753ecbf8e42e1a7ed6630e01f47ade68e4274d659712e77132f491f5`

## 32. Final status

**NOT READY FOR RELEASE CANDIDATE**

## 33. Exact reason

All automated, packaging, reproducibility, clean-install, and optional-integration startup gates
pass after the isolated fixes. Promotion is still blocked because the inherited SQLAccess binary
has no independently confirmed license metadata and because the project's own release gate
requires real-player reward/history/language/TAB/migration evidence that has not been executed.
No unexecuted test is represented as passed.

## Phase-2 addendum — 2026-07-24

The Phase-1 SQLAccess blocker is closed as **A. REDISTRIBUTION CONFIRMED**: Git history preserves
the exact 5,474-byte helper in the original BloodMoon source archive, that archive carries the
original author's zlib license, and the author's public SQLAccess resource explicitly instructs
plugin developers to include it in the final JAR. It is unmodified, runtime-shaded, and used by
`Bloodmoon` and `PeriodicNightCheck`.

Functional protocol-client testing passed survivor exclusions, vanilla boss rewards, English
Mythic reward isolation, historical snapshots, and a tag-built 1.0.1 migration. Commit `2cd68c1`
fixes a reproduced literal legacy color code in survivor messages. The exact evidence index is
`RELEASE_VALIDATION_EVIDENCE_1.1.0.md`.

The project owner additionally confirmed earlier real in-game PASS results for vanilla and
MythicMobs scoreboards, visible boss states/updates, final rewards, configured end commands, and
normal event closure. They are classified **PASS — owner verified, evidence pending** because no
captures, logs, versions, language, hashes, or exact configuration were supplied.

Status remains **NOT READY FOR RELEASE CANDIDATE**. Same-night restart creates a new empty session
before reconnecting players can register when late joiners are disabled. Separate English and
Spanish review and the full Phase-2 26.2 lifecycle are also NOT RUN. The owner-verified shared
scoreboard and normal-completion behavior is not classified as NOT RUN.
