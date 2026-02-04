package com.fluxcraft.fXChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    private final FXChat plugin;

    public ReloadCommand(FXChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        if (!sender.hasPermission("fxchat.reload")) {
            sender.sendMessage("§c权限不足");
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.handleAutoReload(false);
            sender.sendMessage("§a✅ 配置文件已重载");
        } else if ("info".equalsIgnoreCase(args[0])) {
            sender.sendMessage(plugin.getPluginMeta().getVersion());
        } else {
            sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6/fxchat reload");
        sender.sendMessage("§6/fxchat info");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? Arrays.asList("reload", "info") : null;
    }
}
