package com.fluxcraft.fXChat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BiomePlaceholder extends PlaceholderExpansion {
    private final FXChat plugin;

    public BiomePlaceholder(FXChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "flux";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FluxCraft";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "§c玩家离线";
        }

        // 处理 %flux_qx% 占位符
        if (params.equalsIgnoreCase("qx")) {
            return getPlayerBiome(player);
        }

        return null;
    }

    /**
     * 获取玩家当前所在的生物群系名称
     * 兼容 Folia 和 Paper 核心
     */
    private String getPlayerBiome(Player player) {
        // 先检查玩家是否在线
        if (!player.isOnline()) {
            return "§c玩家离线";
        }

        if (plugin.isFolia()) {
            // Folia 核心专用处理
            return getPlayerBiomeFolia(player);
        } else {
            // Paper 核心专用处理
            return getPlayerBiomePaper(player);
        }
    }

    /**
     * Folia 核心的生物群系获取方法
     */
    private String getPlayerBiomeFolia(Player player) {
        // 保存玩家UUID和位置快照，避免竞态条件
        final String playerName = player.getName();
        final java.util.UUID playerUuid = player.getUniqueId();

        try {
            CompletableFuture<String> future = new CompletableFuture<>();

            // 在 Folia 中使用区域调度器
            Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
                try {
                    // 再次检查玩家是否在线
                    Player currentPlayer = Bukkit.getPlayer(playerUuid);
                    if (currentPlayer == null || !currentPlayer.isOnline()) {
                        future.complete("§c玩家离线");
                        return;
                    }

                    Location location = currentPlayer.getLocation();
                    if (location == null || !location.isWorldLoaded()) {
                        future.complete("§c世界未加载");
                        return;
                    }

                    Biome biome = location.getBlock().getBiome();
                    if (biome == null) {
                        future.complete("§c无法获取生物群系");
                        return;
                    }

                    String biomeKey = biome.name();
                    String biomeName = plugin.getBiomeName(biomeKey);
                    future.complete(biomeName);

                } catch (Exception e) {
                    plugin.getLogger().warning("🔧 Folia: 获取生物群系时出现技术问题: " + e.getMessage());
                    future.complete("§c系统繁忙");
                }
            });

            // 等待结果，设置超时时间
            return future.get(2, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            plugin.getLogger().warning("⏰ Folia: 获取生物群系超时");
            return "§c请求超时";
        } catch (Exception e) {
            plugin.getLogger().warning("⚡ Folia: 处理生物群系请求时出现异常: " + e.getMessage());
            return "§c系统错误";
        }
    }

    /**
     * Paper 核心的生物群系获取方法
     */
    private String getPlayerBiomePaper(Player player) {
        // 保存玩家UUID，避免竞态条件
        final String playerName = player.getName();
        final java.util.UUID playerUuid = player.getUniqueId();

        try {
            // 在 Paper 中使用同步任务
            String biomeName = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                try {
                    // 再次检查玩家是否在线
                    Player currentPlayer = Bukkit.getPlayer(playerUuid);
                    if (currentPlayer == null || !currentPlayer.isOnline()) {
                        return "§c玩家离线";
                    }

                    Location location = currentPlayer.getLocation();
                    if (location == null || !location.isWorldLoaded()) {
                        return "§c世界未加载";
                    }

                    Biome biome = location.getBlock().getBiome();
                    if (biome == null) {
                        return "§c无法获取生物群系";
                    }

                    String biomeKey = biome.name();
                    return plugin.getBiomeName(biomeKey);

                } catch (Exception e) {
                    plugin.getLogger().warning("🔧 Paper: 获取生物群系时出现技术问题: " + e.getMessage());
                    return "§c系统繁忙";
                }
            }).get(2, TimeUnit.SECONDS);

            return biomeName;

        } catch (TimeoutException e) {
            plugin.getLogger().warning("⏰ Paper: 获取生物群系超时");
            return "§c请求超时";
        } catch (Exception e) {
            plugin.getLogger().warning("⚡ Paper: 处理生物群系请求时出现异常: " + e.getMessage());
            return "§c系统错误";
        }
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean register() {
        if (super.register()) {
            plugin.getLogger().info("✅ 成功注册 PlaceholderAPI 扩展: " + getIdentifier());
            return true;
        }
        plugin.getLogger().warning("❌ 注册 PlaceholderAPI 扩展失败: " + getIdentifier());
        return false;
    }
}
