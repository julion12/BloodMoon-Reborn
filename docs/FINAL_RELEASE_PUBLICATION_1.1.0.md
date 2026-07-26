# BloodMoon-Reborn 1.1.0 final publication package

Status: **READY FOR FINAL RELEASE**

This document contains copy-ready publication material. It does not authorize or perform a merge,
push, tag, GitHub Release, Spigot update, or any other publication action. There will be no public
release-candidate version; publication is planned directly as stable `v1.1.0`.

## Definitive artifact

- File: `BloodMoon-Reborn-1.1.0.jar`
- Size: 568,988 bytes
- SHA-256: `d28628a57257494c904a73943cadf0f1b170a8dea9cd6940b58a855724b33c05`
- Minimum runtime: Java 21

## GitHub Release v1.1.0

### Title

`BloodMoon-Reborn v1.1.0`

### Description

BloodMoon-Reborn 1.1.0 expands the existing per-world Blood Moon experience with optional survivor
and boss rewards, MythicMobs boss support, a configurable vanilla BossBar, complete English and
Spanish localization, PlaceholderAPI/TAB integration, live per-session statistics, and compact
server-wide history.

Existing worlds and 1.0.1 configurations remain supported. New reward systems are disabled by
default so administrators can review every configured command before enabling payouts.

### Highlights

- Optional survivor command rewards with UUID-based eligibility and death disqualification.
- Optional, exactly-once command rewards for the credited vanilla boss killer.
- Optional MythicMobs boss mode with independent MythicMobs and BloodMoon reward authority.
- Configurable vanilla BossBar with live health and `NEARBY`, `WORLD`, or `ALL` audiences.
- Complete English and Spanish locale catalogs with English fallback and legacy overrides.
- Optional PlaceholderAPI expansion with 41 constant-time `%bloodmoon_*%` identifiers.
- Real-time per-world session counters for deaths, unique deaths, participants, and survivors.
- Narrative boss states: `NOT_SPAWNED`, `ALIVE`, and `DEFEATED`.
- Atomic aggregate history and last-completed-event snapshot in `statistics.yml`.
- Safe abort and same-night restart suppression for interrupted events.
- Bilingual README plus 17 copy-ready administrative examples installed without overwriting edits.

### Breaking changes

None. No existing configuration key, command, permission, or vanilla boss default was removed or
renamed. The historical `/bloodmoon spawnzombieboss` command keeps its name and permission while
honoring the configured boss mode.

### Compatibility

| Platform | Runtime | Status |
| --- | --- | --- |
| Paper 1.21.4, 1.21.8, 1.21.11 | Java 21 | Supported and validated |
| Purpur 1.21.8 | Java 21 | Supported and validated |
| Purpur 26.2 | Java 25 | Full vanilla lifecycle validated |
| Paper 26.2 | Java 25 | Core startup, commands, reload, and shutdown validated |
| MythicMobs 5.12.1 on Paper 1.21.8 | Java 21 | Supported and validated |
| MythicMobs 5.12.1 on Minecraft 26.2 | Java 25 | Not supported |

BloodMoon uses Bukkit/Paper APIs and does not depend on NMS or CraftBukkit internals.

### Requirements

- Paper or Purpur 1.21.4 or newer.
- Java 21 for Minecraft 1.21.x.
- Java 25 for Minecraft 26.1 and newer.
- PlaceholderAPI, MythicMobs, WorldGuard, Multiverse-Core, and TAB are optional.
- TAB is a consumer configuration example, not a plugin dependency.

### Installation

1. Stop the server.
2. Back up the worlds and `plugins/BloodMoon/`.
3. Place `BloodMoon-Reborn-1.1.0.jar` in `plugins/`.
4. Remove any older BloodMoon JAR so only one version remains.
5. Start the server with the Java version required by the Minecraft version.
6. Review the generated README, examples, locale selection, and per-world configuration.
7. Keep reward commands disabled until every command has been reviewed.

### Upgrade from 1.0.1

1. Stop the server and back up `plugins/BloodMoon/` and all worlds.
2. Replace the 1.0.1 JAR with `BloodMoon-Reborn-1.1.0.jar`.
3. Start the server once.
4. BloodMoon creates a timestamped backup before adding missing 1.1.0 sections.
5. Existing values and legacy locale overrides are preserved.
6. Review `SurvivorRewards`, `Boss`, lifecycle commands, locale settings, and generated examples.
7. Run `/bloodmoon reload` only after the configuration has been reviewed.

Migration is idempotent: later starts do not duplicate sections or create another migration backup.

### Known limitations

- MythicMobs 5.12.1 does not support the tested Minecraft 26.2 server line. Use the vanilla boss
  there or install a MythicMobs version that explicitly supports the target server and revalidate it.
- The built-in Bukkit BossBar is for vanilla bosses. MythicMobs controls its own boss bars.
- External MythicMobs, economy, and configured-command messages use their own localization.
- An event interrupted by shutdown, restart, or recoverable crash is aborted and pays no uncertain
  survivor or pending boss reward. Automatic restart is suppressed for the remainder of that
  world/night cycle; an administrator can still start a fresh event explicitly.
- Historical storage contains aggregate server statistics and the last completed event, not
  individual player history.

### Credits

- Original BloodMoon author: **SpectralMemories**
- Modernization and maintenance: **JulioN12**
- SnakeYAML contributors
- PlaceholderAPI, MythicMobs, WorldGuard, Multiverse-Core, TAB, Paper, and Purpur projects

The original zlib license and third-party notices are included in the release JAR.

### Checksum

```text
d28628a57257494c904a73943cadf0f1b170a8dea9cd6940b58a855724b33c05  BloodMoon-Reborn-1.1.0.jar
```

## Spigot update for resource 135838

### Update title

`BloodMoon-Reborn 1.1.0 — Rewards, MythicMobs, BossBar, placeholders and EN/ES`

### Short description

Optional survivor and boss rewards, MythicMobs boss mode, vanilla BossBar, 41 PlaceholderAPI
identifiers, live statistics, history, and complete English/Spanish support.

### Update text

BloodMoon-Reborn 1.1.0 is a compatibility-focused update for Paper and Purpur. It preserves
existing worlds and configuration while adding optional reward systems, MythicMobs boss support,
a vanilla BossBar, PlaceholderAPI/TAB integration, live session statistics, aggregate history, and
complete English/Spanish localization.

All rewards remain disabled by default. Back up `plugins/BloodMoon/`, replace the old JAR, start
once, and review the new configuration sections before enabling reward commands.

### Long description

```text
[SIZE=5][B]BloodMoon-Reborn 1.1.0[/B][/SIZE]

Version 1.1.0 expands the existing per-world Blood Moon event without removing or renaming the
configuration and commands administrators already use.

[B]New in 1.1.0[/B]
[LIST]
[*]Optional survivor command rewards with UUID-based eligibility and death disqualification
[*]Optional exactly-once command rewards for the vanilla boss killer
[*]Optional MythicMobs boss mode with configurable vanilla fallback
[*]Configurable vanilla BossBar with live health and audience scopes
[*]Complete English and Spanish locale catalogs
[*]41 optional PlaceholderAPI identifiers for events, bosses, participation, live statistics, and history
[*]Ready-to-copy TAB scoreboard examples in English and Spanish
[*]Per-world live death, unique-death, participant, and survivor counters
[*]Narrative boss states: NOT_SPAWNED, ALIVE, and DEFEATED
[*]Atomic aggregate statistics and last-completed-event history
[*]Safe handling of interrupted events and same-night restart suppression
[*]A bilingual README and 17 administrative examples installed without overwriting local edits
[/LIST]

[B]Compatibility[/B]
[LIST]
[*]Paper/Purpur 1.21.x: Java 21
[*]Minecraft 26.1 and newer: Java 25
[*]Purpur 26.2: full vanilla lifecycle validated
[*]Paper 26.2: core startup, commands, reload, and shutdown validated
[*]MythicMobs 5.12.1 is supported on the validated Paper 1.21.8 combination, but not on Minecraft 26.2
[/LIST]

[B]Updating from 1.0.1[/B]
[LIST=1]
[*]Stop the server.
[*]Back up your worlds and plugins/BloodMoon/.
[*]Replace the old JAR with BloodMoon-Reborn-1.1.0.jar.
[*]Start the server once.
[*]Review the new SurvivorRewards, Boss, lifecycle-command, locale, and example files.
[*]Leave rewards disabled until every configured command has been checked.
[/LIST]

The migration creates a timestamped backup before adding missing 1.1.0 sections. Existing values
and legacy locale overrides are preserved, and repeated starts do not duplicate sections.

[B]Important behavior[/B]

An active Blood Moon interrupted by shutdown, restart, or recoverable crash is aborted without
uncertain rewards. Automatic restart is suppressed for the rest of that world/night cycle. An
administrator can still start a new event explicitly.

[B]Optional integrations[/B]

PlaceholderAPI, MythicMobs, WorldGuard, Multiverse-Core, and TAB remain optional. The built-in
BossBar applies only to vanilla bosses; MythicMobs controls its own boss bars and reward messages.
```

### Changes that matter to administrators

- Review all newly appended per-world configuration sections before enabling rewards.
- Use `/bloodmoon spawnzombieboss` as before; it now honors `Boss.Mode`.
- Choose `Language: en` or `Language: es`.
- Install PlaceholderAPI only if `%bloodmoon_*%` placeholders are needed.
- Merge one documented TAB scoreboard variant into TAB; do not use both language variants together.
- Use Java 25 for Minecraft 26.1 and newer.

### Problems corrected

- Safe console reload and command parsing.
- Duplicate or incorrect boss reward paths.
- Mythic boss display names incorrectly using the vanilla name.
- Orphan vanilla/Mythic boss state during cleanup.
- Unsafe asynchronous PlaceholderAPI access from consumers such as TAB.
- Literal legacy color codes in configurable survivor messages.
- Mixed-language vanilla death causes caused by flattening translatable messages.
- Unsafe YAML construction and duplicate-key acceptance.
- Incomplete-event restart behavior that could start another automatic event in the same night.

## Short changelog variants

### GitHub

BloodMoon-Reborn 1.1.0 adds optional survivor and boss rewards, MythicMobs boss mode, a configurable
vanilla BossBar, complete English/Spanish localization, 41 PlaceholderAPI identifiers, per-world
live statistics, aggregate history, safe interrupted-event handling, and 17 bundled administrative
examples. Existing 1.0.1 configuration remains supported and all new rewards default to disabled.

### Spigot

Version 1.1.0 adds optional rewards, MythicMobs boss support, a vanilla BossBar, PlaceholderAPI/TAB
integration, live event statistics, history, and complete EN/ES localization while preserving
existing configuration. Back up the plugin data, replace the JAR, and review the new sections
before enabling rewards.

### Discord

**BloodMoon-Reborn 1.1.0 is ready for stable release.**

Highlights: optional survivor/boss rewards, MythicMobs boss mode, vanilla BossBar, 41
`%bloodmoon_*%` placeholders, live per-world statistics, aggregate history, safe restart handling,
and full English/Spanish support.

Existing 1.0.1 configuration is preserved. New rewards are disabled by default.

## Final publication checklist

- [ ] Merge the release branch into `main`.
- [ ] Confirm `main` is clean and contains the approved final commit.
- [ ] Create annotated tag `v1.1.0`.
- [ ] Push `main` and tag `v1.1.0`.
- [ ] Create GitHub Release `BloodMoon-Reborn v1.1.0`.
- [ ] Mark the GitHub Release as stable, not a pre-release.
- [ ] Attach `BloodMoon-Reborn-1.1.0.jar`.
- [ ] Verify the uploaded JAR SHA-256 is
      `d28628a57257494c904a73943cadf0f1b170a8dea9cd6940b58a855724b33c05`.
- [ ] Open existing Spigot resource `135838`; do not create a new resource.
- [ ] Publish the 1.1.0 update text.
- [ ] Upload `BloodMoon-Reborn-1.1.0.jar` to Spigot.
- [ ] Update the displayed compatible Minecraft versions and Java requirements.
- [ ] Publish the stable Spigot update.
- [ ] Verify the GitHub and Spigot downloads after publication.
- [ ] Announce the stable release using the Discord summary.

Do not create `v1.1.0-rc.1`, a release-candidate branch, a GitHub pre-release, a public RC artifact,
or a Spigot RC update.
