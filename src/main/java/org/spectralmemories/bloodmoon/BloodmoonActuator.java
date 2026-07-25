package org.spectralmemories.bloodmoon;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.AnimalTamer;
import org.spectralmemories.bloodmoon.command.CommandExecutionMode;
import org.spectralmemories.bloodmoon.session.BloodMoonSession;
import org.spectralmemories.bloodmoon.session.BossSessionState;
import org.spectralmemories.bloodmoon.boss.BossModeResolver;
import org.spectralmemories.bloodmoon.boss.BossNameResolver;
import org.spectralmemories.bloodmoon.boss.BossRewardPolicy;
import org.spectralmemories.bloodmoon.boss.ConfiguredBossSpawner;
import org.spectralmemories.bloodmoon.boss.SpawnedBossResult;
import org.spectralmemories.bloodmoon.boss.VanillaBossBarValues;
import org.spectralmemories.bloodmoon.integration.SpawnedMythicMob;
import org.spectralmemories.bloodmoon.placeholder.BossPlaceholderSnapshot;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the class that handles most interaction during a BloodMoon
 */
public class BloodmoonActuator implements Listener, Runnable, Closeable
{
    //Eligible mobs
    public final EntityType[] rewardedTypes = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.WITCH,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.PHANTOM,
            EntityType.ENDERMAN
    };

    private int originalMaxSpawn = 0;

    private static Map<World, BloodmoonActuator> actuators;

    private World world;
    private boolean inProgress;

    private BossBar nightBar;
    private ActuatorPeriodic actuatorPeriodic;

    private List<LivingEntity> blacklistedMobs;
    private List<IBoss> bosses;
    private final Set<UUID> rewardedBosses = new HashSet<>();
    private final Set<UUID> administrativelyRemovedBosses = new HashSet<>();
    private final Set<UUID> historicallyDefeatedBosses = new HashSet<>();
    private final Map<UUID, UUID> bossDamagers = new HashMap<>();
    private BloodMoonSession session;
    private final Map<UUID, String> mythicBosses = new LinkedHashMap<>();
    private final Map<UUID, TrackedPlaceholderBoss> placeholderBosses = new LinkedHashMap<>();
    private UUID currentPlaceholderBossId;
    private final AtomicReference<BossPlaceholderSnapshot> bossPlaceholderSnapshot =
            new AtomicReference<>(BossPlaceholderSnapshot.none());

    private void AddActuator (BloodmoonActuator instance)
    {
        if (actuators == null) actuators = new HashMap<>();

        actuators.put(instance.world, instance);
    }

    public static BloodmoonActuator GetActuator (World world)
    {
        try
        {
            return actuators.get(world);
        } catch (Exception ignored)
        {
        }
        return null;
    }

    public static void RefreshAllBossBars() {
        if (actuators == null) return;
        for (BloodmoonActuator actuator : actuators.values()) {
            for (IBoss boss : actuator.bosses) boss.RefreshDisplay();
        }
    }


    public BloodmoonActuator (World world)
    {
        this.world = world;
        inProgress = false;
        AddActuator(this);
        blacklistedMobs = new ArrayList<>();

        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            StartBloodMoon();
        }

        bosses = new ArrayList<>();
    }

    public void StartBloodMoon ()
    {
        if (inProgress) return;
        inProgress = true;
        session = Bloodmoon.GetInstance().getSessionCoordinator().start(world);
        bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.notSpawned());
        RunPreCommand();

        ShowNightBar();
        BroadcastBloodMoonWarning();

        actuatorPeriodic = new ActuatorPeriodic(world);
        actuatorPeriodic.run();

        SpawnBosses();
        StartSpawningOfHordes();

        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);

        originalMaxSpawn = world.getMonsterSpawnLimit();
        world.setMonsterSpawnLimit(reader.GetSpawnRateConfig());
    }

    public void StopBloodMoon ()
    {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            return;
        }
        stopBloodMoon(true);
    }

    public void AbortBloodMoon ()
    {
        stopBloodMoon(false);
    }

    private void stopBloodMoon(boolean complete)
    {
        if (!inProgress && !Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig()) return;
        inProgress = false;

        StopStorm();
        HideNightBar();

        if (actuatorPeriodic != null) actuatorPeriodic.close();
        actuatorPeriodic = null;
        blacklistedMobs.clear();
        if (complete) {
            KillBosses();
        } else {
            // Plugin disable happens after Bukkit has stopped accepting new tasks. An abort must
            // remove bosses immediately, without delayed lightning, rewards, or permanent respawn.
            KillBosses(false, false, false);
        }
        world.setMonsterSpawnLimit(originalMaxSpawn);
        BloodMoonSession finished = Bloodmoon.GetInstance().getSessionCoordinator().finish(world, complete);
        session = finished;
        if (complete) RunPostCommand();
        session = null;
        bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.none());
    }

    public void KillBosses ()
    {
        KillBosses(false);
    }

    public void KillBosses (boolean giveRewards)
    {
        KillBosses(giveRewards, true);
    }

    public void KillBosses (boolean giveRewards, boolean effects)
    {
        KillBosses(giveRewards, effects, true);
    }

    public void KillBosses (boolean giveRewards, boolean effects, boolean respawn)
    {
        BossPlaceholderSnapshot beforeRemoval = bossPlaceholderSnapshot.get();
        Set<UUID> removedBossIds = new LinkedHashSet<>();
        for (IBoss boss : bosses) removedBossIds.add(boss.GetHost().getUniqueId());
        removedBossIds.addAll(mythicBosses.keySet());
        Iterator var2 = bosses.iterator();

        while (var2.hasNext())
        {
            IBoss IBoss = (IBoss) var2.next();
            IBoss.Kill(giveRewards, effects, respawn);
        }

        bosses.clear();
        for (UUID mythicBossId : new ArrayList<>(mythicBosses.keySet())) {
            administrativelyRemovedBosses.add(mythicBossId);
            Bloodmoon.GetInstance().getMythicMobs().remove(mythicBossId);
            forgetBossLifecycle(mythicBossId);
        }
        removedBossIds.forEach(this::forgetBossLifecycle);
        mythicBosses.clear();
        placeholderBosses.clear();
        currentPlaceholderBossId = null;
        if (session != null) removedBossIds.forEach(session::bossRemoved);
        if (beforeRemoval.state() == BossSessionState.ALIVE) {
            bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.defeated(
                    beforeRemoval.boss().name(), beforeRemoval.boss().type()));
        }
    }

    public void SpawnHorde ()
    {
        Random random = new Random();
        Player[] players = world.getPlayers().toArray(new Player[0]);
        if(players.length > 0)
        {
            SpawnHorde(players[random.nextInt(players.length)]);
        }
    }

    public void SpawnHorde (Player target)
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        if (!reader.GetHordeEnabled()) return;

        Random random = new Random();

        Location hordeSpawnLocation;
        hordeSpawnLocation = target.getLocation().clone();

        int minMob = reader.GetHordeMinPopulation();
        int maxMob = reader.GetHordeMaxPopulation();
        int mobAmount = random.nextInt(maxMob - minMob) + minMob;
        int maxDistance = reader.GetHordeSpawnDistance();

        for (int i = 0; i < mobAmount; i++)
        {
            String[] mobList = reader.GetHordeMobWhitelist();
            int mobListLen = mobList.length;
            EntityType mobType = EntityType.valueOf(mobList[random.nextInt(mobListLen)]);

            Location newMobLocation = hordeSpawnLocation.clone();
            if (random.nextBoolean())
            {
                newMobLocation = newMobLocation.add(random.nextInt(maxDistance), 0, random.nextInt(maxDistance));
            } else
            {
                newMobLocation = newMobLocation.subtract(random.nextInt(maxDistance), 0, random.nextInt(maxDistance));
            }

            newMobLocation.setY(world.getHighestBlockYAt(newMobLocation));

            world.spawnEntity(newMobLocation, mobType);
            world.strikeLightningEffect(newMobLocation);
        }
        LocaleReader.MessageAllLocale("HordeArrived", new String[]{"$p"}, new String[]{target.getDisplayName()}, world);
    }

    public void StartSpawningOfHordes ()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        Random random = new Random();

        if (!reader.GetHordeEnabled()) return;

        int min = reader.GetHordeSpawnrateBaseline() - reader.GetHordeSpawnrateVariation();
        int max = reader.GetHordeSpawnrateBaseline() + reader.GetHordeSpawnrateVariation();

        Bloodmoon.GetInstance().GetScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable()
        {
            @Override
            public void run ()
            {
                if (isInProgress())
                {
                    SpawnHorde();
                    Bloodmoon.GetInstance().GetScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), this, random.nextInt(max - min) + max);
                }
            }
        }, random.nextInt(max - min) + max);
    }


    private void RunPreCommand ()
    {
        Bloodmoon.GetInstance().getSessionCoordinator().commandRunner().run(
                Bloodmoon.GetInstance().getConfigReader(world).GetPreBloodMoonCommands(),
                CommandExecutionMode.SERVER_ONCE, world, session, world.getPlayers(), Map.of());
    }

    private void RunPostCommand ()
    {
        Bloodmoon.GetInstance().getSessionCoordinator().commandRunner().run(
                Bloodmoon.GetInstance().getConfigReader(world).GetPostBloodMoonCommands(),
                CommandExecutionMode.SERVER_ONCE, world, session, world.getPlayers(), Map.of());
    }

    public void SpawnBosses ()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        Bloodmoon.GetInstance().getServer().getScheduler().scheduleSyncDelayedTask(Bloodmoon.GetInstance(), new Runnable()
        {
            public void run ()
            {
                if (reader.GetBossMode().equalsIgnoreCase("MYTHICMOBS") || reader.GetEnableZombieBossConfig()) {
                    SpawnConfiguredBoss();
                }
            }
        }, (long) ((new Random()).nextInt(2000) + 400));
    }

    public SpawnedBossResult SpawnConfiguredBoss()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        SpawnedBossResult result = ConfiguredBossSpawner.spawn(reader.GetBossMode(), reader.GetMythicMobsEnabled(),
                Bloodmoon.GetInstance().getMythicMobs().available(), reader.GetMythicFallbackToVanilla(),
                this::spawnVanillaBoss, this::spawnMythicBoss);
        if (result.fallbackUsed()) {
            logLocalizedWarning("MythicMobsFallbackToVanilla", new String[]{"%world%"},
                    new String[]{world.getName()});
        } else if (!result.success() && result.status() == SpawnedBossResult.Status.FAILED
                && reader.GetBossMode().equalsIgnoreCase("MYTHICMOBS")) {
            logLocalizedWarning("MythicBossNotFound", new String[]{"%boss_name%", "%world%"},
                    new String[]{reader.GetMythicMobInternalName(), world.getName()});
        }
        if (result.success()) {
            boolean firstSpawn = session == null || session.bossSpawned(
                    result.entityUuid(), result.displayName(), result.actualMode().name());
            Bloodmoon.GetInstance().getStatisticsService().recordBossSpawned(firstSpawn);
            LocaleReader.MessageAllLocale("ZombieBossSpawned",
                    new String[]{"$b", "%boss_name%", "%boss_type%"},
                    new String[]{result.displayName(), result.displayName(), result.actualMode().name()}, world);
        }
        return result;
    }

    private SpawnedBossResult spawnVanillaBoss()
    {
        if (world.getPlayers().size() > 0)
        {
            List<Player> players = world.getPlayers();
            Random rnd = new Random();
            int index = rnd.nextInt(players.size());
            Player chosenOne = (Player) players.get(index);
            Location spawn = chosenOne.getLocation();
            Location newLocation = spawn.clone();
            newLocation.add((double) (rnd.nextInt(10) + 10), 0.0D, (double) (rnd.nextInt(10) + 10));
            newLocation.setY((double) world.getHighestBlockYAt(newLocation));
            ZombieIBoss zombieBoss = new ZombieIBoss(newLocation);
            zombieBoss.Start();
            bosses.add(zombieBoss);
            trackPlaceholderBoss(zombieBoss.GetHost(), zombieBoss.GetName(), "VANILLA", true);
            return SpawnedBossResult.success(BossModeResolver.Mode.VANILLA,
                    zombieBoss.GetHost().getUniqueId(), zombieBoss.GetName(), zombieBoss.HasActiveBossBar());
        }
        return SpawnedBossResult.failed();
    }

    private SpawnedBossResult spawnMythicBoss()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        if (!Bloodmoon.GetInstance().getMythicMobs().available()) {
            logLocalizedWarning("MythicMobsUnavailable", new String[]{"%world%"},
                    new String[]{world.getName()});
            return SpawnedBossResult.failed();
        }
        if (world.getPlayers().isEmpty()) return SpawnedBossResult.failed();
        Player target = world.getPlayers().get(new Random().nextInt(world.getPlayers().size()));
        Location location = target.getLocation().clone().add(10, 0, 10);
        location.setY(world.getHighestBlockYAt(location));
        Optional<SpawnedMythicMob> spawned = Bloodmoon.GetInstance().getMythicMobs().spawn(
                reader.GetMythicMobInternalName(), location, reader.GetUseMythicMobsRewards(), this::HandleMythicBossDeath);
        if (spawned.isEmpty()) return SpawnedBossResult.failed();
        LivingEntity entity = spawned.get().entity();
        UUID mythicBossId = entity.getUniqueId();
        BossNameResolver.ResolvedBossName resolved = BossNameResolver.resolve("MYTHICMOBS",
                spawned.get().entityDisplayName(), spawned.get().configuredDisplayName(),
                reader.GetMythicMobInternalName(), "",
                Bloodmoon.GetInstance().getLocaleReader().GetLocalePlainString("MythicBossFallbackName"));
        String mythicBossName = resolved.name();
        mythicBosses.put(mythicBossId, mythicBossName);
        trackPlaceholderBoss(entity, mythicBossName, "MYTHICMOBS", false);
        entity.getPersistentDataContainer().set(Bloodmoon.GetInstance().getBossKey(),
                org.bukkit.persistence.PersistentDataType.STRING, "MYTHICMOBS");
        return SpawnedBossResult.success(BossModeResolver.Mode.MYTHICMOBS,
                mythicBossId, mythicBossName, false);
    }

    private void HandleMythicBossDeath(LivingEntity entity, Player killer)
    {
        boolean administrativeRemoval = administrativelyRemovedBosses.remove(entity.getUniqueId());
        String mythicBossName = mythicBosses.remove(entity.getUniqueId());
        if (mythicBossName == null) return;
        if (!administrativeRemoval) {
            boolean firstDefeat = session == null
                    ? historicallyDefeatedBosses.add(entity.getUniqueId())
                    : session.bossDefeated(entity.getUniqueId());
            Bloodmoon.GetInstance().getStatisticsService().recordBossDefeated(firstDefeat);
        } else if (session != null) {
            session.bossRemoved(entity.getUniqueId());
        }
        untrackPlaceholderBoss(entity.getUniqueId());
        ConfigReader config = Bloodmoon.GetInstance().getConfigReader(world);
        if (killer != null) {
            LocaleReader.MessageAllLocale("BossSlain",
                    new String[]{"$b", "$p", "%boss_name%", "%boss_type%"},
                    new String[]{mythicBossName, killer.getName(), mythicBossName, "MYTHICMOBS"}, world);
        }
        if (config.GetRunBloodMoonRewardCommandsForMythic()) {
            runBossRewardCommands(mythicBossName, "MYTHICMOBS", entity, killer);
        }
        forgetBossLifecycle(entity.getUniqueId());
    }

    public void AddToBlacklist (LivingEntity entity)
    {
        blacklistedMobs.add(entity);
    }

    private void StopStorm ()
    {
        world.setStorm(false);
        world.setThundering(false);
    }

    private void ShowNightBar ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetPermanentBloodMoonConfig()) return; //disable nightbar if permanent BM

        if (configReader.GetDarkenSkyConfig())
        {
            nightBar = Bukkit.createBossBar(localeReader.GetLocaleString("BloodMoonTitleBar"),
                    BarColor.RED,
                    BarStyle.SEGMENTED_12,
                    BarFlag.CREATE_FOG,
                    BarFlag.DARKEN_SKY
            );
        } else
        {
            nightBar = Bukkit.createBossBar(localeReader.GetLocaleString("BloodMoonTitleBar"),
                    BarColor.RED,
                    BarStyle.SEGMENTED_12
            );
        }
        nightBar.setProgress(0.0);
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, 0);

        List<Player> players = world.getPlayers();
        for (Player player : players)
        {
            nightBar.addPlayer(player);
        }

        UpdateNightBar();
    }


    private void HideNightBar ()
    {
        if (nightBar != null) nightBar.removeAll();
        nightBar = null;
    }

    private void HideNightBarPlayer (Player player)
    {
        try
        {
            if (nightBar != null) nightBar.removePlayer(player);
        } catch (Exception ignored)
        {
        }
    }

    private void UpdateNightBar ()
    {
        if (Bloodmoon.GetInstance().getConfigReader(world).GetPermanentBloodMoonConfig())
        {
            if (nightBar != null) nightBar.setProgress(1.0);
            return;
        }
        long timeTotal = 12000;
        long currentTime = world.getTime();
        long timeLeft = PeriodicNightCheck.DAY - currentTime;

        double percent = (double) timeLeft / (double) timeTotal;

        if (nightBar != null && percent >= 0.0 && percent <= 1.0f) nightBar.setProgress(1.0 - percent);
    }

    private void HandleReconnectingPlayer (Player player)
    {
        if (isInProgress() && nightBar != null) nightBar.addPlayer(player);
        BroadcastBloodMoonWarningPlayer(player);
    }

    private void BroadcastBloodMoonWarning ()
    {
        for (Player player : world.getPlayers())
        {
            BroadcastBloodMoonWarningPlayer(player);
        }
    }


    private void BroadcastBloodMoonWarningPlayer (Player player)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        LocaleReader.MessageLocale("BloodMoonWarningTitle", null, null, player);
        LocaleReader.MessageLocale("BloodMoonWarningBody", null, null, player);

        if (configReader.GetInventoryLossConfig())
        {
            LocaleReader.MessageLocale("DyingResultsInInventoryLoss", null, null, player);
        }
        if (configReader.GetExperienceLossConfig())
        {
            LocaleReader.MessageLocale("DyingResultsInExperienceLoss", null, null, player);
        }
    }

    /**
     * Generates a random item to be used as a reward
     *
     * @return
     */
    public ItemStack GetRandomBonus ()
    {

        Random random = new Random(); //We want to regenerate it every time to ensure randomness
        Material itemMaterial;

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String[] items = configReader.GetItemListConfig();
        Map<String, Integer[]> indexes = new HashMap<>();
        int totalWeight = 0;

        for (String entry : items)
        {
            String[] parts = entry.split(":");
            int itemWeight = Integer.parseInt(parts[2]);

            indexes.put(entry, new Integer[]{totalWeight, totalWeight + itemWeight});
            totalWeight += itemWeight;
        }

        int rng = random.nextInt(totalWeight);

        for (Map.Entry<String, Integer[]> entry : indexes.entrySet())
        {
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];

            if (rng >= min && rng < max)
            {
                String[] parts = entry.getKey().split(":");
                itemMaterial = Material.valueOf(parts[0]);

                ItemStack itemStack = new ItemStack(itemMaterial, Integer.parseInt(parts[1]));

                for (int i = 3; i < 6; i++)
                {
                    if (parts.length <= i) break;

                    String line = parts[i];
                    if (line.startsWith("$name"))
                    {
                        line = line.substring("$name".length() + 1);

                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName(line);
                        itemStack.setItemMeta(meta);
                    } else if (line.startsWith("$desc"))
                    {
                        line = line.substring("$desc".length() + 1);

                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setLore(Arrays.asList(line.split("\\$n")));
                        itemStack.setItemMeta(meta);
                    } else if (line.startsWith("$enchant"))
                    {
                        line = line.substring("$enchant".length() + 1);

                        String[] enchantLines = line.split(";");
                        for (String enchantLine : enchantLines)
                        {
                            String[] enchant = enchantLine.split(",");
                            itemStack.addEnchantment(
                                    Enchantment.getByKey(NamespacedKey.minecraft(enchant[0].toLowerCase()))
                                    , Integer.parseInt(enchant[1]));
                        }
                    }
                }

                return itemStack;
            }
        }
        return null;
    }

    public boolean IsInProtectedWGRegion (Player player){
        try
        {
            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery rq = rc.createQuery();
            ApplicableRegionSet rs = rq.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

            if (rs == null || rs.size() == 0) return false;

            boolean isProtected = !rs.testState(null, Flags.MOB_DAMAGE);
            return isProtected;
        }catch (NoClassDefFoundError e){
            //Server likely does not have WG
            return false;
        }
    }

    private void ApplySpecialEffect (Player player, LivingEntity mob)
    {
        if(IsInProtectedWGRegion(player)) return;

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        String mobTypeName = mob.getType().name().toUpperCase();
        for (IBoss boss : bosses)
        {
            if (boss.GetHost() == mob)
            {
                mobTypeName += "BOSS";
                break;
            }
        }

        String[] configs = configReader.GetMobEffectConfig(mobTypeName);

        for (String str : configs)
        {
            if (str.equals("LIGHTNING"))
            {
                world.strikeLightning(player.getLocation());
                continue;
            }


            String[] parts = str.split(",");
            PotionEffectType[] types = PotionEffectType.values();
            String effectName = parts[0];
            int ticks = (int) (20f * Float.parseFloat(parts[1]));
            int amp = Integer.parseInt(parts[2]);

            for (PotionEffectType type : types)
            {
                if (type.getName().equals(effectName))
                {
                    player.addPotionEffect(new PotionEffect(type, ticks, amp));
                    break;
                }
            }
            //Effect not found. Meh
        }
    }

    public boolean isInProgress ()
    {
        ConfigReader reader = Bloodmoon.GetInstance().getConfigReader(world);
        return inProgress || reader.GetPermanentBloodMoonConfig();
    }

    public BossPlaceholderSnapshot getBossPlaceholderSnapshot() {
        return bossPlaceholderSnapshot.get();
    }

    public static void refreshAllBossPlaceholderSnapshotsOnMainThread() {
        if (actuators == null) return;
        for (BloodmoonActuator actuator : actuators.values()) {
            actuator.refreshBossPlaceholderSnapshotOnMainThread();
        }
    }

    public void refreshBossPlaceholderSnapshotOnMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Boss placeholder snapshots must be refreshed on the primary thread");
        }
        if (currentPlaceholderBossId == null) return;
        TrackedPlaceholderBoss tracked = placeholderBosses.get(currentPlaceholderBossId);
        if (tracked == null) return;
        LivingEntity entity = tracked.entity();
        if (!entity.isValid() || entity.isDead() || entity.getHealth() <= 0) {
            if (session != null) session.bossRemoved(currentPlaceholderBossId);
            untrackPlaceholderBoss(currentPlaceholderBossId);
            return;
        }
        double current = Math.max(0, entity.getHealth())
                + (tracked.includeAbsorption() ? Math.max(0, entity.getAbsorptionAmount()) : 0);
        double maximum = entityMaximumHealth(entity)
                + (tracked.includeAbsorption() ? Math.max(0, entity.getAbsorptionAmount()) : 0);
        bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.alive(
                tracked.name(), tracked.type(), current, maximum));
    }

    private void trackPlaceholderBoss(LivingEntity entity, String name, String type, boolean includeAbsorption) {
        double maximum = entityMaximumHealth(entity)
                + (includeAbsorption ? Math.max(0, entity.getAbsorptionAmount()) : 0);
        currentPlaceholderBossId = entity.getUniqueId();
        placeholderBosses.put(currentPlaceholderBossId,
                new TrackedPlaceholderBoss(entity, name, type, includeAbsorption));
        double current = Math.max(0, entity.getHealth())
                + (includeAbsorption ? Math.max(0, entity.getAbsorptionAmount()) : 0);
        bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.alive(
                name, type, current, Math.max(0, maximum)));
    }

    private void untrackPlaceholderBoss(UUID entityId) {
        TrackedPlaceholderBoss removed = placeholderBosses.remove(entityId);
        if (!entityId.equals(currentPlaceholderBossId)) return;
        currentPlaceholderBossId = null;
        for (UUID remaining : placeholderBosses.keySet()) currentPlaceholderBossId = remaining;
        if (currentPlaceholderBossId != null) {
            refreshBossPlaceholderSnapshotOnMainThread();
            return;
        }
        BossPlaceholderSnapshot current = bossPlaceholderSnapshot.get();
        String name = removed == null ? current.boss().name() : removed.name();
        String type = removed == null ? current.boss().type() : removed.type();
        bossPlaceholderSnapshot.set(BossPlaceholderSnapshot.defeated(name, type));
    }

    private static double entityMaximumHealth(LivingEntity entity) {
        return entity.getAttribute(Attribute.MAX_HEALTH) == null
                ? Math.max(entity.getHealth(), 1.0) : entity.getAttribute(Attribute.MAX_HEALTH).getValue();
    }

    private record TrackedPlaceholderBoss(LivingEntity entity, String name, String type,
                                          boolean includeAbsorption) { }


    //Events
    @EventHandler
    public void onPlayerConnect (PlayerJoinEvent event)
    {
        if (isInProgress() && event.getPlayer().getWorld() == world)
        {
            Bloodmoon.GetInstance().getSessionCoordinator().join(world, event.getPlayer());
            HandleReconnectingPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event)
    {
        if (isInProgress()) Bloodmoon.GetInstance().getSessionCoordinator().disconnect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport (PlayerTeleportEvent event)
    {
        World to = event.getTo().getWorld();
        World from = event.getFrom().getWorld();
        if (to != world && from != world) return; //None of our concern

        if (from != to)
        {
            if (to == world && isInProgress())
            {
                //Someone entered our bm world
                Bloodmoon.GetInstance().getSessionCoordinator().join(world, event.getPlayer());
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world && isInProgress())
            {
                //Someone left our bm world
                Bloodmoon.GetInstance().getSessionCoordinator().leave(world, event.getPlayer());
                HideNightBarPlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event)
    {
        World from = event.getPlayer().getWorld();
        World to = event.getRespawnLocation().getWorld();
        if (to != world && from != world) return; //None of our concern

        if (from != to)
        {
            if (to == world && isInProgress())
            {
                //Someone respawned in our bm world
                HandleReconnectingPlayer(event.getPlayer());
            }
            if (from == world)
            {
                //Someone respawned out of our bm world
                HideNightBarPlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon

        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        Player deadplayer = event.getEntity();
        if (deadplayer.getWorld() != world) return; //Wrong world

        Bloodmoon.GetInstance().getSessionCoordinator().death(world, deadplayer);

        if (configReader.GetLightningEffectConfig()) world.strikeLightningEffect(deadplayer.getLocation());

        Component deathMessage = event.deathMessage();
        if (deathMessage != null)
        {
            // Preserve Minecraft's translatable death component so each client renders the vanilla
            // cause in its own language; converting it to a legacy String forces the server locale.
            event.deathMessage(deathMessage.append(
                    Component.text(" " + localeReader.GetLocalePlainString("DeathSuffix"))));
        }

        if (configReader.GetExperienceLossConfig())
        {
            event.setNewTotalExp(0);
            event.setDroppedExp(0);
        }


        if (configReader.GetInventoryLossConfig()) event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerSleeps (PlayerBedEnterEvent event)
    {
        if (event.getPlayer().getWorld() == world)
        {
            if (isInProgress())
            {
                ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
                if (configReader.GetPreventSleepingConfig())
                {
                    LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();

                    LocaleReader.MessageLocale("BedNotAllowed", null, null, event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMobSpawn (SpawnerSpawnEvent event)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);

        if (configReader.GetMobsFromSpawnerNoRewardConfig() && event.getEntity().getWorld() == world && isInProgress())
        {
            for (EntityType type : rewardedTypes)
            {
                if (event.getEntityType() == type)
                {
                    if (event.getEntity() instanceof LivingEntity)
                    {
                        blacklistedMobs.add((LivingEntity) event.getEntity());
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMobDeath (EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();

        Iterator<IBoss> bossIterator = bosses.iterator();
        while (bossIterator.hasNext())
        {
            IBoss boss = bossIterator.next();
            if (entity == boss.GetHost())
            {
                Player killer = resolveBossKiller(boss.GetHost());
                if (killer != null)
                {

                    LocaleReader.MessageAllLocale("BossSlain",
                            new String[]{"$b", "$p", "%boss_name%", "%boss_type%"},
                            new String[]{boss.GetName(), killer.getName(), boss.GetName(), "VANILLA"}, world);
                }

                boss.Kill(killer != null && isInProgress());
                runBossRewardCommands(boss.GetName(), "VANILLA", boss.GetHost(), killer);
                boolean firstDefeat = session == null
                        ? historicallyDefeatedBosses.add(entity.getUniqueId())
                        : session.bossDefeated(entity.getUniqueId());
                Bloodmoon.GetInstance().getStatisticsService().recordBossDefeated(firstDefeat);
                untrackPlaceholderBoss(entity.getUniqueId());
                bossIterator.remove();
                forgetBossLifecycle(entity.getUniqueId());
                return;
            }
        }

        String bossMarker = entity.getPersistentDataContainer().get(Bloodmoon.GetInstance().getBossKey(),
                org.bukkit.persistence.PersistentDataType.STRING);
        if ("MYTHICMOBS".equals(bossMarker)) return; // MythicMobs owns its drops/rewards by default.

        if (!isInProgress()) return; //Only during BloodMoon

        if (event.getEntity() instanceof Player) return; //Handled in another method

        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);


        if (entity.getWorld() != world) return; //Wrong world

        if (blacklistedMobs.contains(entity))
        {
            //This mob was explicitely blacklisted. ignore it
            blacklistedMobs.remove(entity);
            return;
        }

        boolean eligible = false;
        for (EntityType type : rewardedTypes)
        {
            if (entity.getType() == type) eligible = true;
        }

        if (!eligible) return; //Not eligible for reward

        event.setDroppedExp(event.getDroppedExp() * configReader.GetExpMultConfig());

        if (configReader.GetMobDeathThunderConfig())
            world.strikeLightningEffect(event.getEntity().getLocation());

        List<ItemStack> bonusDrops = new ArrayList<>();

        int min = configReader.GetMinItemsDropConfig();
        int max = configReader.GetMaxItemsDropConfig();

        int itemCount = (max - min <= 0) ? min : new Random().nextInt(max - min) + min;

        for (int i = 0; i < itemCount; i++)
        {
            bonusDrops.add(GetRandomBonus()); //Add the drops
        }

        for (ItemStack item : bonusDrops)
        {
            world.dropItemNaturally(entity.getLocation(), item); //Drop items
        }
    }

    @EventHandler
    public void onEntityDamage (EntityDamageByEntityEvent event)
    {
        if (!isInProgress()) return; //Only during BloodMoon

        Entity receiver = event.getEntity();
        Entity damager = event.getDamager();

        if (receiver.getWorld() != world || damager.getWorld() != world) return; //Wrong world
        if (damager instanceof Projectile) //if its any damage dealing projectile
        {
            ProjectileSource source = ((Projectile) damager).getShooter(); //Get the shooter as Source
            if (source instanceof LivingEntity) //Source is a mob, not a block
            {
                damager = (LivingEntity) source;
            }
        }

        Player responsiblePlayer = responsiblePlayer(damager);
        if (responsiblePlayer != null && bosses.stream().anyMatch(boss -> boss.GetHost() == receiver)) {
            bossDamagers.put(receiver.getUniqueId(), responsiblePlayer.getUniqueId());
        }


        if (receiver instanceof LivingEntity && damager instanceof LivingEntity)
        {
            ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
            if (receiver instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (damager.getType() == type)
                    {
                        //Player is damaged by monster
                        if (event.getFinalDamage() == 0 && configReader.GetShieldPreventEffects())
                            return; //Hit was shielded. We shall not apply configs
                        event.setDamage(event.getDamage() * configReader.GetMobDamageMultConfig());
                        ApplySpecialEffect((Player) receiver, (LivingEntity) damager);
                        if (configReader.GetPlayerDamageSoundConfig())
                            ((Player) receiver).playSound(receiver.getLocation(), Sound.AMBIENT_CAVE, 80.0f, 1.5f);
                        if (configReader.GetPlayerHitParticleConfig())
                            world.spawnParticle(Particle.FLAME, receiver.getLocation(), 60);
                    }
                }
            } else if (damager instanceof Player)
            {
                for (EntityType type : rewardedTypes)
                {
                    if (receiver.getType() == type)
                    {
                        //Player dealt damage to monster
                        event.setDamage((int) Math.ceil(event.getDamage() / configReader.GetMobHealthMultConfig()));
                        if (configReader.GetMobHitParticleConfig())
                            world.spawnParticle(Particle.CRIT, receiver.getLocation(), 60);

                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld() != world) return;
        boolean tracked = placeholderBosses.containsKey(event.getEntity().getUniqueId());
        for (IBoss boss : bosses) {
            if (boss.GetHost() == event.getEntity()) {
                Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), () -> {
                    boss.RefreshDisplay();
                    refreshBossPlaceholderSnapshotOnMainThread();
                }, 1L);
                return;
            }
        }
        if (tracked) {
            Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(),
                    this::refreshBossPlaceholderSnapshotOnMainThread, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity().getWorld() != world
                || !placeholderBosses.containsKey(event.getEntity().getUniqueId())) return;
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(),
                this::refreshBossPlaceholderSnapshotOnMainThread, 1L);
    }

    /**
     * Runs the actuator's checkup routine. Called internally, you don't need to call it yourself
     */
    @Override
    public void run ()
    {
        if (isInProgress())
        {
            UpdateNightBar();
            Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, 20);
        }
    }

    /**
     * Closes the actuator. You should discard it after doing so
     */
    @Override
    public void close ()
    {
        if (bosses.isEmpty() && mythicBosses.isEmpty()) return; //Nothing to do

        KillBosses(false, false);
        world.save();
    }

    private Player responsiblePlayer(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Tameable tameable) {
            AnimalTamer owner = tameable.getOwner();
            if (owner instanceof Player player) return player;
        }
        return null;
    }

    private Player resolveBossKiller(LivingEntity boss) {
        Player killer = boss.getKiller();
        UUID playerId = bossDamagers.remove(boss.getUniqueId());
        if (killer != null) return killer;
        return playerId == null ? null : Bukkit.getPlayer(playerId);
    }

    private void forgetBossLifecycle(UUID bossId) {
        rewardedBosses.remove(bossId);
        administrativelyRemovedBosses.remove(bossId);
        historicallyDefeatedBosses.remove(bossId);
        bossDamagers.remove(bossId);
    }

    private void runBossRewardCommands(String bossName, String bossType, LivingEntity bossHost, Player killer) {
        ConfigReader config = Bloodmoon.GetInstance().getConfigReader(world);
        UUID bossId = bossHost.getUniqueId();
        boolean firstReward = rewardedBosses.add(bossId);
        if (!BossRewardPolicy.shouldReward(config.GetBossRewardsEnabled(), config.GetBossRequirePlayerKiller(),
                killer != null, config.GetBossRewardOnce(), firstReward)) return;
        Location location = bossHost.getLocation();
        Map<String, Object> values = new HashMap<>();
        double health = Math.max(0.0, bossHost.getHealth()) + Math.max(0.0, bossHost.getAbsorptionAmount());
        double maximumHealth = bossHost.getAttribute(Attribute.MAX_HEALTH) == null
                ? Math.max(health, 1.0) : bossHost.getAttribute(Attribute.MAX_HEALTH).getValue();
        if ("VANILLA".equalsIgnoreCase(bossType)) {
            maximumHealth += 4.0 * (config.GetZombieBossHealth() + 1);
        }
        values.putAll(VanillaBossBarValues.placeholders(bossName, bossType, health, maximumHealth));
        values.put("boss_uuid", bossId);
        values.put("boss_killer", killer == null ? "" : killer.getName());
        values.put("boss_killer_uuid", killer == null ? "" : killer.getUniqueId());
        values.put("boss_world", world.getName());
        values.put("boss_x", location.getBlockX());
        values.put("boss_y", location.getBlockY());
        values.put("boss_z", location.getBlockZ());
        List<Player> targets = killer == null ? List.of() : List.of(killer);
        Bloodmoon.GetInstance().getSessionCoordinator().commandRunner().run(config.GetBossRewardCommands(),
                CommandExecutionMode.SERVER_FOR_EACH_PLAYER, world, session, targets, values);
        Bloodmoon.GetInstance().getLogger().info("Boss command reward processed for " + bossId
                + (killer == null ? " without a killer" : "; killer=" + killer.getUniqueId()));
        if (killer != null) {
            LocaleReader.MessageLocale("BossRewardReceived", new String[]{"%boss_name%", "%boss_type%"},
                    new String[]{bossName == null ? "" : bossName, bossType == null ? "NONE" : bossType}, killer);
        }
    }

    private void logLocalizedWarning(String id, String[] placeholders, String[] replacements) {
        String message = Bloodmoon.GetInstance().getLocaleReader().GetLocalePlainString(id);
        if (placeholders != null && replacements != null && placeholders.length == replacements.length) {
            for (int i = 0; i < placeholders.length; i++) {
                message = message.replace(placeholders[i], replacements[i] == null ? "" : replacements[i]);
            }
        }
        Bloodmoon.GetInstance().getLogger().warning(message);
    }
}
