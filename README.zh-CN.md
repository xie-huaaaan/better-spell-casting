# Better Spellcasting

Better Spellcasting 是一个客户端模组，用于改进 Spell Engine 的施法流程。它整合了 `spell-cycle` 中实用的法术选择与施法控制，并加入轮盘选择、更清晰的法术栏、可配置的施法按键，以及可选的左键弓弩操作。

本模组不会添加法术、修改法术数据，也不需要安装在服务器上。法术候选列表严格使用 Spell Engine 提供的顺序与可用性规则，内容模组仍然负责自己的法术及其依赖。

English：[README.md](README.md)

## 功能

- 按住 **切换选中法术**（默认 `X`）打开施法轮盘。
- 将鼠标指向法术后松开按键确认选择；轮盘中心设有死区，用于保持当前选择。
- 使用 **释放选中法术**（默认鼠标右键）施放当前法术。该按键可以在 Minecraft 的按键控制界面中，在 **更好的施法操作** 分类下修改。
- 可选启用快捷键施法。配置的数字键可以直接施放对应法术，同时不改变轮盘当前选中的法术。
- 法术栏支持单排和动态三排布局，并可将大小调整为 50% 至 150%。
- 打开轮盘时显示聚焦法术的名称，以及 Spell Engine 提供的描述和详细信息。
- 可选将弓和弩的使用改到鼠标左键。该功能仍调用原版的物品使用和停止使用流程，并在远程武器使用期间阻止攻击与破坏方块输入。
- 法术栏、轮盘、快捷键和施法按键共用同一套法术选择与施法路径。

本模组不会额外发明一套法术过滤规则。候选列表完全来自 Spell Engine，并保留其对持有物品、法术书、物品使用类型和法术可用性的判断。

## 安装

请根据 Minecraft 版本和加载器选择对应 JAR：

| Minecraft | 加载器 | 分支 | 文件名 |
| --- | --- | --- | --- |
| 1.20.1 | Fabric | `1.20.1-fabric` | `better-spell-casting-fabric-1.0.1+1.20.1.jar` |
| 1.20.1 | Forge | `1.20.1-forge` | `better-spell-casting-forge-1.0.1+1.20.1.jar` |
| 1.21.1 | Fabric | `1.21.1-fabric` | `better-spell-casting-fabric-1.0.1+1.21.1.jar` |
| 1.21.1 | NeoForge | `1.21.1-neoforge` | `better-spell-casting-neoforge-1.0.1+1.21.1.jar` |
| 26.1 | Fabric | `26.1-fabric` | `better-spell-casting-fabric-1.0.1+26.1.jar` |
| 26.1 | NeoForge | `26.1-neoforge` | `better-spell-casting-neoforge-1.0.1+26.1.2.jar` |

1. 将对应 JAR 放入实例的 `mods` 文件夹。
2. 安装对应 Minecraft 与加载器版本的 **Spell Engine**，以及加载器和内容模组要求的其他依赖。
3. 移除旧的 `spell-cycle` JAR。Better Spellcasting 已经包含其施法流程，同时加载两个模组可能造成重复按键处理、HUD 或 Mixin 冲突。
4. 启动客户端。服务器不需要安装 Better Spellcasting。

在 **选项 → 按键控制** 中可以修改 **切换选中法术** 和 **释放选中法术**。在 **选项 → 法术切换设置** 中可以修改快捷键施法、弓弩输入方式、法术栏布局和法术栏大小。

## 构建

每个支持的目标版本都使用独立分支维护。构建前请切换到对应分支。公开检出时需要提供与目标 Minecraft 和加载器完全匹配的 Spell Engine JAR 路径。

以 26.1 NeoForge 分支为例：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat clean build `
  -Pspell_engine_jar='D:\path\to\spell_engine-neoforge-1.10.5+26.1.2.jar'
```

构建产物位于 `build/libs/`，文件名与上方安装表一致。其他目标版本使用其加载器工具链要求的 Java 版本：1.20.1 Forge 使用 Java 17，1.21.1 NeoForge 使用 Java 21，26.1 使用 Java 25。

## 分支与项目范围

仓库分支对应安装表中的六个目标版本。与加载器相关的输入、界面、渲染和元数据代码保留在各自分支；在平台 API 允许的范围内，各版本保持相同的功能和配置行为。

本项目定位为客户端增强模组，不添加服务器协议、游戏内容或新的玩法数据，也不会在编译期依赖 Wizards、Paladins & Priests、Archers、Rogues & Warriors 等内容模组。

