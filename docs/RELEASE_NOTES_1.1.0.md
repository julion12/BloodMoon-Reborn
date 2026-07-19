# BloodMoon-Reborn 1.1.0 release notes

This incremental release adds configurable lifecycle and reward commands, UUID-based survivor eligibility, command rewards for the existing vanilla boss, and an optional MythicMobs boss mode. All reward features are disabled by default, old per-world keys remain valid, and MythicMobs remains optional.

Upgrade by stopping the server, backing up `plugins/BloodMoon/`, replacing the JAR, and starting with Java 21 (Minecraft 1.21.x) or Java 25 (Minecraft 26.1+). Review the automatically appended sections before enabling rewards.

Known release gate: unit/build verification passes, but official Paper downloads timed out in the build environment. Complete the manual server matrix in `docs/TEST_MATRIX.md` before publishing.
