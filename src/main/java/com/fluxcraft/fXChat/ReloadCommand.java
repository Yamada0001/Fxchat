package com.fluxcraft.fXChat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final FXChat plugin;

    public ReloadCommand(FXChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("fxchat.reload")) {
            sender.sendMessage(ChatColor.RED + "权限不足!");
            return true;
        }

        plugin.loadConfig();
        sender.sendMessage(ChatColor.GREEN + "配置已重载!");
        return true;
    }
}