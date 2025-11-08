package com.fluxcraft.fXChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FXChat extends JavaPlugin implements Listener {
    private String chatFormat;
    private final Map<String, String> biomes = new ConcurrentHashMap<>();
    private boolean placeholderAPIEnabled;
    private boolean isFolia = false;

    @Override
    public void onEnable() {
        isFolia = Bukkit.getServer().getClass().getName().contains("folia");
        saveDefaultConfig();
        loadConfig();
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            new BiomePlaceholder(this).register();
        }
        getCommand("fluxchat").setExecutor(new ReloadCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        chatFormat = ChatColor.translateAlternateColorCodes('&', config.getString("chat-format", "[%player_name%] %message%"));
        biomes.clear();
        config.getConfigurationSection("biomes").getKeys(false).forEach(biome -> {
            biomes.put(biome, ChatColor.translateAlternateColorCodes('&', config.getString("biomes." + biome)));
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(this, task -> {
                String formatted = formatMessage(player, message);
                event.setFormat(formatted);
            });
        } else {
            String formatted = formatMessage(player, message);
            event.setFormat(formatted);
        }
    }

    private String formatMessage(Player player, String message) {
        String result = chatFormat.replace("%player_name%", player.getName()).replace("%message%", message);
        if (placeholderAPIEnabled) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    public String getBiomeName(String biomeKey) {
        return biomes.getOrDefault(biomeKey.toUpperCase(), biomeKey);
    }
}