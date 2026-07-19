package org.spectralmemories.bloodmoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.locale.LocaleCatalog;
import org.spectralmemories.bloodmoon.locale.LocaleDefaults;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocaleReader implements Closeable {
    public static final String STRING_NOT_FOUND = "[String not found]";
    public static final String VERSION_CONFIG = "LocalesVersion";
    public static final String NULL_LOCALE = "null";
    public static final String VOID_STRING = "%void%";

    private static final Map<String, String> DEFAULT_ENGLISH = LocaleDefaults.english();
    public static final String[] LOCALES_IDS = DEFAULT_ENGLISH.keySet().toArray(String[]::new);
    public static final String[] DEFAULT_LOCALES = DEFAULT_ENGLISH.values().toArray(String[]::new);

    private final File legacyFile;
    private final File localeDirectory;
    private final Consumer<String> warningSink;
    private final Set<String> warnedKeys = ConcurrentHashMap.newKeySet();
    private LocaleCatalog catalog;

    /** Legacy constructor retained for tests and integrations that provide one locales.yml file. */
    public LocaleReader(File file) {
        this(file, null, message -> System.out.println("[BloodMoon] WARNING: " + message));
    }

    public LocaleReader(File legacyFile, File localeDirectory, Consumer<String> warningSink) {
        this.legacyFile = legacyFile;
        this.localeDirectory = localeDirectory;
        this.warningSink = warningSink == null ? ignored -> { } : warningSink;
        RefreshLocales();
    }

    @Override public void close() throws IOException { }

    public static void BroadcastLocale(String id, String[] args, String[] replacements) {
        String locale = format(id, args, replacements);
        if (!locale.isEmpty()) Bukkit.broadcastMessage(locale);
    }

    public static void MessageAllLocale(String id, String[] args, String[] replacements, World world) {
        String locale = format(id, args, replacements);
        if (locale.isEmpty() || world == null) return;
        for (Player player : world.getPlayers()) player.sendMessage(locale);
    }

    public static void MessageLocale(String id, String[] args, String[] replacements, CommandSender sender) {
        String locale = format(id, args, replacements);
        if (!locale.isEmpty() && sender != null) sender.sendMessage(locale);
    }

    public static String FormatLocale(String id, String[] args, String[] replacements) {
        return format(id, args, replacements);
    }

    private static String format(String id, String[] args, String[] replacements) {
        Bloodmoon plugin = Bloodmoon.GetInstance();
        if (plugin == null || plugin.getLocaleReader() == null) return "";
        String locale = plugin.getLocaleReader().GetLocaleString(id);
        if (args != null && replacements != null && args.length == replacements.length) {
            for (int i = 0; i < args.length; i++) {
                locale = locale.replace(args[i], replacements[i] == null ? "" : replacements[i]);
            }
        }
        return locale;
    }

    public String GetLocaleString(String id) {
        String value = catalog.get(id).orElse(null);
        if (value == null || NULL_LOCALE.equals(value)) {
            warnOnce(id, "Locale key '" + id + "' is missing from the selected language and English fallback");
            return STRING_NOT_FOUND;
        }
        if (VOID_STRING.equals(value)) return "";
        value = value.replace("$n", "\n");
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String GetLocalePlainString(String id) {
        String colored = GetLocaleString(id);
        String plain = ChatColor.stripColor(colored);
        return plain == null ? "" : plain;
    }

    public String GetFileVersion() { return catalog.version(); }
    public String GetLanguage() { return catalog.language(); }

    public final void RefreshLocales() {
        Map<String, Object> legacy = load(legacyFile);
        Map<String, Object> english = localeDirectory == null
                ? new LinkedHashMap<>(DEFAULT_ENGLISH) : load(new File(localeDirectory, "en.yml"));
        if (english.isEmpty()) english = new LinkedHashMap<>(DEFAULT_ENGLISH);
        String language = String.valueOf(legacy.getOrDefault("Language", "en")).trim().toLowerCase(Locale.ROOT);
        if (!language.matches("[a-z0-9_-]+")) language = "en";
        Map<String, Object> selected = "en".equals(language) || localeDirectory == null
                ? english : load(new File(localeDirectory, language + ".yml"));
        if (selected.isEmpty() && !"en".equals(language)) {
            warnOnce("Language:" + language, "Language '" + language + "' was not found; falling back to English");
        }
        catalog = new LocaleCatalog(legacy, selected, english);
    }

    public void ReadAllEntries() {
        for (String id : LOCALES_IDS) GetLocaleString(id);
    }

    /** Creates a selector/override file for new installations; bundled catalogs hold the full translations. */
    public void GenerateDefaultFile() {
        try (FileWriter writer = new FileWriter(legacyFile, false)) {
            writer.write("# Locale selector and compatibility overrides. Existing values are never overwritten.\n");
            writer.write("LocalesVersion: 1.1.0\n");
            writer.write("Language: en\n");
            writer.write("UseBundledLocales: true\n");
            writer.write("# Add any key from locales/en.yml or locales/es.yml below to override it.\n");
        } catch (IOException exception) {
            warningSink.accept("Could not generate " + Bloodmoon.LOCALES_YML + ": " + exception.getMessage());
        }
        RefreshLocales();
    }

    public static Map<String, String> defaultEnglish() { return DEFAULT_ENGLISH; }

    private Map<String, Object> load(File file) {
        try {
            return LocaleCatalog.load(file == null ? null : file.toPath());
        } catch (IOException | RuntimeException exception) {
            warnOnce(file == null ? "unknown" : file.getAbsolutePath(),
                    "Could not load locale file " + file + ": " + exception.getMessage());
            return Collections.emptyMap();
        }
    }

    private void warnOnce(String key, String message) {
        if (warnedKeys.add(key)) warningSink.accept(message);
    }

}
