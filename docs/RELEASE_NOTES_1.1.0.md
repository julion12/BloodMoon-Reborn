# BloodMoon-Reborn 1.1.0 release notes

This incremental release adds configurable lifecycle and reward commands, UUID-based survivor eligibility, command rewards for the existing vanilla boss, and an optional MythicMobs boss mode. All reward features are disabled by default, old per-world keys remain valid, and MythicMobs remains optional.

Upgrade by stopping the server, backing up `plugins/BloodMoon/`, replacing the JAR, and starting with Java 21 (Minecraft 1.21.x) or Java 25 (Minecraft 26.1+). Review the automatically appended sections before enabling rewards.

Validation completed against Paper 1.21.4, 1.21.8, 1.21.11, and 26.2 plus Purpur 1.21.8 and 26.2. Core load, commands, lifecycle, reload, crash recovery, and two-world isolation passed. See `docs/TEST_MATRIX.md` for exact builds and evidence.

Known release gate: **NOT READY for publication.** Player-driven survivor/boss rewards and an actual Mythic boss death still require live manual execution. MythicMobs 5.12.1 enables with Paper 1.21.8, but fails inside its own server-version/NMS initialization on the tested Paper 26.2 build; BloodMoon core remains operational without it. Complete `docs/MANUAL_TEST_CHECKLIST.md` before publishing.
