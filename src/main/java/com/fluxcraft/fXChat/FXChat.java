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

public final class FXChat extends JavaPlugin implements Listener {
    private String chatFormat;
    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;
    private File biomeFile;
    private FileConfiguration biomeConfig;
    private boolean isFolia = false;

    @Override
    public void onEnable() {
        // 检测服务器核心类型
        isFolia = detectFolia();

        // 初始化配置文件
        saveDefaultConfig();
        setupBiomeConfig();
        loadConfig();

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

    /**
     * 检测是否为 Folia 核心
     */
    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 设置生物群系配置文件
     */
    private void setupBiomeConfig() {
        biomeFile = new File(getDataFolder(), "biome.yml");
        if (!biomeFile.exists()) {
            saveResource("biome.yml", false);
        }
        biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);
    }

    /**
     * 加载所有配置
     */
    public void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();

        // 重新加载生物群系配置
        biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);

        // 加载聊天格式
        String rawFormat = config.getString("chat-format", "[%player_name%] %message%");
        chatFormat = ChatColor.translateAlternateColorCodes('&', rawFormat);

        biomes.clear();

        // 从 biome.yml 加载生物群系配置
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

    /**
     * 保存生物群系配置
     */
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

        // 取消原版消息
        event.setCancelled(true);

        // 格式化消息
        String formatted = formatMessage(player, message);

        // 发送消息
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(formatted);
        }

        // 控制台也显示
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
            // 美化原版生物群系名称
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
