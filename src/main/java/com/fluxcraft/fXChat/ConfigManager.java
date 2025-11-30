package com.fluxcraft.fXChat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final Logger logger;

    private FileConfiguration mainConfig;
    private FileConfiguration biomeConfig;
    private File biomeFile;

    private String chatFormat;
    private boolean useBstats;
    private boolean useBukkitApi;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadConfigs() {
        setupMainConfig();
        setupBiomeConfig();
        loadValues();
    }

    private void setupMainConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        updateConfigWithComments(configFile, "config.yml");

        mainConfig = plugin.getConfig();
    }

    private void setupBiomeConfig() {
        biomeFile = new File(plugin.getDataFolder(), "biome.yml");
        if (!biomeFile.exists()) {
            plugin.saveResource("biome.yml", false);
        }
        biomeConfig = YamlConfiguration.loadConfiguration(biomeFile);
    }

    private void updateConfigWithComments(File configFile, String resourceFileName) {
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
        FileConfiguration defaultConfig;
        List<String> defaultLines;
        List<String> currentLines;

        try (BufferedReader defaultReader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceFileName), StandardCharsets.UTF_8))) {
            defaultLines = new ArrayList<>();
            String line;
            while ((line = defaultReader.readLine()) != null) {
                defaultLines.add(line);
            }
            defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceFileName), StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.severe("读取默认配置文件时发生错误: " + e.getMessage());
            return;
        }

        try (BufferedReader currentReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
            currentLines = new ArrayList<>();
            String line;
            while ((line = currentReader.readLine()) != null) {
                currentLines.add(line);
            }
        } catch (IOException e) {
            logger.severe("读取当前配置文件时发生错误: " + e.getMessage());
            return;
        }

        List<String> newLines = new ArrayList<>();
        boolean needsSave = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (!currentConfig.contains(key)) {
                needsSave = true;
                String value = defaultConfig.get(key).toString();
                String indent = "  ".repeat(key.split("\\.").length - 1);
                String newLine = indent + key.substring(key.lastIndexOf('.') + 1) + ": " + value;
                newLines.add(newLine);
                logger.info("正在为配置文件添加新选项: " + key);
            }
        }

        if (needsSave) {
            currentLines.addAll(newLines);
            try {
                String fileContent = String.join("\n", currentLines);
                configFile.createNewFile();
                java.nio.file.Files.write(configFile.toPath(), fileContent.getBytes(StandardCharsets.UTF_8));
                logger.info("配置文件 " + resourceFileName + " 已成功更新。");
            } catch (IOException e) {
                logger.severe("保存更新后的配置文件 " + resourceFileName + " 时出错: " + e.getMessage());
            }
        }
    }

    private void loadValues() {
        chatFormat = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("chat-format", "[%player_name%] %message%"));
        useBstats = mainConfig.getBoolean("use-bstats", true);
        useBukkitApi = mainConfig.getBoolean("use-bukkit-api", true);
        logger.info("⚙️ 配置加载: bStats=" + useBstats + ", BukkitAPI=" + useBukkitApi);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        setupMainConfig();
        setupBiomeConfig();
        loadValues();
        logger.info("📊 已加载 " + biomeConfig.getConfigurationSection("biomes").getKeys(false).size() + " 个生物群系配置");
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public boolean isUseBstats() {
        return useBstats;
    }

    public boolean isUseBukkitApi() {
        return useBukkitApi;
    }

    public FileConfiguration getBiomeConfig() {
        return biomeConfig;
    }
}
