# BloodMoon-Reborn 1.1.0 release notes

This incremental release adds configurable lifecycle and reward commands, UUID-based survivor eligibility, command rewards for the existing vanilla boss, an optional MythicMobs boss mode, a vanilla-only health bar, complete English/Spanish catalogs, per-session live statistics, narrative boss state, and minimal server-wide history. Mythic messages use the Mythic mob's visible display instead of `ZombieBossName`. All reward features are disabled by default, old per-world and locale keys remain valid, and MythicMobs, PlaceholderAPI, and TAB remain optional.

Upgrade by stopping the server, backing up `plugins/BloodMoon/`, replacing the JAR, and starting with Java 21 (Minecraft 1.21.x) or Java 25 (Minecraft 26.1+). Review the automatically appended sections before enabling rewards.

Validation completed against Paper 1.21.4, 1.21.8, 1.21.11, and 26.2 plus Purpur 1.21.8 and 26.2. Core load, commands, lifecycle, reload, crash recovery, and two-world isolation passed. See `docs/TEST_MATRIX.md` for exact builds and evidence.

The optional PlaceholderAPI expansion now exposes 41 O(1) identifiers, including live
world/session counters, `boss_state`, three localized direct boss display lines, and the immutable
historical snapshot. Persistent aggregates live in `plugins/BloodMoon/statistics.yml`; writes use a
temporary file and atomic replacement, and corrupt input is preserved before safe defaults are
restored. No individual player history is stored. Incomplete-session crash markers contain event
metadata only and never player UUID, death, or reward lists.

PlaceholderAPI requests are safe for asynchronous consumers. A single synchronous publisher
captures player, world, session, and live boss health data every 5 ticks and atomically replaces
immutable per-player contexts. TAB and other consumers perform only a constant-time UUID lookup;
they never trigger entity, chunk, BossBar, MythicMobs, configuration, or file access.

Ready-to-copy administrative examples are indexed in [`docs/EXAMPLES.md`](EXAMPLES.md). On first
startup, the self-contained JAR creates only `plugins/BloodMoon/README.txt` and the 17 files in
`EXAMPLES/`, without overwriting existing files. Complete documentation stays in the official
GitHub repository; the plugin does not create, update, or delete `plugins/BloodMoon/docs/`.
The TAB English and Spanish variants use direct dynamic boss lines and require no global TAB
condition definitions.

The final audit upgraded the shaded YAML reader to SnakeYAML 2.6 with safe construction, removed
the internal test command from production, added required license texts, bounded completed boss
lifecycle tracking, and redacted administrator command arguments from failure logs.

Phase-2 validation executed real protocol-client survivor rewards, vanilla and Mythic boss
rewards, consecutive history, shutdown handling, and a tag-built 1.0.1 migration. SQLAccess
redistribution was confirmed from the original exact-binary source archive, its zlib license, and
the author's public resource instructions. One reward-message color bug was fixed.

Restart policy is now explicit: an active Blood Moon interrupted by shutdown, restart, or a
recoverable crash is definitively aborted. It is not resumed and pays no survivor or pending boss
reward. A small internal per-world/night marker prevents another automatic event during that same
night; it expires at the next cycle. An administrator may still use `/bloodmoon start` to create a
fresh normal session.

The reproduced same-night restart blocker is closed on Paper 1.21.8 build 60 with both
`IncludeLateJoiners` values. The project owner separately verified real in-game vanilla/Mythic
scoreboards, boss-state updates, final rewards, end commands, and normal event closure; formal
evidence is still pending. Release-candidate promotion remains **NOT READY** only because the
separate English/Spanish review and full Paper/Purpur 26.2 Phase-2 lifecycle are still NOT RUN.
MythicMobs 5.12.1 remains unsupported on 26.2.
