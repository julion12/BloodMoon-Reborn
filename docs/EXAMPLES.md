# Ready-to-copy examples

The 17 files in this directory are bundled inside the JAR and copied, only when missing, to
`plugins/BloodMoon/EXAMPLES/`. BloodMoon-Reborn never injects them into TAB, PlaceholderAPI,
MythicMobs, or active world configurations automatically.

Five bilingual index files contain comments only and direct administrators to one functional
language variant. Copy or merge only the selected `-en.yml` or `-es.yml`; using both can create
duplicate YAML keys, duplicate scoreboards, or two command/reward lists.

| Index | English variant | Spanish variant |
| --- | --- | --- |
| [TAB-scoreboards.yml](examples/TAB-scoreboards.yml) | [TAB-scoreboards-en.yml](examples/TAB-scoreboards-en.yml) | [TAB-scoreboards-es.yml](examples/TAB-scoreboards-es.yml) |
| [CommandsOnStart.yml](examples/CommandsOnStart.yml) | [CommandsOnStart-en.yml](examples/CommandsOnStart-en.yml) | [CommandsOnStart-es.yml](examples/CommandsOnStart-es.yml) |
| [CommandsOnEnd.yml](examples/CommandsOnEnd.yml) | [CommandsOnEnd-en.yml](examples/CommandsOnEnd-en.yml) | [CommandsOnEnd-es.yml](examples/CommandsOnEnd-es.yml) |
| [SurvivorRewards.yml](examples/SurvivorRewards.yml) | [SurvivorRewards-en.yml](examples/SurvivorRewards-en.yml) | [SurvivorRewards-es.yml](examples/SurvivorRewards-es.yml) |
| [BossRewards.yml](examples/BossRewards.yml) | [BossRewards-en.yml](examples/BossRewards-en.yml) | [BossRewards-es.yml](examples/BossRewards-es.yml) |

[PlaceholderAPI-examples.txt](examples/PlaceholderAPI-examples.txt) is bilingual and includes all
41 public identifiers. [MythicMobs-BloodMoonBoss.yml](examples/MythicMobs-BloodMoonBoss.yml)
contains two independent mob IDs, `BloodMoonBoss_EN` and `BloodMoonBoss_ES`; configure exactly one
as `Boss.MythicMobs.InternalName`.

All YAML resources and UTF-8 text are validated automatically. Command availability and visual TAB
rendering still depend on the administrator's installed plugins and target server.
