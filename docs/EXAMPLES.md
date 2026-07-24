# Ready-to-copy examples

These files are bundled inside the JAR and copied, only when missing, to
`plugins/BloodMoon/EXAMPLES/`. BloodMoon-Reborn never injects them into TAB, PlaceholderAPI,
MythicMobs, or active world configurations automatically.

- [TAB-scoreboards.yml](examples/TAB-scoreboards.yml): recommended condition-free dynamic boss lines, compact and historical variants, plus a clearly separated advanced optional condition.
- [CommandsOnStart.yml](examples/CommandsOnStart.yml): verified `;s`, `;f`, `;p`, `$w`, `$p`, and modern placeholder usage.
- [CommandsOnEnd.yml](examples/CommandsOnEnd.yml): completion announcements, weather, cleanup, and per-player execution.
- [SurvivorRewards.yml](examples/SurvivorRewards.yml): disabled-by-default item, experience, and optional economy rewards.
- [BossRewards.yml](examples/BossRewards.yml): vanilla/Mythic selection, killer commands, and duplicate-reward warning.
- [PlaceholderAPI-examples.txt](examples/PlaceholderAPI-examples.txt): `/papi` checks for every public placeholder.
- [MythicMobs-BloodMoonBoss.yml](examples/MythicMobs-BloodMoonBoss.yml): MythicMobs boss definition.

Read the comments at the top of each file before merging it. YAML validity is tested automatically, but command availability and visual TAB rendering depend on the administrator's installed plugins and must be validated on the target server.
