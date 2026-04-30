
---

# Programmable Magic (可编程魔法)

<div align="center">

[![Gradle Package](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml)
[![mc_version](https://img.shields.io/badge/minecraft-1.21.11-brightgreen?logo=minecraft)](https://github.com/creepebucket/programmable_magic)
[![last_commit](https://img.shields.io/github/last-commit/creepebucket/programmable_magic?logo=github)](https://github.com/creepebucket/programmable_magic/commits)
[![release](https://img.shields.io/github/v/release/creepebucket/programmable_magic?include_prereleases&logo=github)](https://github.com/creepebucket/programmable_magic/releases)
[![bilibili](https://img.shields.io/badge/bilibili-演示视频-00A1D6?logo=bilibili&logoColor=white)](https://www.bilibili.com/video/BV1mHS8BWE9u)
[![repo](https://img.shields.io/badge/repo-programmable_magic-181717?logo=github&logoColor=white)](https://github.com/creepebucket/programmable_magic)

![GitHub Repo Card](https://githubcard.com/creepebucket/programmable_magic.svg?d=dU-_JTBK)

</div>

## 📖 简介 (Overview)

**Programmable Magic** 是一个基于 NeoForge 1.21.11 的技术向魔法模组。

在这里，魔法不再是简单的“按键释放”，而是一门**工程学**。你需要将一组「法术卡片」编排进魔杖，构建出可复用的**施法序列**。通过引入变量、逻辑判断与数学计算，你可以将魔法写成一段可执行的“程序”。

随着游戏的推进，配合插件系统与魔力网络，你将从手搓火球走向魔法自动化工业。

> [!WARNING]
> **开发状态提示**：目前模组仍处于**早期开发阶段 (Alpha/Beta)**，内容、数值平衡与合成配方可能会频繁调整。

## ✨ 核心特性 (Features)

### 1. 逻辑构建，而非数值堆砌
拒绝枯燥的等级碾压。强度的上限取决于你的**构建逻辑（Build）**。
- **模块化施法**：将“效果”、“修饰”、“控制”与“计算”卡片像代码一样组合。
- **高复用性**：一套优秀的法术序列可以针对不同场景（战斗、挖掘、移动）进行快速改装。

### 2. 深度自定义的法术系统
- **基础效果**：爆炸、加速、方块放置、药水云、数据打印等。
- **流程控制**：支持 `if/else` 条件判断、`while` 循环、逻辑运算（与/或/非）及比较运算。
- **数学表达式**：提供数字常量、四则运算、幂运算及括号支持。拒绝硬编码，让参数随心所欲。
- **动态参数**：实时获取施法者位置、视线向量、目标实体数据，实现“自瞄”或“追踪”逻辑。

### 3. 多维度的魔力体系
引入四系魔力作为施法成本，为后续的“魔力工业”奠定基础：
- ☢️ **辐射 (Radiation)**
- 🔥 **温度 (Temperature)**
- 💨 **动量 (Momentum)**
- 🧱 **压力 (Pressure)**

### 4. 硬件与扩展
- **魔杖插件**：通过插件扩展魔杖的内存（卡槽）、供能效率与释放模式，定制你的专属法器。
- **魔力网络 (WIP)**：实装线缆、产生器、缓存器与路由设备，目标是实现法术的远程传输与自动化执行。

## 📥 安装指南 (Installation)

1.  **环境要求**：
  - Minecraft `1.21.11`
  - NeoForge `21.11.19-beta` 或更高版本
2.  **下载**：请前往 [GitHub Releases](https://github.com/creepebucket/programmable_magic/releases) 下载最新构建。
3.  **安装**：将下载的 `.jar` 文件放入游戏目录下的 `mods` 文件夹即可。

## 🛠️ 构建与开发 (Build & Dev)

如果你想参与开发或自行构建：

- **JDK 版本**：JDK 21 (Gradle Toolchain 会自动处理)
- **常用命令**：
  - 构建模组：`./gradlew build`
  - 运行客户端：`./gradlew runClient`
  - 生成数据 (DataGen)：`./gradlew runData`
- **资源流水线**：
  - 贴图切片：运行 `src/main/resources/buildassets.sh` (Windows下为 `.ps1`)，依赖 Python Pillow 库。
  - 自动化：DataGen 产物会在构建时自动合并，无需手动维护 `generated` 目录。

<details>
<summary>📂 点击展开代码结构概览</summary>

```text
src/main/java/org/creepebucket/programmable_magic
├─ spells/           # 法术核心：效果/调整/控制/载体/计算、序列执行机
├─ gui/wand/         # 交互界面：魔杖UI、编程面板
├─ items/            # 物品注册：法术卡、魔杖、魔力单元
├─ mananet/          # 魔力网络：传输协议与设备逻辑
├─ block/            # 方块与TileEntity
├─ registries/       # 注册中心 (DeferredRegisters)
└─ data/             # DataGen (配方、模型、LootTable)

src/main/resources
├─ assets/           # 静态资源 (Lang, Textures)
└─ build_assets.*    # 资源处理脚本
```
</details>

## 🗺️ 路线图 (Roadmap)

- [x] 基础法术序列与执行系统
- [x] 数学表达式与逻辑控制
- [x] 基础魔杖与插件系统
- [ ] **魔法科技化**：设备化的法术执行器、总线式网络、红石信号互转。
- [ ] **生态完善**：更多“载体”与“计算”模组，建立从开荒到后期的完整生产线。

## 📄 许可 (License)

**源代码**：采用 GPLv3 协议开源。

**美术资源**：本模组的美术资源包含原作者保留所有权利（ARR）的资产，以及部分原作者授权的第三方资产。绝大部分美术资源不可自由挪用。
详细的资源版权归属与来源名单，请务必查看项目中的 ASSET_LICENSE.md 文件。

---

<div align="center">
<a href="https://www.star-history.com/#creepebucket/programmable_magic&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&legend=top-left" />
 </picture>
</a>
</div>

---