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

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "info":
                handleInfo(sender);
                break;

            default:
                sender.sendMessage("§c⚠️ 未知命令！使用 §e/" + label + " §c查看可用命令");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6=== fXChat 命令帮助 ===");
        sender.sendMessage("§e/" + label + " reload §7- 重新加载配置文件");
        sender.sendMessage("§e/" + label + " info §7- 显示插件信息");
        sender.sendMessage("§6=====================");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("fxchat.reload")) {
            sender.sendMessage("§c⛔ 您没有执行此命令的权限");
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.loadBiomes();

        sender.sendMessage("§a✅ 配置文件已成功重载！");
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§b📋 " + plugin.getPluginInfo());
        sender.sendMessage("§7PlaceholderAPI 支持: " +
                (plugin.isPlaceholderAPIEnabled() ? "§a已启用" : "§c未启用"));
        sender.sendMessage("§7bStats 统计: " + (plugin.getConfigManager().isUseBstats() ? "§a已启用" : "§c已禁用"));
        sender.sendMessage("§7使用 Bukkit 广播API: " + (plugin.getConfigManager().isUseBukkitApi() ? "§a是" : "§c否"));

        int biomeCount = 0;
        if (plugin.getConfigManager().getBiomeConfig().getConfigurationSection("biomes") != null) {
            biomeCount = plugin.getConfigManager().getBiomeConfig().getConfigurationSection("biomes").getKeys(false).size();
        }
        sender.sendMessage("§7生物群系配置数量: §e" + biomeCount);

        sender.sendMessage("§7服务器核心类型: §e" + (plugin.isFolia() ? "Folia" : "Paper"));
        sender.sendMessage("§7在线玩家数: §e" + org.bukkit.Bukkit.getOnlinePlayers().size());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "info");
        }
        return null;
    }
}
