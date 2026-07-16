package com.fluxcraft.fXChat.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public final class FoliaScheduler implements SchedulerAdapter {
    private final Plugin plugin;
    public FoliaScheduler(Plugin plugin) { this.plugin = plugin; }
    @Override public void runGlobal(Runnable task) { plugin.getServer().getGlobalRegionScheduler().execute(plugin, task); }
    @Override public void runRegion(Location location, Runnable task) { plugin.getServer().getRegionScheduler().execute(plugin, location, task); }
}
