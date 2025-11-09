package com.fluxcraft.fXChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public final class FXChat extends JavaPlugin implements Listener {
    private String chatFormat;
    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;
    private File biomeFile;
    private FileConfiguration biomeConfig;
    private boolean isFolia = false;

    private Metrics metrics;

    @Override
    public void onEnable() {
        isFolia = detectFolia();

        saveDefaultConfig();
        setupBiomeConfig();
        loadConfig();

        initializeMetrics();

        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            new BiomePlaceholder(this).register();
            getLogger().info("✨ 已成功注册 PlaceholderAPI 扩展");
        }

        getCommand("fluxchat").setExecutor(new ReloadCommand(this));
        getCommand("fluxchat").setTabCompleter(new ReloadCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("🎉 fXChat 插件已成功启用 - 核心类型: " + (isFolia ? "Folia" : "Paper"));
    }

    @Override
    public void onDisable() {
        getLogger().info("👋 fXChat 插件已禁用");
    }

    private void initializeMetrics() {
        try {
            int pluginId = 27914;

            metrics = new Metrics(this, pluginId);

            addCustomCharts();

            getLogger().info("📊 bStats 统计功能已启用");
        } catch (Exception e) {
            getLogger().warning("📊 bStats 统计初始化失败: " + e.getMessage());
        }
    }

    private void addCustomCharts() {
        if (metrics == null) return;

        metrics.addCustomChart(new SimplePie("server_core", () ->
                isFolia ? "Folia" : "Paper"
        ));

        metrics.addCustomChart(new SimplePie("placeholderapi_enabled", () ->
                placeholderAPIEnabled ? "已启用" : "未启用"
        ));

        metrics.addCustomChart(new SingleLineChart("biome_config_count", () ->
                biomes.size()
        ));

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

    private void setupBiomeConfig() {
        biomeFile = new File(getDataFolder(), "biome.yml");
        if (!biomeFile.exists()) {
            saveResource("biome.yml", false);
        }
        biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);
    }

    public void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();

        biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);

        String rawFormat = config.getString("chat-format", "[%player_name%] %message%");
        chatFormat = ChatColor.translateAlternateColorCodes('&', rawFormat);

        biomes.clear();

        if (biomeConfig.getConfigurationSection("biomes") != null) {
            for (String biome : biomeConfig.getConfigurationSection("biomes").getKeys(false)) {
                String biomeName = biomeConfig.getString("biomes." + biome);
                if (biomeName != null) {
                    biomes.put(biome.toUpperCase(), ChatColor.translateAlternateColorCodes('&', biomeName));
                }
            }
        }

        getLogger().info("📊 已加载 " + biomes.size() + " 个生物群系配置");
    }

    public void saveBiomeConfig() {
        try {
            biomeConfig.save(biomeFile);
        } catch (IOException e) {
            getLogger().severe("❌ 保存生物群系配置文件时出错: " + e.getMessage());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        event.setCancelled(true);

        String formatted = formatMessage(player, message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(formatted);
        }

        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    private String formatMessage(Player player, String message) {
        String result = chatFormat
                .replace("%player_name%", player.getDisplayName())
                .replace("%message%", message);

        if (placeholderAPIEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }

        result = ChatColor.translateAlternateColorCodes('&', result);
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

    public FileConfiguration getBiomeConfig() {
        return biomeConfig;
    }
}
