# BloodMoon-Reborn 1.1.0 functional validation evidence

Validation date: 2026-07-24. Branch:
`feature/1.1.0-multiversion-rewards-mythicmobs`.

The ignored local evidence root is `exes/manual-validation-1.1.0/evidence/`. It is intentionally
not committed because it contains logs, inventories, world data, databases, and offline test-player
identifiers. Functional server scenarios used `BloodMoon-Reborn-1.1.0.jar`, 559,198 bytes,
SHA-256 `3f2faaab950829f1c08e222eae780b1e37b40f5076fac71b132208c1963e09f8`.
Afterward, only the packaged SQLAccess notice/documentation changed. The final rebuilt artifact is
559,407 bytes, SHA-256 `208060c36f395fea76a58ac8fa046bee9d221a610a84e5a837d604bda86ba38e`;
it passed all 131 automated tests. No functional bytecode changed between those two artifacts.

Phase 2.1 used the corrected final bytecode on Paper 1.21.8 build 60 with Java Temurin 21.0.11 and
PlaceholderAPI 2.12.3. Its JAR is 568,928 bytes with SHA-256
`08984085dddd33b3d86704c4303780501918b994af3dc8f1449c48051b64646d`; final reproducibility is
recorded in the RC audit. Ignored evidence is under `exes/rc-blocker-*`.

## Executed scenarios

| Scenario | Result | Evidence |
| --- | --- | --- |
| Survivor rewards, two protocol clients, six event attempts | PASS with one documented restart failure | `rewards-history/result.json`, snapshots `00` through `08`, complete console log |
| First event: survivor/death/vanilla boss | PASS | Eligible player received one diamond; dead player received zero; one death and boss defeat recorded |
| Late joiner disabled | PASS | Late player received zero |
| Disconnect/reconnect | PASS | Reconnected registered player received one |
| Other world / offline at end | PASS | Each ineligible player received zero |
| Active-event shutdown | PASS | No reward was delivered and the incomplete marker was discarded |
| Fresh event immediately after nighttime restart | FAIL before fix; PASS after Phase 2.1 | Pre-fix evidence preserves the empty session; corrected runs suppress it for both late-joiner values |
| Configurable reward message colors | FAIL then fixed | Live chat exposed literal `&a`; commit `2cd68c1` translates legacy colors; automated regression passes |
| Vanilla boss lifecycle and rewards | PASS | One server execution line, exactly three emeralds to credited killer, zero to second attacker; administrative removal and live-at-end produced zero |
| MythicMobs 5.12.1 English lifecycle | PASS | NOT_SPAWNED → ALIVE → DEFEATED, health/damage/heal updates, exactly three Mythic-owned diamonds, no BloodMoon command, no vanilla fallback, next event clean |
| Mythic attempts with nonlethal/ground-drop harnesses | NOT APPLICABLE to product result | Preserved under the Mythic evidence directory; causes were test-fixture health mitigation and pickup distance |
| Historical statistics | PASS with restart caveat above | Consecutive completed events remained separated; before/after `statistics.yml` and empty completed `sessions.yml` snapshots retained |
| Real tag-built 1.0.1 migration | PASS | 1.0.1 and two 1.1.0 boot logs, before/after files, SQL cache backup, unified diffs |
| TAB scoreboard with vanilla boss | PASS — owner verified, evidence pending | Owner confirmed visible boss state and live updates in a real in-game test; version, language, captures, and logs were not supplied |
| TAB scoreboard with MythicMobs boss | PASS — owner verified, evidence pending | Owner confirmed visible boss state and live updates in a real in-game test; version, language, captures, and logs were not supplied |
| Final Blood Moon rewards | PASS — owner verified, evidence pending | Owner confirmed final reward delivery at normal event completion; recipient matrix and artifacts were not supplied |
| Configured commands at event end | PASS — owner verified, evidence pending | Owner confirmed configured end commands executed; exact configuration and logs were not supplied |
| General event completion | PASS — owner verified, evidence pending | Owner confirmed the normal closing flow worked; restart/crash behavior is outside this verification |
| TAB English-specific language/layout review | NOT RUN | The owner verification did not identify the language or provide the EN-specific visual checklist |
| TAB Spanish-specific language/layout review | NOT RUN | The owner verification did not identify the language or provide the ES-specific visual checklist |
| Full Spanish Mythic/player walkthrough | NOT RUN | Prepared but not executed |
| Paper/Purpur 26.2 Phase-2 functional walkthrough | NOT RUN | Earlier core startup evidence remains valid; no new full Phase-2 lifecycle was executed |

## Environment

- Paper 1.21.8 build 60, Java Temurin 21.0.11.
- PlaceholderAPI 2.12.3.
- MythicMobs 5.12.1 build `46bae256`, only on Paper 1.21.8.
- TAB 5.3.2 was prepared locally. Separate earlier in-game vanilla/Mythic scoreboard behavior is
  owner-verified; formal evidence and language-specific EN/ES review remain pending.
- Economy/Vault: NOT APPLICABLE. Version 1.1.0 implements command rewards; economy is available
  only through an administrator command supplied by another plugin and has no native Vault balance API.

## Migration assertions

The 1.0.1 JAR was built from tag `v1.0.1` (SHA-256
`3912af4153d80ca5d48e729e05e4612458fd5c151c51afdc3df4e55b7f129aa2`). The
migration preserved `BloodMoonInterval: 9`, disabled sound/spawn/horde values, `cache.db`, locales,
and a pre-existing `docs/legacy-sentinel.txt`. It created one backup, one `ConfigVersion`, one
`SurvivorRewards`, one `Boss`, `README.txt`, `EXAMPLES/`, `statistics.yml`, and `sessions.yml`.
The second 1.1.0 boot performed no second migration and `/bloodmoon reload` succeeded.

## Owner verification versus pending evidence

The project owner reports real in-game PASS results, performed before formal 1.1.0 preparation,
for vanilla and MythicMobs scoreboards, visible boss states/updates, final rewards, configured end
commands, and normal event closure. These are behavioral PASS results, not NOT RUN. Because no
captures, logs, versions, language, hashes, or exact configuration were supplied, documentary
evidence remains pending and none of those details is inferred here.

This verification does not cover dead-player exclusion, duplicate prevention, history after
restart, 1.0.1 migration, separate English/Spanish review, or Paper/Purpur 26.2. Those retain the
results from the formal matrix.

## Remaining language-specific graphical capture procedure

Use a graphical 1.21.8 client in each prepared `tab-en` and `tab-es` server, never both example
files at once. Capture: inactive, active/NOT_SPAWNED, ALIVE full health, ALIVE partial health,
DEFEATED, participant, dead participant, nonparticipant, and event end. Each capture must include
the scoreboard and chat language; retain `latest.log` and reject unresolved placeholders, flicker,
duplicates, mojibake, bad alignment, or stale boss lines. Until those captures exist, both rows
remain NOT RUN for the language-specific review. This does not revoke the owner's PASS for the
shared vanilla/Mythic scoreboard behavior.

## Release conclusion

SQLAccess is classified **A. REDISTRIBUTION CONFIRMED**. The candidate is nevertheless
**NOT READY FOR RELEASE CANDIDATE** because the separate EN/ES locale review and the full 26.2
Phase-2 walkthrough remain unexecuted. The automatic same-night post-restart functional blocker is
fixed and validated. Owner-verified scoreboard, final-reward, end-command, and normal-close
behavior remains PASS with evidence pending.

## Phase-2.1 restart recovery evidence

The pre-fix run preserved configuration, complete logs, both session markers, caches, statistics,
timestamps, world UUID, world time, and assertions. It proved that `PeriodicNightCheck` restored
`days=0/checkAt=0`, then opened a different empty session before the player returned.

Corrected `IncludeLateJoiners: false` evidence contains nine state snapshots over three boots. All
14 assertions pass: no empty same-night session, repeated suppression, manual override, zero abort
payout, one next-cycle completion/reward, one history increment, marker expiry, and one recovery
message per boot. The equivalent true configuration passes all 11 assessed assertions. No player
UUID or reward state is stored in `aborted-nights.yml`.

The additional Mythic-enabled recovery smoke confirmed suppression before and after a manually
aborted event, no completed-history increment, no reward command, and no residual boss state. Its
fixture fell back to vanilla because the copied Mythic definition was not resolved in that isolated
copy, so it is not represented as a new Mythic spawn PASS; the earlier real Mythic lifecycle PASS
and the automated Mythic cleanup regressions remain the applicable evidence.
