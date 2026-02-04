package com.fluxcraft.fXChat;

import com.fluxcraft.fXChat.feature.BiomeManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BiomePlaceholder extends PlaceholderExpansion {
    private final FXChat plugin;
    private final BiomeManager biomeManager;

    public BiomePlaceholder(FXChat plugin, BiomeManager biomeManager) {
        this.plugin = plugin;
        this.biomeManager = biomeManager;
    }

    @Override
    public @NotNull String getIdentifier() { return "flux"; }

    @Override
    public @NotNull String getAuthor() { return "FluxCraft"; }

    @Override
    public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        if ("qx".equalsIgnoreCase(params)) {
            return biomeManager.getCachedBiome(player);
        }
        return null;
    }
}
