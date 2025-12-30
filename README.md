# Programmable Magic（可编程魔法）

> “它看起来像一串随意拼起来的法术卡片：爆炸、速度、括号、加号……但当你把它们串成一个序列，你就会明白它真正的目标：让魔法像工程一样可组合、可复用、可扩展。”
>
> ——GPT-5.2 Thinking（乱编）

[![Gradle Package](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml)
[![mc_version](https://img.shields.io/badge/minecraft-1.21.6~.8-brightgreen?logo=minecraft)](https://github.com/creepebucket/programmable_magic)
[![last_commit](https://img.shields.io/github/last-commit/creepebucket/programmable_magic?logo=github)](https://github.com/creepebucket/programmable_magic/commits)
[![release](https://img.shields.io/github/v/release/creepebucket/programmable_magic?include_prereleases&logo=github)](https://github.com/creepebucket/programmable_magic/releases)
[![bilibili](https://img.shields.io/badge/bilibili-点击观看-00A1D6?logo=bilibili&logoColor=white)](https://www.bilibili.com/video/BV1mHS8BWE9u)
[![repo](https://img.shields.io/badge/repo-programmable_magic-181717?logo=github&logoColor=white)](https://github.com/creepebucket/programmable_magic)

![GitHub Repo Card](https://githubcard.com/creepebucket/programmable_magic.svg?d=dU-_JTBK)

<a href="https://www.star-history.com/#creepebucket/programmable_magic&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=creepebucket/programmable_magic&type=date&legend=top-left" />
 </picture>
</a>

## 概述

Programmable Magic 是一个基于 NeoForge 1.21.8 的魔法模组：核心玩法不是“学会一个强力技能”，而是把一组「法术卡片」按顺序放进魔杖界面，组合出可复用的施法序列。你可以把它理解成“把魔法写成一条能跑的流程”——并且随着插件、计算模组与网络设备的加入，这条流程会逐渐工程化。

## 注意

- 版本：本项目基于 NeoForge `1.21.8`（`minecraft_version_range=[1.21.6,1.22)`）。
- 进度：目前仍处于早期开发阶段，内容、平衡与配方可能会频繁调整。

## 相关

- 灵感方向：组合式法术系统与“把能力工程化”的玩法（本项目为独立实现，不依赖其他同类模组）。
- 反馈与交流：欢迎提 Issue/PR，或通过 README 顶部链接联系作者。

## 系统构成

- 法术序列：一次释放会按顺序执行“法术卡片”，其中既有「效果」也有「修饰/控制/载体/计算」等模块。
- 表达式计算：提供数字、括号与运算符等计算模组，让法术参数更像“写公式”而不是“堆配置”。
- 魔力消耗：以「辐射 / 温度 / 动量 / 压力」四系魔力作为成本维度，从背包中的「魔力单元」扣除。
- 魔杖与插件：通过插件扩展存储槽、供应与释放等能力，让同一把魔杖可以走不同的“构筑路线”。
- 魔力网络（开发中）：已实装线缆/产生器/缓存器与基础网络逻辑，为后续设备化与自动化做地基。

## 主要特点

总体来说，Programmable Magic…

- 鼓励“组合”而不是“刷数值”：强度更多来自组合方式与参数表达，而不是单一等级的堆叠。
- 鼓励“可复用”的施法构筑：一组法术序列可以针对不同场景快速改装与复制。
- 试图把魔法推向工程化：后续路线将围绕网络、路由、缓存与输入输出等方向展开（仍在开发中）。

## 当前已实装（节选）

- 基础效果：爆炸、速度加成、放置方块、药水附加、点燃、萤光、数据打印等。
- 调整模组：延时、触地触发、触实体触发、威力提升/倍率等。
- 控制模组：条件判断、循环、逻辑运算（与/或/非）与比较运算（等于/不等/大小比较）等。
- 计算模组：数字与四则/幂、括号、动态常量（如视角向量、施法者位置/实体等）、存储读写等。
- 魔力网络原型：魔力线缆、魔力产生器、魔力缓存器。

## 最前期体验（当前已实装的开局流程）

- 提纯红石粉：红石粉右击「盛水炼药锅」（下方有火/灵魂火）→ 掉落「纯净红石粉」。
- 原始合金熔炼炉：
  - 任意锄头右击砖块 → 转化为合金炉（扣 1 点耐久，返还 1×「砖」），并具有朝向与三态贴图/音效。
  - 投入「纯净红石粉」+「金锭」，用岩浆桶点火 5 秒完成；空手可取回原料。
  - 挖掘掉落：任何状态掉落 3×「砖」；若为「阻挡状态」且用「镐类工具」挖掘，额外掉落 2×「红石-金合金」+ 1×「黑曜石」。

## 安装

- 运行环境：Minecraft `1.21.8` + NeoForge `21.8.33`（或满足 `neo_version_range` 的兼容版本）。
- 获取方式：优先从 GitHub Releases 下载成品构建。
- 使用方法：将本模组的 `.jar` 放入 `mods` 文件夹后启动游戏。

## 构建与开发
- 要求：JDK 21（Gradle Toolchain 自动处理）、NeoForge 1.21.8。
- 常用命令：
  - 构建：`./gradlew build`（Windows 使用 `gradlew.bat build`）
  - 运行客户端：`./gradlew runClient`（Windows 使用 `gradlew.bat runClient`）
  - 数据生成：`./gradlew runData`（Windows 使用 `gradlew.bat runData`）
- 流水线：
  - 贴图切片：`src/main/resources/buildassets.sh`（Windows 使用 `buildassets.ps1`，依赖 Pillow），构建前自动生成 UI/物品/方块贴图切片。
  - 数据生成：datagen 产物在打包前自动合并到构建资源目录，无需手工维护 `generated` JSON。

## 代码结构（概览）
```
src/main/java/org/creepebucket/programmable_magic
├─ spells/           # 法术核心：效果/调整/控制/载体/计算、序列执行、费用计算
├─ gui/wand/         # 魔杖界面：法术展示/存储槽位、交互管线
├─ items/            # 物品：法术卡片、魔杖、魔力单元等
├─ mananet/          # 魔力网络：接口与简化网络原型（开发中）
├─ block(+blockentity)/events/  # 方块与交互事件（含早期流程方块）
├─ registries/       # 注册中心（方块/物品/方块实体/菜单/标签/法术）
└─ data/             # 数据生成器（blockstate、模型、客户端 items）

src/main/resources
├─ assets/programmable_magic    # 基础资源（语言、手写模型/状态）
├─ build_assets.py|.sh|.json    # 贴图切片脚本与配置
└─ pack.mcmeta                  # 资源包元数据

src/generated/resources         # 数据生成输出（构建时自动合并）
```

## 路线图（简要）
- 魔法科技：设备化的法术执行/路由/缓存与输入输出，总线/节点式网络，联动红石与外部能力。
- 法术生态：更多可组合的“载体/控制/计算”模组与素材生产线，形成从开荒到中后期的闭环。

## 许可

本项目使用 `GPL-3.0`，详见 `LICENSE`。

# 你为啥直接 commit 到我的 master 分支啊？！GitHub 上根本不是这样！你应该先 fork 我的仓库，然后从 develop 分支 checkout 一个新的 feature 分支，比如叫 feature/confession。然后你把你的心意写成代码，并为它写好单元测试和集成测试，确保代码覆盖率达到95%以上。接着你要跑一下 Linter，通过所有的代码风格检查。然后你再 commit，commit message 要遵循 Conventional Commits 规范。之后你把这个分支 push 到你自己的远程仓库，然后给我提一个 Pull Request。在 PR 描述里，你要详细说明你的功能改动和实现思路，并且 @ 我和至少两个其他的评审。我们会 review 你的代码，可能会留下一些评论，你需要解决所有的 thread。等 CI/CD 流水线全部通过，并且拿到至少两个 LGTM 之后，我才会考虑把你的分支 squash and merge 到 develop 里，等待下一个版本发布。你怎么直接上来就想 force push 到 main？！GitHub 上根本不是这样！我拒绝合并！
# 姑姑嘎嘎!
# 别扯了，你那破 repo 一共就俩人儿，其中一个还是个bot，我从哪给你凑5个评审去。唉算了，你直接给把仓库密钥给我得了，我强推你master，代码还没警告多呢，啥也不是。
