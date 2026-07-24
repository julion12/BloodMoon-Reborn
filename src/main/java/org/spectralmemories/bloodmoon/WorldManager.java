package org.spectralmemories.bloodmoon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldManager implements Listener
{
    @EventHandler
    public void onWorldLoad (WorldLoadEvent event)
    {
        //This method will check if the world is blacklisted itself
        Bloodmoon.GetInstance().LoadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        BloodmoonActuator actuator = BloodmoonActuator.GetActuator(event.getWorld());
        if (actuator != null) {
            PeriodicNightCheck nightCheck = PeriodicNightCheck.GetPeriodicNightCheck(event.getWorld());
            if (nightCheck != null) {
                nightCheck.PrepareAbortedShutdown("world-unload");
                nightCheck.UpdateCacheDatabase();
            }
            actuator.AbortBloodMoon();
            actuator.close();
        }
    }
}
