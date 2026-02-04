package com.fluxcraft.fXChat.feature;

import com.fluxcraft.fXChat.FXChat;
import com.fluxcraft.fXChat.scheduler.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BiomeManager {
    private final FXChat plugin;
    private final SchedulerAdapter scheduler;
    private final Map<UUID, String> biomeCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService autoUpdateExecutor;

    public BiomeManager(FXChat plugin, SchedulerAdapter scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;

        this.autoUpdateExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "FXChat-BiomeUpdater");
            thread.setDaemon(true);
            return thread;
        });
    }

    public String getCachedBiome(Player player) {
        if (!player.isOnline()) return "§c玩家离线";
        return biomeCache.getOrDefault(player.getUniqueId(), "计算中...");
    }

    public void clearCache() {
        biomeCache.clear();
    }

    public void close() {
        autoUpdateExecutor.shutdown();
    }

    public void startAutoUpdate() {
        if (scheduler instanceof com.fluxcraft.fXChat.scheduler.FoliaScheduler) {
            plugin.getLogger().severe("⚠️ [Folia 环境] 生物群系自动轮询功能已强制禁用以防服务器崩溃。");
            plugin.getLogger().warning("⚠️ Folia 架构下暂不支持后台全局扫描，生物群系占位符可能显示为 '计算中...'。");
            return;
        }

        int interval = plugin.getConfigManager().getBiomeUpdateInterval();

        autoUpdateExecutor.scheduleAtFixedRate(() -> {
            if (!plugin.isEnabled()) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerBiome(player);
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    private void updatePlayerBiome(Player player) {
        Location loc = player.getLocation();

        scheduler.runRegion(loc, () -> {
            if (!player.isOnline()) return;
            Location cur = player.getLocation();
            if (cur == null || !cur.isWorldLoaded()) return;

            try {
                String key = cur.getBlock().getBiome().name();
                String name = plugin.getBiomeName(key);
                biomeCache.put(player.getUniqueId(), name);
            } catch (Exception ignored) {}
        });
    }
}
