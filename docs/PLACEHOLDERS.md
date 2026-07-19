# Internal placeholders

Replacement is literal, deterministic, null-safe, and does not use regular expressions. Unknown placeholders remain unchanged. Newlines in values are replaced with spaces. Player/killer placeholders are empty when that context does not exist.

| Context | Placeholders |
| --- | --- |
| Player target | `%player%`, `%player_name%`, `%player_uuid%` |
| World | `%world%`, `%world_name%`, `%world_uuid%` |
| Session | `%session_uuid%`, `%start_time%`, `%end_time%`, `%duration_seconds%`, `%participant_count%`, `%survivor_count%`, `%death_count%` |
| Participation | `%participation_seconds%`, `%participation_percent%`, `%died%`, `%survived%` |
| Boss identity | `%boss_name%`, `%boss_type%`, `%boss_uuid%`, `%boss_killer%`, `%boss_killer_uuid%`, `%boss_world%`, `%boss_x%`, `%boss_y%`, `%boss_z%` |
| Boss health | `%boss_health%`, `%boss_max_health%`, `%boss_health_percent%` |

Boss-name rules:

- Vanilla: `$b` and `%boss_name%` use the customized `ZombieBossName`; `%boss_type%` is `VANILLA`.
- MythicMobs: `$b` and `%boss_name%` use the active entity display, then configured Mythic display, then `InternalName`, then localized fallback; `%boss_type%` is `MYTHICMOBS`.
- None/missing context: boss values are empty or `NONE`, without throwing an exception.

Legacy `$w`, `$p`, and `$b` remain supported. `$b` is available in boss locale messages and boss reward commands. `%boss_health%`, `%boss_max_health%`, and `%boss_health_percent%` are available in the vanilla BossBar and boss command context. PlaceholderAPI is not required and no external expansion is registered in 1.1.0.
