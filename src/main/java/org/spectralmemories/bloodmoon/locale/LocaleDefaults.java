package org.spectralmemories.bloodmoon.locale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LocaleDefaults {
    private static final Map<String, String> ENGLISH = createEnglish();

    private LocaleDefaults() { }

    public static Map<String, String> english() { return ENGLISH; }

    private static Map<String, String> createEnglish() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("BloodMoonTitleBar", "Blood Moon");
        values.put("DeathSuffix", "during a Blood Moon!");
        values.put("DaysBeforeBloodMoon", "&aThere are &f$d &adays until the next Blood Moon.");
        values.put("BloodMoonRightNow", "&fA Blood Moon is active right now!");
        values.put("BloodMoonTomorrow", "&6A Blood Moon will arrive tomorrow.");
        values.put("BloodMoonTonight", "&5The sky is darker than usual tonight.");
        values.put("BloodMoonWarningTitle", "&4&lThe Blood Moon is upon us!");
        values.put("BloodMoonWarningBody", "&cExperience is multiplied and mobs have bonus drops.$n&cMobs are stronger and apply special effects.");
        values.put("BloodMoonEndingMessage", "&3&lThe Blood Moon fades away... for now.");
        values.put("DyingResultsInInventoryLoss", "&4&lDying during a Blood Moon removes your inventory.");
        values.put("DyingResultsInExperienceLoss", "&4&lDying during a Blood Moon resets your experience.");
        values.put("PluginReloaded", "&bThe plugin was reloaded successfully.");
        values.put("NoBloodMoonInWorld", "&fThere is no active Blood Moon in this world.");
        values.put("CommandNotFound", "&cThe command &o$d&r&c does not exist.");
        values.put("NoPermission", "&cYou do not have permission to do that.");
        values.put("AllowedCommands", "&fAvailable commands: &o$d");
        values.put("BedNotAllowed", "&cYou cannot sleep during a Blood Moon.");
        values.put("WorldIsPermanentBloodMoon", "&cThis world is permanently under a Blood Moon.");
        values.put("CannotStopBloodMoon", "&cYou cannot stop the Blood Moon in this world.");
        values.put("GeneralError", "&cAn error occurred while processing your request.");
        values.put("ZombieBossSpawned", "&f&l$b &fhas arrived!");
        values.put("ZombieBossName", "The Tough One");
        values.put("BossSlain", "&l$p &2has defeated &f$b!");
        values.put("HordeArrived", "&fA horde has descended upon &f$p!");
        values.put("NoPlayerOfName", "&cPlayer &f$p &cwas not found in world &f$w.");
        values.put("SurvivorRewardReceived", "&aYou survived the Blood Moon and received your reward.");
        values.put("SurvivorDisqualifiedByDeath", "&cYou died and are no longer eligible for the survivor reward.");
        values.put("SurvivorInsufficientParticipation", "&eYou did not participate long enough to receive the survivor reward.");
        values.put("NoEligibleSurvivors", "No players were eligible for survivor rewards in %world%.");
        values.put("BossRewardReceived", "&aYou received the reward for defeating %boss_name%.");
        values.put("BossRewardFailed", "The boss reward could not be processed for %boss_name%.");
        values.put("MythicBossNotFound", "Configured MythicMob '%boss_name%' was not found in %world%.");
        values.put("MythicMobsUnavailable", "MythicMobs is unavailable or incompatible in %world%.");
        values.put("MythicMobsFallbackToVanilla", "Falling back to the vanilla Blood Moon boss in %world%.");
        values.put("MythicBossFallbackName", "Mythic Boss");
        values.put("BossDisabled", "&eThe Blood Moon boss is disabled in this world.");
        values.put("BossSpawnFailed", "&cThe configured Blood Moon boss could not be spawned.");
        values.put("PlaceholderActive", "Active");
        values.put("PlaceholderInactive", "Inactive");
        values.put("PlaceholderNone", "None");
        values.put("PlaceholderEligible", "Eligible");
        values.put("PlaceholderDisqualified", "Disqualified");
        values.put("PlaceholderNotParticipating", "Not participating");
        values.put("PlaceholderNoBoss", "Not spawned yet");
        values.put("BloodMoonStatus", "&fSession %session_uuid%: %eligible_count%/%participant_count% participants are currently eligible.");
        values.put("BloodMoonSurvivorsHeader", "&fEligible Blood Moon survivors in %world%:");
        values.put("BloodMoonSessionNotFound", "&fThere is no active Blood Moon session in %world%.");
        values.put("ConfigurationMigrationCompleted", "Migrated %file% to %version%; backup: %backup%.");
        values.put("ConfigurationMigrationFailed", "Could not migrate %file%; the original file was preserved.");
        return Collections.unmodifiableMap(values);
    }
}
