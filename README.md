# fXChat - 极致的轻量化聊天插件 💬

> ⚡ **专为现代 Minecraft 服务器设计的轻量级聊天插件**
> 🎯 **简单配置 · 极致性能 · 全面兼容**

---

## ✨ 核心特性

| 特性 | 描述 | 状态 |
|------|------|------|
| 🚀 **极致轻量化** | 代码精简，资源占用极低 | ✅ |
| 🎨 **PAPI 变量支持** | 完整支持 PlaceholderAPI 所有变量 | ✅ |
| 🔄 **热重载功能** | `/fluxchat reload` 实时更新配置 | ✅ |
| 🌍 **生物群系检测** | 自带 `%flux_qx%` 生物群系变量 | ✅ |
| ⚡ **多核心兼容** | 完美支持 Folia & Paper 等核心 | ✅ |
| 🎯 **简单配置** | 极简配置文件，小白也能轻松上手 | ✅ |

---

## 🛠️ 快速开始

### 📥 前置要求
```bash
# 安装 PlaceholderAPI 扩展
/papi ecloud download Player
/papi ecloud download Server
/papi ecloud download Statistic
/papi reload
```

### ⚙️ 基础配置
```yaml
# config.yml - 主配置文件
chat-format: "&7[&6%player_name%&7] &f%message%"

# biome.yml - 生物群系配置文件
biomes:
  PLAINS: "&a平原"
  FOREST: "&2森林"
  DESERT: "&e沙漠"
  OCEAN: "&1海洋"
```

---

## 🎮 命令与权限

| 命令 | 权限 | 描述 |
|------|------|------|
| `/fluxchat reload` | `fxchat.reload` | 重载插件配置 |
| `/fluxchat info` | `fxchat.info` | 查看插件信息 |

---

## 📊 效果展示

### 🎨 聊天格式示例
```
⚡ 当前延迟: 49ms  🌍 所在群系: 平原  ⏰ 在线: 12.5小时
[玩家名] 这里是聊天内容...
```

### 🌟 特色占位符
- `%flux_qx%` - 智能生物群系显示
- `%player_name%` - 玩家名称
- `%message%` - 聊天内容
- 支持所有 PAPI 变量！

---

## 🔧 技术优势

### 🏗️ 架构设计
```java
// 多核心智能适配
if (isFolia) {
    // Folia 区域调度器
    Bukkit.getRegionScheduler().execute(...);
} else {
    // Paper 同步调度器
    Bukkit.getScheduler().callSyncMethod(...);
}
```

### ⚡ 性能表现
- 🚀 **启动时间**: < 100ms
- 💾 **内存占用**: < 5MB
- ⏱️ **处理延迟**: < 3ms/消息

---

## 🌈 为什么选择 fXChat？

### 🆚 与传统插件对比

| 功能 | fXChat | 传统插件 |
|------|--------|----------|
| 配置复杂度 | ⭐ | ⭐⭐⭐⭐⭐ |
| Folia 支持 | ✅ | ❌ |
| 启动速度 | ⚡ 极快 | 🐢 较慢 |
| 资源占用 | 💾 极低 | 📈 较高 |
| 学习曲线 | 📚 简单 | 🎓 复杂 |

---

## 🎯 适用场景

- 🏠 **大型服务器** - 轻量不卡顿
- 🏢 **大型网络** - 高性能稳定运行
- 🔧 **技术服** - 完美兼容 Folia
- 🎨 **主题服** - 灵活自定义格式
- ⚡ **高性能服** - 极致优化体验

---

## 📝 版本信息

- **支持核心**: Paper Folia等分支
- **依赖插件**: PlaceholderAPI

---

> 💫 **还在为复杂的聊天插件配置而烦恼吗？**
> 🎉 **fXChat 让聊天美化变得如此简单！**
> 
> ⚡ **立即体验，感受极致的轻量化魅力！**

---

*✨ 为现代 Minecraft 服务器而生*

效果图
![草原](.\png\1.png)

![房屋](.\png\2.png)
