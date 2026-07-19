# Migration from 1.0.1

1. Stop the server and back up the plugin folder.
2. Install `BloodMoon-Reborn-1.1.0.jar`.
3. Start once and inspect each per-world config.
4. Configure rewards while they remain disabled, then enable deliberately.

For each valid YAML file, migration:

- compares `ConfigVersion` semantically;
- creates `config.yml.bak-YYYYMMDD-HHMMSS` before any write;
- updates only the version line;
- appends only missing `SurvivorRewards` and `Boss` sections;
- preserves existing keys, lists, comments, and custom values;
- is idempotent.

Invalid YAML is retained unchanged and the previous in-memory configuration is kept during `/bloodmoon reload`. On initial load, safe defaults are used and writes are disabled for the invalid file. Fix the logged YAML error manually; the plugin does not silently regenerate it.

The deprecated global `plugins/BloodMoon/config.yml` is still not used; configuration remains per world as in 1.0.1.
