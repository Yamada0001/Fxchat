package com.fluxcraft.fXChat.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
public interface SchedulerAdapter {
    void runAsync(Runnable task);
    void runGlobal(Runnable task);
    void runRegion(Location location, Runnable task);
    default void close() {}
    static SchedulerAdapter create(Plugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return new FoliaScheduler(plugin);
        } catch (ClassNotFoundException e) {
            return new PaperScheduler(plugin);
        }
    }
}
