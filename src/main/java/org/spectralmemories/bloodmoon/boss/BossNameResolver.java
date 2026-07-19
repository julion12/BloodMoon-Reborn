package org.spectralmemories.bloodmoon.boss;

import java.util.Locale;
import java.util.regex.Pattern;

/** Resolves display-only boss names without participating in entity identity. */
public final class BossNameResolver {
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)(?:&|\u00a7)[0-9A-FK-ORX]");
    private static final Pattern MINI_MESSAGE_TAG = Pattern.compile("</?[A-Za-z][^>]*>");

    private BossNameResolver() { }

    public static ResolvedBossName resolve(String mode, String entityDisplayName, String configuredDisplayName,
                                           String internalName, String vanillaName, String localizedFallback) {
        String normalizedMode = mode == null ? "NONE" : mode.trim().toUpperCase(Locale.ROOT);
        return switch (normalizedMode) {
            case "VANILLA" -> new ResolvedBossName(safe(vanillaName), "VANILLA");
            case "MYTHICMOBS" -> new ResolvedBossName(firstVisible(entityDisplayName, configuredDisplayName,
                    internalName, localizedFallback), "MYTHICMOBS");
            default -> new ResolvedBossName("", "NONE");
        };
    }

    private static String firstVisible(String... candidates) {
        for (String candidate : candidates) {
            String visible = toPlainText(candidate);
            if (!visible.isBlank()) return visible;
        }
        return "";
    }

    /** Converts legacy or MiniMessage formatting to safe visible text for legacy Bukkit messages. */
    public static String toPlainText(String value) {
        if (value == null || value.isBlank()) return "";
        String input = value.replace('\r', ' ').replace('\n', ' ').trim();
        String withoutLegacyCodes = LEGACY_COLOR.matcher(input).replaceAll("");
        return MINI_MESSAGE_TAG.matcher(withoutLegacyCodes).replaceAll("").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public record ResolvedBossName(String name, String type) { }
}
