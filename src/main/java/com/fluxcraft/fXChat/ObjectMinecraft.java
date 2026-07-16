package com.fluxcraft.fXChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class ObjectMinecraft {

    private static final Logger LOGGER = Logger.getLogger("FXChat");
    private static final MinecraftVersion SUPPORTED_VERSION = new MinecraftVersion(1, 21, 9);
    private static Boolean isSupportedVersion = null;
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    public static boolean isObjectTextSupported() {
        if (isSupportedVersion != null) {
            return isSupportedVersion;
        }

        try {
            // Paper 26.2+: 使用 Minecraft 版本号替代 Bukkit 版本号，适配新版本号方案
            String versionString = Bukkit.getMinecraftVersion();
            var matcher = VERSION_PATTERN.matcher(versionString);

            if (matcher.find()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

                MinecraftVersion current = new MinecraftVersion(major, minor, patch);
                isSupportedVersion = current.compareTo(SUPPORTED_VERSION) >= 0;

                if (isSupportedVersion) {
                    LOGGER.fine("[FXChat] 检测到版本 " + major + "." + minor + "." + patch + " - 头像功能已启用");
                }

                return isSupportedVersion;
            }
        } catch (Exception e) {
            LOGGER.warning("[FXChat] 解析服务器版本失败: " + e.getMessage());
        }

        isSupportedVersion = false;
        return false;
    }

    public static String getHeadPlaceholder() {
        return "\uE000";
    }

    public static Component parseMessageWithHeadAndColors(String message, Player player, boolean useHead, LegacyComponentSerializer colorSerializer) {
        if (!useHead || !isObjectTextSupported()) {
            return colorSerializer.deserialize(message);
        }

        String placeholder = getHeadPlaceholder();

        if (!message.contains(placeholder)) {
            return colorSerializer.deserialize(message);
        }

        String[] parts = message.split(Pattern.quote(placeholder), -1);
        Component result = Component.empty();

        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result = result.append(colorSerializer.deserialize(parts[i]));
            }

            if (i < parts.length - 1) {
                result = result.append(createPlayerHeadComponent(player));
            }
        }

        return result;
    }

    public static Component createPlayerHeadComponent(Player player) {
        if (player == null) return Component.text("[?]");

        Component hoverText = Component.text()
                .append(Component.text("玩家: ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("点击复制玩家名", NamedTextColor.YELLOW))
                .build();

        return Component.text()
                .append(createHeadContent())
                .hoverEvent(hoverText)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(player.getName()))
                .build();
    }

    private static Component createHeadContent() {
        return Component.text()
                .append(Component.text("\uD83D\uDDE8️", TextColor.color(0xF5E050)))
                .build();
    }

    private record MinecraftVersion(int major, int minor, int patch) implements Comparable<MinecraftVersion> {
        @Override
        public int compareTo(MinecraftVersion other) {
            if (this.major != other.major) return this.major - other.major;
            if (this.minor != other.minor) return this.minor - other.minor;
            return this.patch - other.patch;
        }
    }
}
