package com.fluxcraft.fXChat;

import com.fluxcraft.fXChat.feature.BiomeManager;
import com.fluxcraft.fXChat.scheduler.SchedulerAdapter;
import com.fluxcraft.fXChat.util.FileWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FXChat extends JavaPlugin implements Listener {

    private SchedulerAdapter scheduler;
    private BiomeManager biomeManager;
    private ConfigManager configManager;
    private FileWatcher fileWatcher;

    private static final LegacyComponentSerializer HEX_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;
    private Metrics metrics;

    @Override
    public void onEnable() {
        this.scheduler = SchedulerAdapter.create(this);
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        loadBiomes();
        this.biomeManager = new BiomeManager(this, scheduler);
        this.biomeManager.startAutoUpdate();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("fluxchat").setExecutor(new ReloadCommand(this));

        if (configManager.isAutoReload()) {
            // ✅ 修复：使用 Lambda 表达式传入参数 true
            this.fileWatcher = new FileWatcher(() -> handleAutoReload(true));

            this.fileWatcher.watchFile(new File(getDataFolder(), "config.yml"));
            this.fileWatcher.watchFile(new File(getDataFolder(), "biome.yml"));
            this.fileWatcher.start(configManager.getAutoReloadInterval());
            getLogger().info("👁️ 文件变化自动侦测已启动 (间隔: " + configManager.getAutoReloadInterval() + "s)");
        }

        if (configManager.isUseBstats()) initializeMetrics();

        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            new BiomePlaceholder(this, biomeManager).register();
        }

        if (configManager.isUsePlayerHead() && ObjectMinecraft.isObjectTextSupported()) {
            getLogger().info("🗨️ 玩家头像功能已启用");
        }

        getLogger().info("🎉 fXChat 启用成功 - 调度器: " + scheduler.getClass().getSimpleName());
    }

    @Override
    public void onDisable() {
        if (fileWatcher != null) fileWatcher.stop();
        if (biomeManager != null) biomeManager.close();
        scheduler.close();
        getLogger().info("👋 fXChat 已禁用");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        Component formatted = formatMessageToComponent(player, event.getMessage());
        scheduler.runGlobal(() -> Bukkit.getServer().broadcast(formatted));
    }

    private Component formatMessageToComponent(Player player, String message) {
        boolean useHead = configManager.isUsePlayerHead() && ObjectMinecraft.isObjectTextSupported();
        String format = configManager.getChatFormat();
        String headPlaceholder = configManager.getHeadPlaceholder();

        String result = format
                .replace("%player_name%", player.getDisplayName())
                .replace("%message%", message);

        if (placeholderAPIEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }

        result = result.replace(headPlaceholder, useHead ? ObjectMinecraft.getHeadPlaceholder() : "");

        return ObjectMinecraft.parseMessageWithHeadAndColors(result, player, useHead, HEX_SERIALIZER);
    }

    public void handleAutoReload(boolean isAuto) {
        scheduler.runGlobal(() -> {
            if (isAuto) {
                getLogger().info("🔄 检测到配置文件变更，正在后台重载...");
            } else {
                getLogger().info("🔄 管理员手动触发了重载...");
            }

            configManager.loadConfigs();
            loadBiomes();
            biomeManager.clearCache();
        });
    }

    public void loadBiomes() {
        biomes.clear();
        var section = configManager.getBiomeConfig().getConfigurationSection("biomes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key);
                if (name != null) {
                    biomes.put(key.toUpperCase(), org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
                }
            }
        }
    }

    public String getBiomeName(String key) {
        if (key == null) return "§c未知";
        return biomes.getOrDefault(key.toUpperCase(), "§7" + key.toLowerCase().replace("_", " "));
    }

    public ConfigManager getConfigManager() { return configManager; }
    public boolean isPlaceholderAPIEnabled() { return placeholderAPIEnabled; }

    private void initializeMetrics() {
        try {
            metrics = new Metrics(this, 27914);
            metrics.addCustomChart(new SimplePie("server_core", () ->
                    scheduler instanceof com.fluxcraft.fXChat.scheduler.FoliaScheduler ? "Folia" : "Paper"
            ));
        } catch (Exception ignored) {}
    }
}
