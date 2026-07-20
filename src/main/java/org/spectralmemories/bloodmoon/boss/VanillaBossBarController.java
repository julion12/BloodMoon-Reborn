package org.spectralmemories.bloodmoon.boss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.spectralmemories.bloodmoon.Bloodmoon;
import org.spectralmemories.bloodmoon.ConfigReader;
import org.spectralmemories.bloodmoon.command.PlaceholderEngine;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Bukkit BossBar owned only by the legacy vanilla Blood Moon boss. */
public final class VanillaBossBarController {
    private static final long UPDATE_TICKS = 10L;

    private final LivingEntity host;
    private final ConfigReader config;
    private final String bossName;
    private final VanillaBossBarLifecycle lifecycle = new VanillaBossBarLifecycle();
    private BossBar bar;
    private int updateTask = -1;
    private double maximumHealth;

    public VanillaBossBarController(LivingEntity host, ConfigReader config, String bossName) {
        this.host = host;
        this.config = config;
        this.bossName = bossName == null ? "" : bossName;
        this.maximumHealth = effectiveHealth();
    }

    public void refresh() {
        if (!host.isValid() || host.isDead()) {
            close();
            return;
        }
        VanillaBossBarLifecycle.Transition transition = lifecycle.refresh(config.GetVanillaBossBarEnabled(), "VANILLA");
        if (transition == VanillaBossBarLifecycle.Transition.REMOVE) {
            removeBar();
            return;
        }
        if (transition == VanillaBossBarLifecycle.Transition.CREATE) {
            bar = Bukkit.createBossBar("", parseColor(config.GetVanillaBossBarColor()),
                    parseStyle(config.GetVanillaBossBarStyle()));
        }
        if (bar == null) return;
        updateBar();
        if (updateTask < 0) {
            updateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bloodmoon.GetInstance(), this::refresh,
                    UPDATE_TICKS, UPDATE_TICKS);
        }
    }

    public void close() {
        lifecycle.close();
        removeBar();
    }

    public boolean isActive() { return lifecycle.active(); }

    private void updateBar() {
        double current = effectiveHealth();
        maximumHealth = Math.max(maximumHealth, current);
        bar.setProgress(VanillaBossBarValues.progress(current, maximumHealth));
        bar.setColor(parseColor(config.GetVanillaBossBarColor()));
        bar.setStyle(parseStyle(config.GetVanillaBossBarStyle()));
        String template = config.GetVanillaBossBarShowHealthNumbers()
                ? config.GetVanillaBossBarTitle() : "%boss_name%";
        String title = PlaceholderEngine.replace(template,
                VanillaBossBarValues.placeholders(bossName, current, maximumHealth));
        bar.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        updateAudience();
    }

    private double effectiveHealth() {
        return Math.max(0.0, host.getHealth()) + Math.max(0.0, host.getAbsorptionAmount());
    }

    private void updateAudience() {
        Set<Player> wanted = new HashSet<>();
        String audience = config.GetVanillaBossBarAudience().trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(audience)) {
            wanted.addAll(Bukkit.getOnlinePlayers());
        } else if ("WORLD".equals(audience)) {
            wanted.addAll(host.getWorld().getPlayers());
        } else {
            double range = Math.max(0, config.GetVanillaBossBarViewDistance());
            double rangeSquared = range * range;
            for (Player player : host.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(host.getLocation()) <= rangeSquared) wanted.add(player);
            }
        }
        for (Player player : bar.getPlayers().toArray(Player[]::new)) {
            if (!wanted.contains(player)) bar.removePlayer(player);
        }
        for (Player player : wanted) {
            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        }
    }

    private void removeBar() {
        if (updateTask >= 0) {
            Bukkit.getScheduler().cancelTask(updateTask);
            updateTask = -1;
        }
        if (bar != null) bar.removeAll();
        bar = null;
    }

    private static BarColor parseColor(String configured) {
        try { return BarColor.valueOf(configured.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException ignored) { return BarColor.RED; }
    }

    private static BarStyle parseStyle(String configured) {
        try { return BarStyle.valueOf(configured.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException ignored) { return BarStyle.SEGMENTED_10; }
    }
}
