package com.fluxcraft.fXChat.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public final class PaperScheduler implements SchedulerAdapter {
    private final Plugin plugin;
    public PaperScheduler(Plugin plugin) { this.plugin = plugin; }
    @Override public void runGlobal(Runnable task) { Bukkit.getScheduler().runTask(plugin, task); }
    @Override public void runRegion(Location location, Runnable task) { Bukkit.getScheduler().runTask(plugin, task); }
}
