# Manual release checklist

Use disposable copies of the production server and plugin data. Preserve each `latest.log`, the before/after `config.yml`, `sessions.yml`, and any migration backup as release evidence. Do not use synthetic results to sign off a real administrative migration.

## 1. Real 1.0.1 migration

1. Stop the disposable server and copy an unmodified, user-owned BloodMoon 1.0.1 world configuration into `plugins/BloodMoon/<world>/config.yml`.
2. Record its SHA-256, start once, and confirm exactly one `config.yml.bak-<timestamp>` exists with the original SHA-256.
3. Confirm legacy values are unchanged and exactly one `ConfigVersion`, `SurvivorRewards`, and `Boss` section exists.
4. Restart twice and run `/bloodmoon reload`; confirm no additional migration backup or duplicated section appears.
5. Stop cleanly and retain the files and log.

## 2. Survivor eligibility with two players

Enable a harmless, uniquely identifiable survivor command and set a short minimum participation time. Use players A and B in the same normal world.

1. Start a BloodMoon and confirm OnStart runs once.
2. Keep A alive and present past the minimum time.
3. Kill B during the event; confirm B remains ineligible after respawn.
4. Have A leave the world and return, then disconnect and reconnect; confirm accumulated eligible time follows the UUID without resetting or duplicating.
5. Join player C after the event started; confirm late-join behavior matches the configured policy.
6. Reload once from console and once from a permitted player while the session is active; confirm the session UUID and participant state remain stable and no lifecycle command repeats.
7. End the BloodMoon. Confirm A receives exactly one reward, B receives none, and C receives only the policy-appropriate result.
8. Repeat with two simultaneous worlds and stop only one; confirm participants and rewards do not cross worlds.

## 3. Vanilla boss rewards

Set `Boss.Mode: VANILLA`, enable a uniquely identifiable boss reward command, and disable unrelated rewards.

1. Run `/bloodmoon spawnzombieboss` with a player present; confirm `ZombieBossName`, exactly one vanilla BossBar, and no Mythic entity.
2. Damage the boss and confirm the bar decreases; defeat it normally and confirm only the credited killer receives exactly one command reward and the bar disappears.
3. Repeat with environmental/no-player final damage; confirm the configured no-killer behavior and no console exception.
4. Exercise cleanup (`bloodmoon killbosses`, event end, and plugin disable) and confirm no reward is issued for administrative removal.
5. Inspect the log for duplicate death processing and verify the reward cannot run twice for the same boss UUID.
6. Test `NEARBY`, `WORLD`, and `ALL`; for `NEARBY`, cross the configured radius and confirm the audience refreshes without creating another bar.
7. Reload repeatedly, disable/re-enable the bar through config plus reload, end the event, unload the disposable world, and stop the plugin. Confirm no duplicate or orphan bar remains.

## 4. MythicMobs rewards and fallback

Use a MythicMobs build that officially supports the exact server version. MythicMobs 5.12.1 is suitable for the validated Paper 1.21.8 bridge test but not for the tested Paper 26.2 build.

1. Install `MythicMobs-BloodMoonBoss.yml`; choose `BloodMoonBoss_EN` or `BloodMoonBoss_ES`, set `Boss.Mode: MYTHICMOBS`, `Enabled: true`, and its exact `InternalName`.
2. Run `/bloodmoon spawnzombieboss`. Confirm only the MythicMob appears, the arrival uses its display (never `ZombieBossName`), and no BloodMoon vanilla BossBar exists while damaging it.
3. With `UseMythicMobsRewards: true` and `RunBloodMoonRewardCommands: false`, kill the boss. Confirm the death name is correct, Mythic drops/skills run once, BloodMoon boss reward commands do not run, and no vanilla bar/state remains.
4. Enable `RunBloodMoonRewardCommands`, repeat with a credited killer, and confirm the additional BloodMoon command runs exactly once without duplicating ordinary enhanced-mob drops.
5. End an event, run `killbosses`, and disable the plugin with tracked Mythic bosses; confirm every exact entity is removed and no reward is granted.
6. Set an unknown `InternalName` and `FallbackToVanilla: true`; run the command and confirm one vanilla entity, one vanilla announcement, and one functional vanilla BossBar. Repeat with fallback `false`; confirm no entity/bar and one clear failure response.
7. Remove MythicMobs completely and restart. Confirm BloodMoon enables, reloads, follows the configured fallback policy, and stops without `NoClassDefFoundError` or linkage errors.

## 5. Languages and legacy locales

1. On a new disposable installation, select `Language: en`, reload, and exercise status, warnings, boss, survivor, and migration messages.
2. Select `Language: es`, reload, and repeat. Confirm accents, opening punctuation, color codes, and placeholders render correctly.
3. Remove one noncritical key from a copy of `locales/es.yml`; confirm the English value is used and no exception occurs.
4. Install an old customized `locales.yml` containing `ZombieBossName: "el duro"`. Start once and confirm a timestamped backup, preserved custom value, and appended missing keys.
5. Restart and reload twice; confirm no second migration write or backup.
6. Spawn vanilla and Mythic bosses in turn. Confirm vanilla uses `el duro`, while Mythic uses its own `Display`.

## 6. PlaceholderAPI and TAB

Use Paper/Purpur 1.21.8, PlaceholderAPI, and a real connected player. TAB is optional and must be installed separately when validating the scoreboard example.

1. Without PlaceholderAPI installed, start BloodMoon-Reborn, run reload/status, and stop; confirm no missing-class error or unnecessary warning.
2. Install PlaceholderAPI, restart, and run `/papi list`; confirm the internal `bloodmoon` expansion appears once.
3. Outside an event, run every command in `docs/examples/PlaceholderAPI-examples.txt`; confirm `boss_state=NONE`, numeric zeroes, localized no-history/no-boss values, and `00:00`.
4. Start a Blood Moon in the player's world and repeat; move a second player to another world and confirm active/time/session values remain world-contextual.
5. Before spawning, confirm `boss_state=NOT_SPAWNED`, `boss_alive=false`, type `NONE`, and zero health.
6. Spawn a vanilla boss, confirm `ALIVE`, parse its name/type/health, damage it, parse again, kill it, and confirm `DEFEATED`, retained name/type, and zero health.
7. Repeat with a configured MythicMob; confirm `ALIVE`, its resolved display and Bukkit health, `MYTHICMOBS`, and no BloodMoon vanilla BossBar; after death confirm `DEFEATED`.
8. Confirm participation seconds and all four live session counters update immediately and remain isolated in a second world.
9. End normally; confirm all last-event fields and totals update once. Restart and confirm the values persist.
10. Run `/bloodmoon reload` during another active session; confirm state/counters remain unchanged and the completed-event total does not increment.
11. Run `/papi reload`; confirm `%bloodmoon_active%` still resolves because the expansion persists.
12. Corrupt a disposable copy of `statistics.yml`, restart, and confirm one `statistics.corrupt-*.yml` copy, a clear warning, safe defaults, and successful enable.
13. Remove PlaceholderAPI and restart; confirm BloodMoon lifecycle, boss state tracking, statistics writes, reload, and shutdown still work.
14. Parse `boss_display_line_1`, `_line_2`, and `_line_3` in `NONE`, `NOT_SPAWNED`,
    `ALIVE`, and `DEFEATED`; confirm 0/1/3/2 non-empty localized lines, resolved values, and
    legacy `&` color codes.
15. With TAB's asynchronous refresh enabled, keep the Blood Moon scoreboard visible for at least
    two minutes. Damage and heal vanilla and Mythic bosses, then search the complete `latest.log`
    for `AsyncCatcher`, `Chunk getEntities`, `Placeholder Refreshing Thread`, `ERROR`, and
    BloodMoon warnings. Do not approve this row unless the exact target TAB and PlaceholderAPI
    versions were used with a connected player.

## 7. TAB visual validation

Use exactly one functional variant selected through
[`examples/TAB-scoreboards.yml`](examples/TAB-scoreboards.yml): `TAB-scoreboards-en.yml` or
`TAB-scoreboards-es.yml`. Merge it into an existing TAB configuration without duplicate global
sections. Neither variant requires `%condition:*%` placeholders.

1. Run `/tab reload` and confirm no parse/condition warnings.
2. Start an event and confirm the Blood Moon design appears.
3. With no boss, confirm only the configured localized “not spawned” text appears—never type `NONE` or health `0%`.
4. Spawn a boss and confirm name, type, and live health appear.
5. Defeat it and confirm retained name plus localized defeated status, with no `0%` health line.
6. Confirm the chosen design stays within 15 visible lines and inspect flicker/line transitions with a real client.

## 8. Bundled administrator files

1. Start a clean Paper/Purpur 1.21.8 server with only the final BloodMoon JAR.
2. Confirm `README.txt` and all 17 `EXAMPLES/` files appear beneath `plugins/BloodMoon/`.
3. Confirm `plugins/BloodMoon/docs/` is not created and no Markdown document is extracted.
4. Confirm README has separate `ENGLISH` and `ESPAÑOL` sections and the official GitHub URL.
5. Confirm all five indexes are comments-only and every `-en.yml`/`-es.yml` variant is UTF-8 YAML.
6. Restart and confirm no distributed file is rewritten or retimestamped.
7. Edit one extracted file, restart, and confirm its bytes and timestamp remain unchanged.
8. Remove one example, restart, and confirm only that missing example is restored.
9. Create a legacy `docs/custom-guide.md`, restart, and confirm it remains byte-identical.
10. Confirm the first-start log mentions `EXAMPLES/` and the GitHub URL but never `docs/`.
11. Repeat from a server path containing spaces.
12. Obstruct one example target; confirm a clear warning and normal plugin enable.

## Sign-off

Release status may change from `NOT READY` only after the real player tests above pass, the deployment server/Mythic combination enables, and the collected logs show no duplicate reward, cross-world leakage, unsafe crash payout, class-link error, or shutdown exception.

## Exact pending release-candidate matrix

Every row below is still pending unless signed evidence is attached. “PASS” requires the expected
result and all requested evidence; any missing evidence, duplicate action, exception, leakage, or
different result is “FAIL”.

| Test | Preparation | Steps | Expected result | Required evidence | PASS / FAIL |
| --- | --- | --- | --- | --- | --- |
| Real survivor reward | Two real players; harmless unique reward marker; rewards enabled | Start event; keep A eligible past minimum; complete normally | A receives exactly one marker | Config, session UUID, chat/console output, complete log | PASS only once for A; FAIL for zero/multiple |
| Dead-player exclusion | Same event with player B | Kill B; respawn; complete event | B remains ineligible and receives nothing | Death line, survivor status before end, reward log | PASS only with no B reward; otherwise FAIL |
| Vanilla boss reward | `Boss.Mode: VANILLA`; unique reward command | Spawn, damage, kill with credited player | One vanilla boss/bar and one killer reward | Config, boss UUID, before/after screenshots, log | PASS exactly once; duplicate/missing/wrong player is FAIL |
| MythicMobs boss reward | Paper 1.21.8 + supported Mythic 5.12.1; documented mob | Spawn and kill with each reward-isolation policy | Mythic and BloodMoon rewards follow their independent flags exactly once | Exact plugin hashes, mob/config YAML, log, inventory/command marker | PASS only with correct isolation; otherwise FAIL |
| Duplicate prevention | Enable unique markers; reuse each preceding flow | Reload during session; repeat damage/death/end triggers | No session or boss UUID produces a second reward | Full unfiltered log plus marker counts | PASS at count 1; any count >1 is FAIL |
| Completed-event history | Clean `statistics.yml`; one controlled event | Record baseline; complete event; inspect file/placeholders | Totals and last-event snapshot increment exactly once | Before/after file hashes/content and PAPI parses | PASS exact delta 1; otherwise FAIL |
| History after restart | Continue previous test | Stop cleanly; restart; parse history again | Same completed aggregates survive with no extra increment | Shutdown/startup log, file, PAPI output | PASS identical values; loss/duplication is FAIL |
| TAB graphical review | Real graphical client; exact EN or ES example | Reload TAB; observe no boss, alive, damaged, defeated states | Correct lines, ≤15 visible, no stale health/`NONE`, acceptable transitions | Screenshots/video and complete log | PASS only visual and log criteria; otherwise FAIL |
| English language | `Language: en`; fresh and migrated copies | Exercise lifecycle, warnings, boss, survivor, PAPI states | Natural English, resolved tokens, valid colors | Screenshots/chat export and log | PASS with no unresolved/missing text; otherwise FAIL |
| Spanish language | `Language: es`; fresh and migrated copies | Repeat English sequence | Correct accents/punctuation, resolved tokens, valid colors | Screenshots/chat export and UTF-8 log | PASS with no mojibake/unresolved text; otherwise FAIL |
| Paper/Purpur 26.2 deployment | Disposable exact deployment builds; Java 25 | Start, reload/status, event lifecycle, stop; test supported integrations only | Core loads and stops cleanly; unsupported Mythic 5.12.1 is not claimed | Exact builds/hashes and complete logs | PASS per platform only with clean core lifecycle; otherwise FAIL |
| Real 1.0.1 migration | Unmodified user-owned 1.0.1 data and backup | Hash input; start once; restart twice; reload | One backup, preserved values, one 1.1 section, idempotent later starts | Original and backup hashes, before/after config, all logs | PASS only if all preservation/idempotency checks pass; otherwise FAIL |
