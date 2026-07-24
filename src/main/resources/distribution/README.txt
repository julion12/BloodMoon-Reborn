BloodMoon-Reborn 1.1.0
======================

BloodMoon-Reborn continues SpectralMemories' BloodMoon plugin with per-world
events, stronger mobs, optional survivor/boss rewards, boss state, session
statistics, and safe server-wide historical totals.

QUICK START

- World configuration: plugins/BloodMoon/<world>/config.yml
- Language selector: plugins/BloodMoon/locales.yml (Language: en or es)
- Ready-to-review examples: plugins/BloodMoon/EXAMPLES/
- Administrator guides: plugins/BloodMoon/docs/

OPTIONAL INTEGRATIONS

- PlaceholderAPI exposes the internal "bloodmoon" expansion.
- MythicMobs can provide the configured Blood Moon boss.
- TAB can use the scoreboard example. BloodMoon-Reborn does not require TAB.

SUPPORTED RUNTIME

- Paper or Purpur 1.21.4 through 26.2 as documented in COMPATIBILITY.md.
- Java 21 for Minecraft 1.21.x; Java 25 for Minecraft 26.1+.

MAIN COMMANDS

/bloodmoon show
/bloodmoon start
/bloodmoon stop
/bloodmoon reload
/bloodmoon survivors
/bloodmoon spawnzombieboss
/bloodmoon killbosses

Review every example before merging it into an active YAML file. Preserve the
existing indentation and never paste a second top-level section with the same
name over an existing configuration.

Repository: https://github.com/julion12/BloodMoon-Reborn

Original author: SpectralMemories
Maintenance: JulioN12
