package com.fluxcraft.fXChat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration biomeConfig;

    private String chatFormat;
    private boolean useBstats;
    private boolean useBukkitApi;
    private boolean usePlayerHead;
    private String headPlaceholder;
    private boolean autoReload;
    private int autoReloadInterval; // 秒
    private int biomeUpdateInterval; // 秒

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.chatFormat = ChatColor.translateAlternateColorCodes('&', config.getString("chat-format", "<%player_name%> %message%"));
        this.useBstats = config.getBoolean("use-bstats", true);
        this.useBukkitApi = config.getBoolean("use-bukkit-api", true);
        this.usePlayerHead = config.getBoolean("use-player-head", false);
        this.headPlaceholder = config.getString("head-placeholder", "%player_head%");
        this.autoReload = config.getBoolean("auto-reload-enabled", false);
        this.autoReloadInterval = config.getInt("auto-reload-interval", 3);
        this.biomeUpdateInterval = config.getInt("biome-update-interval", 2);

        plugin.saveResource("biome.yml", false);
        try {
            this.biomeConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(plugin.getDataFolder(), "biome.yml"));
        } catch (Exception e) {
            plugin.getLogger().warning("无法加载 biome.yml");
        }
    }
    public String getChatFormat() { return chatFormat; }
    public boolean isUseBstats() { return useBstats; }
    public boolean isUseBukkitApi() { return useBukkitApi; }
    public boolean isUsePlayerHead() { return usePlayerHead; }
    public boolean isAutoReload() { return autoReload; }
    public int getAutoReloadInterval() { return autoReloadInterval; }
    public int getBiomeUpdateInterval() { return biomeUpdateInterval; }
    public String getHeadPlaceholder() { return headPlaceholder; }
    public FileConfiguration getBiomeConfig() { return biomeConfig; }
}
