# FxChat

高级 Minecraft 聊天格式插件，基于 Paper 26.2 API，支持 PlaceholderAPI 变量、生物群系占位符、玩家头像渲染，兼容 Paper 与 Folia 核心。

## 功能特性

- **聊天格式化**：自定义聊天消息格式，支持 `&` 颜色代码和十六进制颜色 (`&#RRGGBB`)
- **PlaceholderAPI 集成**：支持所有 PAPI 变量（`%player_name%`、`%player_ping%`、`%statistic_time_played%` 等）
- **生物群系占位符**：实时显示玩家当前所在生物群系的中文名称
- **玩家头像渲染**：在聊天消息中显示玩家头像（需服务端资源包支持 `\uE000` 字符）
- **Folia 兼容**：通过调度器抽象层自动适配 Paper / Folia 核心
- **配置热更新**：自动侦测配置文件变更，无需重启服务器
- **bStats 统计**：匿名收集服务器核心类型数据（可关闭）

## 环境要求

| 项目 | 要求 |
|------|------|
| 服务端 | Paper 26.2+（或 Folia 对应版本） |
| Java | 21+ |
| 前置插件 | PlaceholderAPI（可选，推荐安装） |

## 安装说明

1. 从 [Releases](../../releases) 下载最新 `FxChat-3.jar`
2. 将 JAR 文件放入服务端的 `plugins/` 目录
3. 启动服务器，插件会自动生成默认配置文件
4. 根据需要编辑 `plugins/FxChat/config.yml` 和 `plugins/FxChat/biome.yml`

## 快速开始

### 基础配置

编辑 `config.yml` 设置聊天格式：

```yaml
# 聊天消息格式
# 可用变量：
#   %player_name%  - 玩家显示名
#   %message%      - 发送的消息
#   %flux_qx%      - 生物群系名称（来自 biome.yml）
#   %player_head%  - 玩家头像占位符（需开启 use-player-head）
#   以及所有 PlaceholderAPI 支持的变量
chat-format: '[&e%player_ping%ms %flux_qx%&f &d%statistic_time_played:hours%h&f]%player_name%&6>>&f%message%'

# 是否启用玩家头像功能（需 1.21.9+ 及资源包支持）
use-player-head: false

# 头像占位符标识
head-placeholder: "%player_head%"
```

### 推荐的 PlaceholderAPI 扩展

```
/papi ecloud download Player
/papi ecloud download Server
/papi ecloud download Statistic
/papi reload
```

### 生物群系配置

`biome.yml` 存储生物群系键名到中文名称的映射。插件已内置原版全部生物群系（含 Paper 26.2 新增的 `SULFUR_CAVES` 硫磺洞穴）。

自定义生物群系示例（支持数据包/模组添加的非原版群系）：

```yaml
biomes:
  PLAINS: "&a平原"
  SULFUR_CAVES: "&6硫磺洞穴"
  # 数据包自定义群系（使用 命名空间:路径 格式）
  MYMOD:CUSTOM_BIOME: "&b自定义群系"
```

## 命令与权限

| 命令 | 别名 | 权限 | 说明 |
|------|------|------|------|
| `/fluxchat reload` | `/fxchat reload`、`/fxc reload` | `fxchat.reload` | 重载配置文件（默认：OP） |
| `/fluxchat info` | `/fxchat info`、`/fxc info` | `fxchat.info` | 查看插件版本信息（默认：所有人） |

## 配置项参考

### config.yml

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `use-bstats` | `true` | 是否启用 bStats 匿名统计 |
| `use-player-head` | `false` | 是否启用玩家头像渲染 |
| `head-placeholder` | `%player_head%` | 头像占位符标识 |
| `chat-format` | 见上文 | 聊天消息格式模板 |
| `auto-reload-enabled` | `true` | 是否启用配置文件热更新 |
| `auto-reload-interval` | `3` | 热更新检测间隔（秒） |
| `biome-update-interval` | `2` | 生物群系缓存刷新间隔（秒） |

## 项目结构

```
src/main/java/com/fluxcraft/fXChat/
├── FXChat.java              # 主插件类，事件监听与消息格式化
├── ConfigManager.java       # 配置文件加载与管理
├── ObjectMinecraft.java     # 玩家头像渲染与版本检测
├── BiomePlaceholder.java    # PlaceholderAPI 占位符扩展
├── ReloadCommand.java       # 命令处理器
├── feature/
│   └── BiomeManager.java    # 生物群系缓存与自动更新
├── scheduler/
│   ├── SchedulerAdapter.java  # 调度器抽象接口
│   ├── PaperScheduler.java    # Paper 核心调度实现
│   └── FoliaScheduler.java    # Folia 核心调度实现
└── util/
    └── FileWatcher.java       # 配置文件变更侦测工具
```

## 构建说明

```bash
mvn clean package
```

构建产物位于 `target/FxChat-3.jar`。

### 技术栈

- **Paper API**: `26.2.build.60-beta`
- **Java**: 21
- **Adventure API**: 用于文本组件处理（Component）
- **Maven Shade Plugin**: 依赖打包与重定位

## Folia 兼容说明

插件通过 `SchedulerAdapter` 抽象层自动检测并适配服务端核心：

- **Paper 核心**：使用 `Bukkit.getScheduler()` 进行任务调度
- **Folia 核心**：使用 `RegionizedServer` 的区域/全局调度器

> 注意：在 Folia 环境下，生物群系自动轮询功能会被禁用（防止跨区域访问导致崩溃），占位符可能显示为「计算中...」。

## 许可证

详见项目根目录 LICENSE 文件。
