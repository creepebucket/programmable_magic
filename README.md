# Programmable Magic

> AI写的, 将就看吧, 比没有好

[![Gradle Package](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml)
[![mc_version](https://img.shields.io/badge/minecraft-1.21.6~.8-brightgreen?logo=minecraft)](https://github.com/creepebucket/programmable_magic)
[![last_commit](https://img.shields.io/github/last-commit/creepebucket/programmable_magic?logo=github)](https://github.com/creepebucket/programmable_magic/commits)
[![release](https://img.shields.io/github/v/release/creepebucket/programmable_magic?include_prereleases&logo=github)](https://github.com/creepebucket/programmable_magic/releases)
[![bilibili](https://img.shields.io/badge/bilibili-点击观看-00A1D6?logo=bilibili&logoColor=white)](https://www.bilibili.com/video/BV1mHS8BWE9u)
[![repo](https://img.shields.io/badge/repo-programmable_magic-181717?logo=github&logoColor=white)](https://github.com/creepebucket/programmable_magic)

一个“可编排法术”为核心的魔法模组（NeoForge 1.21.8）。玩家用“法术卡片（物品）”在魔杖界面拼接序列来施法，配合四系魔力与未来的魔法科技，做出既有表达力又能工程化的玩法体系。

## 你能做什么（亮点）
- 可编排法术系统：
  - 基础法术（爆炸、速度、数据标记等）是“效果”，真正的编排力来自“修饰”。
  - 修饰类别：数值调整（强度倍率等）、控制调整（延时等）、载体/目标（投射物等）、计算模组（数字与四则运算）。
  - 在魔杖 GUI 中把法术物品放入“存储槽”，按序列执行；计算模组为后续法术提供参数，让“堆法术”变成“写公式”。
- 四系魔力消耗模型：辐射/温度/动量/压力，按法术组动态计算需求，背包中的“魔力单元”按类型扣除。
- 工程向“魔法科技”（WIP）：
  - 已有最小网络原型（Mana Cable + 简化连通标识），后续拓展能量巴士、节点设备与自动化接口。
  - 所有玩法组件尽量数据驱动，可扩展、易联动。
- 一键资产与数据生成：贴图自动切片、blockstate/模型/标签由 datagen 产出，减少手写 JSON。

## 最前期体验（当前已实装的开局流程）
- 提纯红石粉：红石粉右击“盛水炼药锅”（下方有火/灵魂火）→ 掉落“纯净红石粉”。
- 原始合金熔炼炉：
  - 任意锄头右击砖块→转化为合金炉（扣 1 点耐久，返还 1×物品“砖”）；方块具朝向与三态贴图/音效。
  - 投入纯净红石粉 + 金锭，岩浆桶点火 5 秒→完成；空手可取回原料。
  - 挖掘掉落：任何状态掉 3×物品“砖”，若为“阻挡状态”且用“镐类工具”，额外 2×红金合金 + 1×黑曜石。

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
├─ mana/             # 魔力网络：接口与简化网络原型（WIP）
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
- 魔法科技：设备化的法术执行/路由/缓存与 IO，总线/节点式网络，联动红石与外部能力。
- 法术生态：更多可组合的“载体/控制/计算”模组与素材生产线，形成从开荒到中后期的闭环。

欢迎提出 Issue/PR，一起把“可编排法术 + 魔法科技”做成既能玩又能造的系统。
