package com.fluxcraft.fXChat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || !params.equalsIgnoreCase("qx")) return "";

        return plugin.getBiomeName(
                player.getLocation().getBlock().getBiome().name()
        );
    }

    @Override
    public boolean persist() {
        return true;
    }
}