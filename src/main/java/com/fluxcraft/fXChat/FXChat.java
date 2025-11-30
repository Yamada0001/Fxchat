package com.fluxcraft.fXChat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public final class FXChat extends JavaPlugin implements Listener {
    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;
    private boolean isFolia = false;
    private Metrics metrics;
    private ExecutorService messageExecutor;
    private ScheduledExecutorService autoReloadExecutor;
    private ConfigManager configManager;
    private long lastConfigModified;
    private long lastBiomeModified;

    @Override
    public void onEnable() {
        isFolia = detectFolia();
        messageExecutor = Executors.newFixedThreadPool(2);
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        loadBiomes();
        startAutoReloadTimer();

        if (configManager.isUseBstats()) {
            initializeMetrics();
        }

        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            new BiomePlaceholder(this).register();
        }

        getCommand("fluxchat").setExecutor(new ReloadCommand(this));
        getCommand("fluxchat").setTabCompleter(new ReloadCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("🎉 fXChat 插件已成功启用 - 核心类型: " + (isFolia ? "Folia" : "Paper"));
    }

    @Override
    public void onDisable() {
        if (messageExecutor != null) {
            messageExecutor.shutdown();
        }
        if (autoReloadExecutor != null) {
            autoReloadExecutor.shutdown();
        }
        getLogger().info("👋 fXChat 插件已禁用");
    }

    private void initializeMetrics() {
        try {
            int pluginId = 27914;
            metrics = new Metrics(this, pluginId);
            addCustomCharts();
        } catch (Exception e) {
            getLogger().severe("📊 bStats 统计初始化失败: " + e.getMessage());
        }
    }

    private void addCustomCharts() {
        if (metrics == null) return;
        metrics.addCustomChart(new SimplePie("server_core", () -> isFolia ? "Folia" : "Paper"));
        metrics.addCustomChart(new SimplePie("placeholderapi_enabled", () -> placeholderAPIEnabled ? "已启用" : "未启用"));
        metrics.addCustomChart(new SingleLineChart("biome_config_count", () -> biomes.size()));
        metrics.addCustomChart(new SimplePie("server_version", () -> {
            String version = Bukkit.getVersion();
            if (version.contains("1.21")) return "1.21.x";
            if (version.contains("1.20")) return "1.20.x";
            return "其他版本";
        }));
        metrics.addCustomChart(new SimplePie("online_players_range", () -> {
            int online = Bukkit.getOnlinePlayers().size();
            if (online == 0) return "0";
            if (online <= 10) return "1-10";
            if (online <= 50) return "11-50";
            if (online <= 100) return "51-100";
            return "100+";
        }));
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void startAutoReloadTimer() {
        File configFile = new File(getDataFolder(), "config.yml");
        File biomeFile = new File(getDataFolder(), "biome.yml");
        lastConfigModified = configFile.lastModified();
        lastBiomeModified = biomeFile.lastModified();

        autoReloadExecutor = Executors.newSingleThreadScheduledExecutor();
        autoReloadExecutor.scheduleAtFixedRate(() -> {
            long currentConfigModified = configFile.lastModified();
            long currentBiomeModified = biomeFile.lastModified();
            boolean needsReload = false;

            if (currentConfigModified > lastConfigModified) {
                lastConfigModified = currentConfigModified;
                needsReload = true;
            }
            if (currentBiomeModified > lastBiomeModified) {
                lastBiomeModified = currentBiomeModified;
                needsReload = true;
            }

            if (needsReload) {
                if (isFolia) {
                    getServer().getGlobalRegionScheduler().run(this, scheduledTask -> {
                        configManager.reloadConfigs();
                        loadBiomes();
                    });
                } else {
                    getServer().getScheduler().runTask(this, () -> {
                        configManager.reloadConfigs();
                        loadBiomes();
                    });
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    public void loadBiomes() {
        biomes.clear();
        if (configManager.getBiomeConfig().getConfigurationSection("biomes") != null) {
            for (String biome : configManager.getBiomeConfig().getConfigurationSection("biomes").getKeys(false)) {
                String biomeName = configManager.getBiomeConfig().getString("biomes." + biome);
                if (biomeName != null) {
                    biomes.put(biome.toUpperCase(), org.bukkit.ChatColor.translateAlternateColorCodes('&', biomeName));
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (isFolia) {
            handleFoliaChat(event);
        } else {
            handlePaperChat(event);
        }
    }

    private void handlePaperChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = event.getMessage();

        messageExecutor.submit(() -> {
            String formatted = formatMessage(player, message);
            // Paper: 在主线程中广播，使用 Bukkit API
            getServer().getScheduler().runTask(this, () -> {
                Bukkit.broadcastMessage(formatted);
            });
        });
    }

    private void handleFoliaChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = event.getMessage();

        messageExecutor.submit(() -> {
            String formatted = formatMessage(player, message);
            // Folia: 直接在异步线程中广播给玩家
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(formatted);
            }
            // 控制台也需要消息
            Bukkit.getConsoleSender().sendMessage(formatted);
        });
    }

    private String formatMessage(Player player, String message) {
        String result = configManager.getChatFormat()
                .replace("%player_name%", player.getDisplayName())
                .replace("%message%", message);

        if (placeholderAPIEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }
        result = org.bukkit.ChatColor.translateAlternateColorCodes('&', result);
        return result;
    }

    public String getBiomeName(String biomeKey) {
        if (biomeKey == null) {
            return "§c未知区域";
        }
        String result = biomes.get(biomeKey.toUpperCase());
        if (result == null) {
            return "§7" + biomeKey.toLowerCase().replace("_", " ");
        }
        return result;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public boolean isFolia() {
        return isFolia;
    }

    public String getPluginInfo() {
        return "§bfXChat §fv" + getPluginMeta().getVersion() +
                " §7(核心: " + (isFolia ? "Folia" : "Paper") + ")";
    }
}
