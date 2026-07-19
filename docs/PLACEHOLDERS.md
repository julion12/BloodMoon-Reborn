# Internal placeholders

Replacement is literal, deterministic, null-safe, and does not use regular expressions. Unknown placeholders remain unchanged. Newlines in values are replaced with spaces. Player/killer placeholders are empty when that context does not exist.

| Context | Placeholders |
| --- | --- |
| Player target | `%player%`, `%player_name%`, `%player_uuid%` |
| World | `%world%`, `%world_name%`, `%world_uuid%` |
| Session | `%session_uuid%`, `%start_time%`, `%end_time%`, `%duration_seconds%`, `%participant_count%`, `%survivor_count%`, `%death_count%` |
| Participation | `%participation_seconds%`, `%participation_percent%`, `%died%`, `%survived%` |
| Boss | `%boss_name%`, `%boss_type%`, `%boss_uuid%`, `%boss_killer%`, `%boss_killer_uuid%`, `%boss_world%`, `%boss_x%`, `%boss_y%`, `%boss_z%` |

Legacy `$w` and `$p` remain supported in configured command lists. `%survivor_count%` is available during survivor rewards; boss placeholders are available during boss rewards. PlaceholderAPI is not required and no external expansion is registered in 1.1.0.
