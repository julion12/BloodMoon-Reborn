package org.spectralmemories.bloodmoon.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Idempotently adds 1.1 settings while preserving existing values and comments. */
public final class ConfigMigrator {
    public static final String TARGET_VERSION = "1.1.0";
    private static final Pattern VERSION = Pattern.compile("(?m)^ConfigVersion\\s*:\\s*([^#\\r\\n]+)");
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);
    private static final String SURVIVOR_SETTINGS = """

# BloodMoon-Reborn 1.1.0 additions. All rewards are disabled by default.
SurvivorRewards:
  Enabled: false
  RequireOnlineAtEnd: true
  IncludeLateJoiners: true
  MinimumParticipationSeconds: 0
  DisqualifyOnDeath: true
  DisqualifyOnWorldLeave: false
  DisqualifyOnDisconnect: false
  RewardOncePerSession: true
  Messages: []
  Commands: []
""";
    private static final String BOSS_SETTINGS = """
Boss:
  Mode: VANILLA
  Rewards:
    Enabled: false
    Mode: KILLER
    RequirePlayerKiller: true
    RewardOnce: true
    Commands: []
  MythicMobs:
    Enabled: false
    InternalName: BloodMoonBoss
    UseMythicMobsRewards: true
    RunBloodMoonRewardCommands: false
    FallbackToVanilla: true
""";

    private ConfigMigrator() { }

    public static MigrationResult migrate(Path file, Clock clock) throws IOException {
        String original = Files.readString(file, StandardCharsets.UTF_8);
        String migrated = migrateContent(original);
        if (original.equals(migrated)) return new MigrationResult(false, null);

        Path backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()));
        int suffix = 1;
        while (Files.exists(backup)) {
            backup = file.resolveSibling(file.getFileName() + ".bak-" + BACKUP_TIME.format(clock.instant()) + "-" + suffix++);
        }
        Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
        Files.writeString(file, migrated, StandardCharsets.UTF_8);
        return new MigrationResult(true, backup);
    }

    public static String migrateContent(String original) {
        String source = original == null ? "" : original;
        Matcher matcher = VERSION.matcher(source);
        String current = matcher.find() ? matcher.group(1).trim() : "0.0.0";
        boolean needsVersion = SemanticVersion.parse(current).compareTo(SemanticVersion.parse(TARGET_VERSION)) < 0;
        boolean needsSurvivor = !Pattern.compile("(?m)^SurvivorRewards\\s*:").matcher(source).find();
        boolean needsBoss = !Pattern.compile("(?m)^Boss\\s*:").matcher(source).find();
        if (!needsVersion && !needsSurvivor && !needsBoss) return source;

        String result = source;
        if (needsVersion) {
            Matcher versionMatcher = VERSION.matcher(result);
            result = versionMatcher.find()
                    ? versionMatcher.replaceFirst(Matcher.quoteReplacement("ConfigVersion: " + TARGET_VERSION))
                    : "ConfigVersion: " + TARGET_VERSION + System.lineSeparator() + result;
        }
        if (needsSurvivor) result = result.stripTrailing() + System.lineSeparator() + SURVIVOR_SETTINGS;
        if (needsBoss) result = result.stripTrailing() + System.lineSeparator() + BOSS_SETTINGS;
        return result;
    }

    public record MigrationResult(boolean changed, Path backup) { }
}
