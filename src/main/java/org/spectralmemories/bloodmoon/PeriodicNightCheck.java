package org.spectralmemories.bloodmoon;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.spectralmemories.sqlaccess.FieldType;
import org.spectralmemories.sqlaccess.SQLAccess;
import org.spectralmemories.sqlaccess.SQLField;
import org.spectralmemories.bloodmoon.lifecycle.AutomaticStartPolicy;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class PeriodicNightCheck implements Runnable, Listener, AutoCloseable
{

    public static final int DAY = 24000;

    private static Map<java.util.UUID, PeriodicNightCheck> nightChecks;

    private long checkupAfter;
    private int daysBeforeBloodMoon;
    private boolean manualStartRequested;
    private boolean recoveryLogged;
    private boolean closed;

    private World world;
    private BloodmoonActuator actuator;

    public PeriodicNightCheck(World world, BloodmoonActuator actuator)
    {
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        this.actuator = actuator;
        this.world = world;
        daysBeforeBloodMoon = (configReader.GetIntervalConfig() - 1); //Workaround
        checkupAfter = world.getFullTime();

        AddCheck(this);
    }

    private static void AddCheck (PeriodicNightCheck instance)
    {
        if (nightChecks == null) nightChecks = new HashMap<>();

        nightChecks.put(instance.GetWorld().getUID(), instance);
    }

    public static int GetDaysRemaining (World world)
    {
        PeriodicNightCheck instance = world == null || nightChecks == null ? null : nightChecks.get(world.getUID());

        if (instance != null) return (instance.GetRemainingDays() + 1);

        return -1;
    }

    public World GetWorld ()
    {
        return world;
    }

    public static PeriodicNightCheck GetPeriodicNightCheck (World world)
    {
        try
        {
            return world == null ? null : nightChecks.get(world.getUID());
        }
        catch (Exception ignored){}
        return null;
    }

    public int GetBloodMoonInterval ()
    {
        return Bloodmoon.GetInstance().getConfigReader(world).GetIntervalConfig();
    }

    public int GetRemainingDays ()
    {
        return daysBeforeBloodMoon;
    }

    public void SetDaysRemaining (int remaining)
    {
        daysBeforeBloodMoon = remaining;
    }

    public void SetCheckAfter (long time)
    {
        checkupAfter = time;
    }

    public long GetCheckAfter ()
    {
        return checkupAfter;
    }

    public void RequestManualStart ()
    {
        manualStartRequested = true;
    }

    public void ClearRestartSuppression ()
    {
        Bloodmoon.GetInstance().getAbortedNightStore().clear(world.getUID());
        recoveryLogged = false;
    }

    public boolean PrepareAbortedShutdown (String cause)
    {
        if (!actuator.isInProgress()) return false;
        Bloodmoon.GetInstance().getAbortedNightStore().mark(
                world.getUID(), world.getName(), world.getFullTime(), cause);
        ResetScheduleAfterConsumedNight();
        return true;
    }

    public void RecoverIncompleteSession (long recordedCycle)
    {
        long cycle = recordedCycle >= 0 ? recordedCycle
                : org.spectralmemories.bloodmoon.lifecycle.NightCycle.identity(world.getFullTime());
        Bloodmoon.GetInstance().getAbortedNightStore().markCycle(
                world.getUID(), world.getName(), cycle, "unexpected-stop");
        ResetScheduleAfterConsumedNight();
    }

    public boolean RestoreRestartSuppression ()
    {
        boolean suppressed = Bloodmoon.GetInstance().getAbortedNightStore().suppresses(
                world.getUID(), world.getFullTime(), world.getTime());
        if (!suppressed) return false;
        ResetScheduleAfterConsumedNight();
        LogRestartSuppression();
        return true;
    }

    public void UpdateCacheDatabase ()
    {

        String worldUid = world.getUID().toString();
        String tableName = "lastBloodMoon";
        SQLAccess access = Bloodmoon.GetInstance().getSqlAccess();
        boolean exists = false;
        try
        {
            exists = access.EntryExist (tableName, new SQLField(
                            "world",
                            FieldType.TEXT,
                            true,
                            false), worldUid);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        String sql;
        int days = daysBeforeBloodMoon;
        long checkAt = checkupAfter;
        if (exists)
        {
            sql = "UPDATE " + tableName + " SET days = " + (days + 1) + ", checkAt = " + checkAt;
            sql += " WHERE world = '" + worldUid + "';";
        }
        else
        {
            sql = "INSERT INTO " + tableName + " VALUES('" + worldUid + "', " + (days + 1) + ", " + checkAt + ");";
        }

        try
        {
            if (! access.ExecuteSQLOperation(sql))
            {
                System.out.println("[Warning] There was an issue updating plugin");
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }

    @Override
    public void run()
    {
        if (closed) return;
        Bloodmoon.GetInstance().GetScheduler().runTaskLater(Bloodmoon.GetInstance(), this, Bloodmoon.NIGHT_CHECK_DELAY);

        /*

        This code is quite messy honestly. A complete rewrite of the class should be planned

        but, it works, so for this alpha version, this is fine

        */

        // Also expires a consumed-night marker as soon as the world's full-time cycle advances.
        Bloodmoon.GetInstance().getAbortedNightStore().suppresses(
                world.getUID(), world.getFullTime(), world.getTime());
        Check11();
        Check13();
        CheckDay();
    }

    @Override
    public void close()
    {
        closed = true;
        HandlerList.unregisterAll(this);
        if (nightChecks != null && world != null) nightChecks.remove(world.getUID(), this);
    }

    private void Check11 ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        if (world.getTime() > 11000)
        {
            if (world.getFullTime() <= checkupAfter) return;
            if (daysBeforeBloodMoon > 0)
            {

                if (daysBeforeBloodMoon == 1)
                {
                    LocaleReader.MessageAllLocale("BloodMoonTomorrow", null, null, world);
                }
                else
                {
                    LocaleReader.MessageAllLocale("DaysBeforeBloodMoon", new String[]{"$d"}, new String[]{String.valueOf(daysBeforeBloodMoon)}, world);
                }
                SetDaysRemaining(daysBeforeBloodMoon - 1);
                checkupAfter = getNextEvening();
                return;
            }

            //Day 0: prepare for Blood Moon
            LocaleReader.MessageAllLocale("BloodMoonTonight", null, null, world);
            checkupAfter = getTodayZero() + 12000;
        }
    }
    private void Check13 ()
    {
        //Check if its Blood Moon night, then time is over 13000, and if its the day after the day 0 warning
        boolean eligible = world.getFullTime() >= checkupAfter
                && world.getTime() >= 12000 && daysBeforeBloodMoon == 0;
        if (eligible)
        {
            boolean suppressed = Bloodmoon.GetInstance().getAbortedNightStore().suppresses(
                    world.getUID(), world.getFullTime(), world.getTime());
            if (!AutomaticStartPolicy.mayStart(true, manualStartRequested, suppressed))
            {
                ResetScheduleAfterConsumedNight();
                LogRestartSuppression();
                return;
            }
            if (manualStartRequested)
            {
                manualStartRequested = false;
                ClearRestartSuppression();
            }

            actuator.StartBloodMoon();

            ResetScheduleAfterConsumedNight();
        }
    }
    private void CheckDay ()
    {
        LocaleReader localeReader = Bloodmoon.GetInstance().getLocaleReader();
        ConfigReader configReader = Bloodmoon.GetInstance().getConfigReader(world);
        if (actuator.isInProgress())
        {
            //If Blood Moon is in progress but its daytime, stop it
            if (world.getTime() < 12000)
            {

                actuator.StopBloodMoon();
                LocaleReader.MessageAllLocale("BloodMoonEndingMessage", null, null, world);
                for (Player player : world.getPlayers())
                {
                    if (configReader.GetBloodMoonEndSoundConfig())
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0f, 1.2f);
                }
                checkupAfter = 0; //This avoids some bugs when players use /time set 0
            }

        }

    }

    private long getNextEvening ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return ((currentTime - remaining) + DAY + 11000);
    }

    private long getTodayZero ()
    {
        long currentTime = world.getFullTime();
        long remaining = currentTime % DAY;

        return (currentTime - remaining);
    }

    private void ResetScheduleAfterConsumedNight ()
    {
        checkupAfter = getNextEvening();
        daysBeforeBloodMoon = Math.max(0,
                Bloodmoon.GetInstance().getConfigReader(world).GetIntervalConfig() - 1);
    }

    private void LogRestartSuppression ()
    {
        if (recoveryLogged) return;
        recoveryLogged = true;
        Bloodmoon.GetInstance().getLogger().warning(
                "Previous active Blood Moon in world " + world.getName() + " was aborted after restart.");
        Bloodmoon.GetInstance().getLogger().warning(
                "Automatic restart is suppressed for the remainder of this night.");
    }


    //We need to make sure the checkAt var is reset when time is manually changed
    @EventHandler
    public void onCommandIssued (PlayerCommandPreprocessEvent event)
    {
        Player sender = event.getPlayer();
        String command = event.getMessage();

        if (command.startsWith("/time set")
            || command.startsWith("/night")
            || command.startsWith("/day")
        )
        {
            if (sender.getWorld() == world && !event.isCancelled())
            {
                SetCheckAfter(0);
            }
        }
    }
}
