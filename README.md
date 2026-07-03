
---

# Programmable Magic (可编程魔法)

<div align="center">

[![Gradle Package](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/creepebucket/programmable_magic/actions/workflows/gradle-publish.yml)
[![mc_version](https://img.shields.io/badge/minecraft-26.1.2-brightgreen?logo=minecraft)](https://github.com/creepebucket/programmable_magic)
[![last_commit](https://img.shields.io/github/last-commit/creepebucket/programmable_magic?logo=github)](https://github.com/creepebucket/programmable_magic/commits)
[![release](https://img.shields.io/github/v/release/creepebucket/programmable_magic?include_prereleases&logo=github)](https://github.com/creepebucket/programmable_magic/releases)
[![bilibili](https://img.shields.io/badge/bilibili-演示视频-00A1D6?logo=bilibili&logoColor=white)](https://www.bilibili.com/video/BV1mHS8BWE9u)
[![repo](https://img.shields.io/badge/repo-programmable_magic-181717?logo=github&logoColor=white)](https://github.com/creepebucket/programmable_magic)

![GitHub Repo Card](https://githubcard.com/creepebucket/programmable_magic.svg?d=dU-_JTBK)

</div>

## 🤔 把魔法写成代码，会怎样？

在大多数魔法模组里，法术是现成的——右键放火球，shift 放护盾，威力看等级，区别只在于数值。玩法始终是"选技能、按按键、等冷却"。

**Programmable Magic** 把施法变成了一门工程学。法术是零件——效果、运算、条件、循环。你需要把这些零件像写程序一样编排进魔杖，组合出自己的施法序列。

> 你的强弱，取决于你构建的逻辑。

---

## 🧩 一门真正的"魔法编程语言"

魔杖里流淌的是一段用卡片写成的程序。

**基础效果**是输出——爆炸、传送、方块放置、药水效果……这些是法术最终要做的事。一张火球还不够自动化，你需要**运算卡片**来计算——四则运算、三角函数、向量投影、动态获取施法者位置和视线方向，让参数告别硬编码。**控制卡片**来决策——`if` 条件分支区分敌我、`while` 循环批量处理、逻辑运算符组合判断。**触发器**来感知——接触地面时引爆、触碰到实体时追踪、延时精确到刻。

数字用 0-9 卡片拼积木一样拼出来。括号决定优先级，逗号分隔参数。70+ 张卡片构成了一门完整的编程语言——有类型系统、有运算符优先级、有作用域、有异常处理。

你组装的不是技能，是**可复用的法术函数**。一套优秀的序列可以针对战斗、挖掘、移动场景快速改装。

---

## 🔋 魔力是四维向量

传统的魔力就是一个数字，够不够看一眼蓝条就知道。这里，每条法术同时消耗**四种魔力**：辐射、温度、动量、压力。四系独立结算，任一项不足就施法失败。

这改变了发电机的意义——不能只造一种。风力涡轮产出动量，太阳能板产出辐射，水泵消耗动量产出压力，蒸汽锅炉把温度和压力转换成功。你需要搭建电力网级别的魔力网络，用线缆连接设备，用路由分配负荷，像管理电网一样管理魔力。

不同的法术序列需要不同的魔力配比，意味着你需要规划产能结构。想做爆炸法师就多修动量塔，想走辅助路线则温度和辐射产线得跟上。

---

## ⚙️ 魔杖即硬件，插件即配置

空手施法在这里不存在。魔杖是法术的物理载体，有固定的法术槽位和插件槽位。插件是**可插拔的硬件配置**：供给插件决定魔力转化效率，存储插件扩展法术容量，释放插件控制充能功率。

不同场景换不同插件，一把魔杖可以有完全不同的使用体验。这是模组化的思路——不强迫你农满级，只根据需求搭配。

---

## 📦 上手

1. 环境：Minecraft `26.1.2` + NeoForge，前置 **GeckoLib**
2. 从 [GitHub Releases](https://github.com/creepebucket/programmable_magic/releases) 下载
3. 放入 `mods`
4. 合成一把魔杖和一些法术卡片，潜行右键打开编程界面，开始组装你的第一个法术序列

---

## 📄 许可

**源代码**：GPLv3。**美术资源**：原作者保留所有权利（ARR），绝大部分美术资源不可自由挪用。详见 `ASSET_LICENSE.md`。

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
