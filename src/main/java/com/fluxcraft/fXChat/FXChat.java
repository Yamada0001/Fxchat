package com.fluxcraft.fXChat;

import com.fluxcraft.fXChat.feature.BiomeManager;
import com.fluxcraft.fXChat.scheduler.SchedulerAdapter;
import com.fluxcraft.fXChat.util.FileWatcher;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        this.scheduler = SchedulerAdapter.create(this);
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        loadBiomes();
        this.biomeManager = new BiomeManager(this, scheduler);
        this.biomeManager.startAutoUpdate();

        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand command = getCommand("fluxchat");
        if (command != null) {
            command.setExecutor(new ReloadCommand(this));
        } else {
            getLogger().warning("无法注册命令 fluxchat，请检查 plugin.yml 配置");
        }

        if (configManager.isAutoReload()) {
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
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        Component formatted = formatMessageToComponent(player, message);
        scheduler.runGlobal(() -> Bukkit.getServer().broadcast(formatted));
    }

    private Component formatMessageToComponent(Player player, String message) {
        boolean useHead = configManager.isUsePlayerHead() && ObjectMinecraft.isObjectTextSupported();
        String format = configManager.getChatFormat();
        String headPlaceholder = configManager.getHeadPlaceholder();

        String displayName = LEGACY_SECTION.serialize(player.displayName());

        String result = format
                .replace("%player_name%", displayName)
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
                    biomes.put(key.toUpperCase(), translateColorCodes(name));
                }
            }
        }
    }

    private static String translateColorCodes(String input) {
        return LegacyComponentSerializer.legacySection().serialize(
                LegacyComponentSerializer.legacy('&').deserialize(input)
        );
    }

    public String getBiomeName(NamespacedKey key) {
        if (key == null) return "§c未知";
        String pathKey = key.getKey().toUpperCase();
        // 先尝试仅路径名（兼容原版 biome.yml 配置）
        String name = biomes.get(pathKey);
        if (name != null) return name;
        // 再尝试完整 命名空间:路径（支持非原版/数据包生物群系）
        name = biomes.get(key.toString().toUpperCase());
        if (name != null) return name;
        // 回退：将下划线替换为空格的可读格式
        return "§7" + pathKey.toLowerCase().replace("_", " ");
    }

    public ConfigManager getConfigManager() { return configManager; }

    private void initializeMetrics() {
        try {
            Metrics metrics = new Metrics(this, 27914);
            metrics.addCustomChart(new SimplePie("server_core", () ->
                    scheduler instanceof com.fluxcraft.fXChat.scheduler.FoliaScheduler ? "Folia" : "Paper"
            ));
        } catch (Exception ignored) {}
    }
}
