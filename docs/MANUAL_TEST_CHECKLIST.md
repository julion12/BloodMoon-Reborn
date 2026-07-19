# Manual release checklist

Use disposable copies of the production server and plugin data. Preserve each `latest.log`, the before/after `config.yml`, `sessions.yml`, and any migration backup as release evidence. Do not use synthetic results to sign off a real administrative migration.

## 1. Real 1.0.1 migration

1. Stop the disposable server and copy an unmodified, user-owned BloodMoon 1.0.1 world configuration into `plugins/BloodMoon/<world>/config.yml`.
2. Record its SHA-256, start once, and confirm exactly one `config.yml.bak-<timestamp>` exists with the original SHA-256.
3. Confirm legacy values are unchanged and exactly one `ConfigVersion`, `SurvivorRewards`, and `Boss` section exists.
4. Restart twice and run `/bloodmoon reload`; confirm no additional migration backup or duplicated section appears.
5. Stop cleanly and retain the files and log.

## 2. Survivor eligibility with two players

Enable a harmless, uniquely identifiable survivor command and set a short minimum participation time. Use players A and B in the same normal world.

1. Start a BloodMoon and confirm OnStart runs once.
2. Keep A alive and present past the minimum time.
3. Kill B during the event; confirm B remains ineligible after respawn.
4. Have A leave the world and return, then disconnect and reconnect; confirm accumulated eligible time follows the UUID without resetting or duplicating.
5. Join player C after the event started; confirm late-join behavior matches the configured policy.
6. Reload once from console and once from a permitted player while the session is active; confirm the session UUID and participant state remain stable and no lifecycle command repeats.
7. End the BloodMoon. Confirm A receives exactly one reward, B receives none, and C receives only the policy-appropriate result.
8. Repeat with two simultaneous worlds and stop only one; confirm participants and rewards do not cross worlds.

## 3. Vanilla boss rewards

Set `Boss.Mode: VANILLA`, enable a uniquely identifiable boss reward command, and disable unrelated rewards.

1. Spawn the boss with a player present and defeat it normally; confirm only the credited killer receives exactly one command reward.
2. Repeat with environmental/no-player final damage; confirm the configured no-killer behavior and no console exception.
3. Exercise cleanup (`bloodmoon killbosses`, event end, and plugin disable) and confirm no reward is issued for administrative removal.
4. Inspect the log for duplicate death processing and verify the reward cannot run twice for the same boss UUID.
5. With `VanillaBossBar.Enabled: true`, confirm one bar appears using `ZombieBossName`, follows damage, and disappears after death.
6. Test `NEARBY`, `WORLD`, and `ALL`; for `NEARBY`, cross the configured radius and confirm the audience refreshes without creating another bar.
7. Reload repeatedly, disable/re-enable the bar through config plus reload, end the event, unload the disposable world, and stop the plugin. Confirm no duplicate or orphan bar remains.

## 4. MythicMobs rewards and fallback

Use a MythicMobs build that officially supports the exact server version. MythicMobs 5.12.1 is suitable for the validated Paper 1.21.8 bridge test but not for the tested Paper 26.2 build.

1. Install the example `BloodMoonBoss` definition from `docs/MYTHICMOBS.md`; set `Boss.Mode: MYTHICMOBS`, `Enabled: true`, and its exact `InternalName`.
2. With `UseMythicMobsRewards: true` and `RunBloodMoonRewardCommands: false`, spawn and kill the boss. Confirm Mythic drops/skills run once and BloodMoon boss reward commands do not run.
3. Enable `RunBloodMoonRewardCommands`, repeat with a credited killer, and confirm the additional BloodMoon command runs exactly once without duplicating ordinary enhanced-mob drops.
4. End an event and disable the plugin while a tracked Mythic boss exists; confirm only that entity is removed and no reward is granted.
5. Test an unknown `InternalName`: with `FallbackToVanilla: true`, confirm one vanilla boss; with it `false`, confirm no boss and one clear warning.
6. Remove MythicMobs completely and restart. Confirm BloodMoon enables, reloads, runs the vanilla/default path, and stops without `NoClassDefFoundError` or linkage errors.
7. Confirm the arrival and death messages show the Mythic `Display`, never the customized vanilla `ZombieBossName`, and that no BloodMoon vanilla BossBar appears.

## 5. Languages and legacy locales

1. On a new disposable installation, select `Language: en`, reload, and exercise status, warnings, boss, survivor, and migration messages.
2. Select `Language: es`, reload, and repeat. Confirm accents, opening punctuation, color codes, and placeholders render correctly.
3. Remove one noncritical key from a copy of `locales/es.yml`; confirm the English value is used and no exception occurs.
4. Install an old customized `locales.yml` containing `ZombieBossName: "el duro"`. Start once and confirm a timestamped backup, preserved custom value, and appended missing keys.
5. Restart and reload twice; confirm no second migration write or backup.
6. Spawn vanilla and Mythic bosses in turn. Confirm vanilla uses `el duro`, while Mythic uses its own `Display`.

## Sign-off

Release status may change from `NOT READY` only after the real player tests above pass, the deployment server/Mythic combination enables, and the collected logs show no duplicate reward, cross-world leakage, unsafe crash payout, class-link error, or shutdown exception.
