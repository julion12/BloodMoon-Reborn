package org.spectralmemories.bloodmoon;

import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;
import org.spectralmemories.sqlaccess.SQLTable;
import org.spectralmemories.bloodmoon.config.ConfigMigrator;
import org.spectralmemories.bloodmoon.session.SessionCoordinator;
import org.spectralmemories.bloodmoon.integration.MythicMobsBridge;
import org.spectralmemories.bloodmoon.integration.NoMythicMobsBridge;
import org.spectralmemories.bloodmoon.placeholder.NoPlaceholderIntegration;
import org.spectralmemories.bloodmoon.placeholder.PlaceholderIntegration;
import org.spectralmemories.bloodmoon.locale.LocaleMigrator;
import org.spectralmemories.bloodmoon.statistics.HistoricalStatisticsService;
import org.spectralmemories.bloodmoon.distribution.AdministratorGuideInstaller;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.time.Clock;

/**
 * Entry class for the BloodMoon plugin. Singleton, you should never create an instance manually
 */
public final class Bloodmoon extends JavaPlugin
{
    public static final String CACHE_DB = "cache.db";
    /**
     * The config file
     */
    public static final String CONFIG_FILE = "config.yml";
    public static final String SLASH = "/";
    /**
     * The locale file
     */
    public static final String LOCALES_YML = "locales.yml";
    public final static long NIGHT_CHECK_DELAY = 40;

    private static SQLAccess sqlAccess;
    private static LocaleReader localeReader;

    private static Bloodmoon instance;

    private static List<PeriodicNightCheck> nightChecks;
    private static List<BloodmoonActuator> actuators;
    private static List<World> bloodmoonWorlds;
    private static Map<World, ConfigReader> configReaders;
    private static List<ConfigReader> allConfigReaders;

    private static WorldManager worldManager;
    private SessionCoordinator sessionCoordinator;
    private HistoricalStatisticsService statisticsService;
    private MythicMobsBridge mythicMobs = new NoMythicMobsBridge();
    private PlaceholderIntegration placeholderIntegration = new NoPlaceholderIntegration();

    /**
     * Returns the Bloodmoon instance
     * This method has a very low latency
     * @return The Bloodmoon singleton instance
     */
    public static Bloodmoon GetInstance ()
    {
        return instance;
    }

    private List<World> BlackListedWorlds;

    /**
     * Returns blacklisted worlds
     * @return Blacklisted worlds
     */
    public List<World> getBlacklistedWorlds()
    {
        return BlackListedWorlds;
    }

    /**
     * Returns worlds where bloodmoons apply, either periodically or permanently
     * @return Bloodmoon enabled worlds
     */
    public static List<World> GetBloodMoonWorlds()
    {
        return bloodmoonWorlds;
    }

    /**
     * Returns the server scheduler. Mostly a shortcut
     * @return scheduler
     */
    public BukkitScheduler GetScheduler ()
    {
        return getServer().getScheduler();
    }

    private void InitializeSQLAccess ()
    {
        boolean mustCreateDb = ! (new File(getDataFolder().getAbsolutePath() + SLASH + CACHE_DB).exists());
        try
        {
            DriverManager.getConnection(SQLAccess.JDBC_SQLITE + getDataFolder().getAbsolutePath() + SLASH + CACHE_DB); //create db if it does not exist
            File db = new File(getDataFolder().getAbsoluteFile() + SLASH + CACHE_DB);

            sqlAccess = new SQLAccess(db);

            if (mustCreateDb)
            {
                List<SQLField> fields = new ArrayList<>();
                fields.add(new SQLField("world", FieldType.TEXT, true, false));
                fields.add(new SQLField("days", FieldType.INTEGER, false, false));
                fields.add(new SQLField("checkAt", FieldType.INTEGER, false, false));

                SQLTable bloodMoonTable = new SQLTable("lastBloodMoon",  fields);

                sqlAccess.CreateTable(bloodMoonTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns the SQLAccess object initialized at startup
     * @return the SQLAccess instance
     */
    public SQLAccess getSqlAccess ()
    {
        if (sqlAccess == null) InitializeSQLAccess();
        return sqlAccess;
    }

    /**
     * Returns the initialized ConfigReader for a world. May return null if none was found
     * @param world The world in question
     * @return The ConfigReader for that world
     */
    public ConfigReader getConfigReader (World world)
    {
        try
        {
            return configReaders.get(world);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns all valid ConfigReaders found
     * @return all ConfigReaders
     */
    public ConfigReader[] getAllConfigReaders ()
    {
        return allConfigReaders.toArray(new ConfigReader[allConfigReaders.size()]);
    }

    /**
     * Returns the LocaleReader
     * @return LocaleReader
     */
    public LocaleReader getLocaleReader ()
    {
        if (localeReader == null)
        {
            File localeFile = new File (getDataFolder () + SLASH + LOCALES_YML);
            File localeDirectory = new File(getDataFolder(), "locales");

            try
            {
                if (!localeDirectory.exists() && !localeDirectory.mkdirs()) {
                    throw new IOException("Could not create " + localeDirectory);
                }
                saveBundledLocale("locales/en.yml");
                saveBundledLocale("locales/es.yml");
                if (! localeFile.exists())
                {
                    localeFile.createNewFile();
                    localeReader = new LocaleReader(localeFile, localeDirectory,
                            message -> getLogger().warning(message));
                    localeReader.GenerateDefaultFile();
                } else {
                    LocaleMigrator.MigrationResult migration = LocaleMigrator.migrate(localeFile.toPath(), Clock.systemUTC());
                    localeReader = new LocaleReader(localeFile, localeDirectory,
                            message -> getLogger().warning(message));
                    if (migration.changed()) {
                        getLogger().info(localeReader.GetLocalePlainString("ConfigurationMigrationCompleted")
                                .replace("%file%", LOCALES_YML)
                                .replace("%version%", LocaleMigrator.TARGET_VERSION)
                                .replace("%backup%", migration.backup().getFileName().toString()));
                    }
                }
            }
            catch (IOException e)
            {
                getLogger().log(Level.SEVERE, "Could not initialize locale files; built-in English remains available", e);
                localeReader = new LocaleReader(localeFile, localeDirectory,
                        message -> getLogger().warning(message));
            }
            localeReader.ReadAllEntries();
        }

        return localeReader;
    }

    private void saveBundledLocale(String resourcePath) {
        File target = new File(getDataFolder(), resourcePath.replace('/', File.separatorChar));
        if (!target.exists()) {
            saveResource(resourcePath, false);
            return;
        }
        try (InputStream input = getResource(resourcePath)) {
            if (input == null) return;
            Object loaded = new Yaml().load(input);
            if (!(loaded instanceof Map<?, ?> map)) return;
            Map<String, Object> bundled = new java.util.LinkedHashMap<>();
            map.forEach((key, value) -> {
                if (key != null) bundled.put(String.valueOf(key), value);
            });
            LocaleMigrator.MigrationResult migration = LocaleMigrator.migrateCatalog(
                    target.toPath(), bundled, Clock.systemUTC());
            if (migration.changed()) {
                getLogger().info("Added missing entries to " + resourcePath + "; backup: "
                        + migration.backup().getFileName());
            }
        } catch (IOException | RuntimeException exception) {
            getLogger().log(Level.WARNING, "Could not update missing entries in " + resourcePath
                    + "; existing locale was preserved", exception);
        }
    }

    public SessionCoordinator getSessionCoordinator() { return sessionCoordinator; }
    public HistoricalStatisticsService getStatisticsService() { return statisticsService; }
    public NamespacedKey getBossKey() { return new NamespacedKey(this, "bloodmoon_boss"); }
    public MythicMobsBridge getMythicMobs() { return mythicMobs; }
    public PlaceholderIntegration getPlaceholderIntegration() { return placeholderIntegration; }

    /**
     * Creates the BloodMoon folder if it does not exist
     */
    public void CreateFolder ()
    {
        File folder = getDataFolder();
        folder.mkdir();
    }

    private void LoadCache (World world)
    {
        try
        {
            SQLAccess access = getSqlAccess();
            boolean exists = access.EntryExist("lastBloodMoon", new SQLField("world", FieldType.TEXT, true, false), world.getUID().toString());

            if (exists)
            {
                ResultSet set = access.ExecuteSQLQuery("SELECT days, checkAt FROM lastBloodMoon WHERE world = '" + world.getUID().toString() + "';");
                set.next();

                PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(world);

                nightCheck.SetDaysRemaining(set.getInt("days"));
                nightCheck.SetCheckAfter(set.getInt("checkAt"));
                set.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Create a config reader, setting it up if it does not exist
    private ConfigReader CreateSingleConfigReader (World world)
    {
        File worldFolder = new File (getDataFolder() + SLASH + world.getName());
        if (! worldFolder.exists()) worldFolder.mkdir();

        File configFile = new File (worldFolder.getAbsolutePath() + SLASH + CONFIG_FILE);
        if (! configFile.exists())
        {
            try
            {
                configFile.createNewFile();
                ConfigReader reader = new ConfigReader(configFile, world);
                reader.GenerateDefaultFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        ConfigReader reader = new ConfigReader(configFile, world);
        if (!reader.TryRefreshConfigs()) {
            getLogger().severe("Invalid YAML in " + configFile + "; preserving the file and using safe defaults");
        } else {
            try {
                ConfigMigrator.MigrationResult migration = ConfigMigrator.migrate(configFile.toPath(), Clock.systemUTC());
                if (migration.changed()) {
                    String message = getLocaleReader().GetLocalePlainString("ConfigurationMigrationCompleted")
                            .replace("%file%", world.getName() + "/config.yml")
                            .replace("%version%", ConfigMigrator.TARGET_VERSION)
                            .replace("%backup%", migration.backup().getFileName().toString());
                    getLogger().info(message);
                    reader.TryRefreshConfigs();
                }
            } catch (IOException exception) {
                String message = getLocaleReader().GetLocalePlainString("ConfigurationMigrationFailed")
                        .replace("%file%", configFile.toString());
                getLogger().log(Level.SEVERE, message, exception);
            }
        }
        reader.ReadAllSettings();
        configReaders.put(world, reader);
        allConfigReaders.add(reader);
        return reader;
    }

    /**
     * Enables the plugin
     */
    @Override
    public void onEnable()
    {
		getLogger().info("BloodMoon-Reborn v" + getDescription().getVersion());
		getLogger().info("Original author: SpectralMemories");
		getLogger().info("Maintained by: JulioN12");
        instance = this;

        CreateFolder();
        var guides = new AdministratorGuideInstaller(
                getDataFolder().toPath(), getClass().getClassLoader(), getLogger()).installMissing();
        if (guides.readmeCreated()) {
            getLogger().info("Administrator guides were created in: "
                    + new File(getDataFolder(), "docs").getPath());
            getLogger().info("Ready-to-copy examples were created in: "
                    + new File(getDataFolder(), "EXAMPLES").getPath());
            getLogger().info("Start with " + new File(getDataFolder(), "README.txt").getPath());
        } else if (guides.createdCount() > 0) {
            getLogger().info("Created " + guides.createdCount()
                    + " missing administrator documentation file(s) without overwriting existing files");
        }

        statisticsService = new HistoricalStatisticsService(
                new File(getDataFolder(), "statistics.yml").toPath(), getLogger());
        sessionCoordinator = new SessionCoordinator(this);

        if (getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            try {
                mythicMobs = new org.spectralmemories.bloodmoon.integration.mythic.MythicMobsIntegration(this);
                getLogger().info("MythicMobs integration enabled");
            } catch (LinkageError | RuntimeException exception) {
                mythicMobs = new NoMythicMobsBridge();
                getLogger().log(Level.WARNING, "MythicMobs was found but its public API could not be initialized; vanilla fallback remains available", exception);
            }
        }

        getSqlAccess();
        getLocaleReader();

        worldManager = new WorldManager();

        nightChecks = new ArrayList<>();
        actuators = new ArrayList<>();

        BlackListedWorlds = new ArrayList<>();
        configReaders = new HashMap<>();
        allConfigReaders = new ArrayList<>();
        bloodmoonWorlds = new ArrayList<>();

        for (World world : getServer().getWorlds())
        {
            LoadWorld(world);
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                placeholderIntegration = new org.spectralmemories.bloodmoon.placeholder.papi.PlaceholderApiIntegration(this);
                if (placeholderIntegration.registered()) getLogger().info("PlaceholderAPI expansion registered");
            } catch (LinkageError | RuntimeException exception) {
                placeholderIntegration = new NoPlaceholderIntegration();
                getLogger().log(Level.WARNING, "PlaceholderAPI was found but the BloodMoon expansion could not be registered", exception);
            }
        }

        getServer().getPluginManager().registerEvents (worldManager, this);

        BloodmoonCommandExecutor commandExecutor = new BloodmoonCommandExecutor();
        if (getCommand("bloodmoon") != null) {
            getCommand("bloodmoon").setExecutor(commandExecutor);
            getCommand("bloodmoon").setTabCompleter(commandExecutor);
        }


        if (getCommand("testsuite") != null) getCommand("testsuite").setExecutor(new TestCommandExecutor());

        CheckOlderConfigs();
    }

    /**
     * Loads a world and reads its config
     * @param world The world to load
     */
    public void LoadWorld (World world)
    {
        if (world.getEnvironment() != World.Environment.NORMAL)
        {
            return;
        }

        ConfigReader configReader = CreateSingleConfigReader(world);
        if (configReader.GetIsBlacklistedConfig())
        {
            BlackListedWorlds.add(world);
            return;
        }

        BloodmoonActuator actuator = new BloodmoonActuator(world);
        getServer().getPluginManager().registerEvents(actuator, this);
        actuators.add(actuator);

        PurgeBosses(world);

        if (! configReader.GetPermanentBloodMoonConfig())
        {
            PeriodicNightCheck nightCheck = new PeriodicNightCheck(world, actuator);
            GetScheduler().runTaskLater(this, nightCheck, 0);
            getServer().getPluginManager().registerEvents(nightCheck, this);
            nightChecks.add(nightCheck);
            LoadCache(world);
        }

        bloodmoonWorlds.add(world);
    }


    /**
     * Removes all boss remaining from a world
     * @param world Chosen world
     */
    private void PurgeBosses (World world){
        for(LivingEntity entity : world.getLivingEntities()){
            boolean markedBoss = entity.getPersistentDataContainer().has(getBossKey(), PersistentDataType.STRING);
            boolean legacyBoss = entity.getCustomName() != null
                    && !entity.getCustomName().isEmpty()
                    && entity.getCustomName().equals(getLocaleReader().GetLocaleString("ZombieBossName"));
            if(markedBoss || legacyBoss){
                entity.remove();
            }
        }
    }

    /**
     * Disables the plugin
     */
    @Override
    public void onDisable()
    {
        for (PeriodicNightCheck nightCheck : nightChecks)
        {
            nightCheck.UpdateCacheDatabase();
        }

        for (BloodmoonActuator actuator : actuators)
        {
            if (actuator.isInProgress()) actuator.AbortBloodMoon();
            actuator.close();
        }

        if (sqlAccess != null) sqlAccess.close();
        if (sessionCoordinator != null) sessionCoordinator.abortAll();
        if (statisticsService != null) statisticsService.saveIfDirty();
        placeholderIntegration.close();
        mythicMobs.close();
        for (ConfigReader configReader : allConfigReaders)
        {
            if (configReader != null)
            {
                try
                {
                    configReader.close();
                }
                catch (IOException e)
                {
                    getLogger().log(Level.SEVERE,"[Error]");
                    e.printStackTrace();
                }
            }
        }
        try
        {
            localeReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void CheckOlderConfigs ()
    {
        getLogger().log(Level.INFO,"BloodMoon-Reborn v" + getDescription().getVersion() + " - multi-version rewards release");

        File oldConfig = new File (getDataFolder() + SLASH + CONFIG_FILE);
        if (oldConfig.exists()) getLogger().log(Level.WARNING,"[Deprecated] BloodMoon/config.yml is no longer used. Use per-world configuration instead");

        String localesVersion = getLocaleReader().GetFileVersion();
        if (localesVersion.equals("NaN"))
        {
            getLogger().log(Level.WARNING,"[Error] locales.yml has no valid version tag. Consider regenerating it");
            return;
        }
        if (! GetMajorVersions(localesVersion).equals(GetMajorVersions(getDescription().getVersion())))
            getLogger().log(Level.WARNING,"[Warning] Locales file was not updated since the last major update. Regenerating it is *highly* recommended");
        for (World world : bloodmoonWorlds)
        {
            if (BlackListedWorlds.contains(world)) continue;

            String configVersion = getConfigReader(world).GetFileVersion();
            if (configVersion.equals("NaN"))
            {
                getLogger().log(Level.SEVERE,"[Error] " + world.getName() + "/config.yml has no valid version tag. Consider regenerating it");
                return;
            }
            if (! GetMajorVersions(configVersion).equals(GetMajorVersions(getDescription().getVersion())))
                getLogger().log(Level.WARNING, "[Warning] Config file for world " + world.getName() + " was not updated since the last major update. Regenerating it is *highly* recommended");
            if (getConfigReader(world).GetIntervalConfig() < 1)
                getLogger().log(Level.WARNING,"[Warning] BloodMoonInterval config is set to 0 or less.\nThis may cause problem, please use the PermanentBloodMoon option instead");

        }
    }

    private static String GetMajorVersions (String version)
    {
        String[] segments = version.split("\\.");
        return segments.length >= 2 ? segments[0] + "." + segments[1] : version;
    }
}
