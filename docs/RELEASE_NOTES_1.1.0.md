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

Known release gate: **NOT READY FOR RELEASE CANDIDATE.** Player-driven survivor/boss rewards,
completed-event history, graphical TAB rendering, and a real 1.0.1 administrative migration still
require manual execution. MythicMobs 5.12.1 enables with Paper 1.21.8, but fails inside its own
server-version/NMS initialization on the tested Paper 26.2 build; BloodMoon core remains
operational without it. The inherited `libs/SQLAccess.jar` also contains no separate license
metadata, so its distribution status needs confirmation from the rights holder before an RC is
declared.
