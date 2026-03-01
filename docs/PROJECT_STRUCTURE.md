# 可编程魔法模组 - 项目结构简述

## 📋 项目概述

这是一个基于 NeoForge 1.21.1 的 Minecraft 模组，实现了可编程的魔法系统。玩家可以通过组合不同的"法术"来创建复杂的魔法效果，类似于编程语言的语法结构。

## 🏗️ 核心架构

### 主要组件

```
Programmable_magic (主类)
├── 注册系统 (registries/)
├── 法术系统 (spells/)
├── GUI系统 (gui/)
├── 网络通信 (network/)
├── 数据组件 (data/)
├── 实体系统 (entities/)
├── 物品系统 (items/)
├── 渲染系统 (renderer/)
├── 魔力网络 (mananet/)
└── 工具类 (ModUtils)
```

## 🔧 核心模块详解

### 1. 注册系统 (`registries/`)
负责所有游戏对象的注册管理：
- **ModItems.java** - 物品注册
- **SpellRegistry.java** - 法术注册
- **WandPluginRegistry.java** - 魔杖插件注册
- **ModEntityTypes.java** - 实体类型注册
- **ModBlockEntities.java** - 方块实体注册
- **ModMenuTypes.java** - 菜单类型注册
- **ModCreativeTabs.java** - 创造模式标签页

### 2. 法术系统 (`spells/`)
实现核心的可编程魔法逻辑：

#### 法术基础类
- **SpellItemLogic.java** - 法术逻辑抽象基类
- **SpellSequence.java** - 法术序列管理（双向链表）
- **ExecutionResult.java** - 法术执行结果封装
- **SpellValueType.java** - 法术值类型枚举

#### 法术分类
- **spells_base/** - 基础法术（实体交互、世界交互、视觉效果）
- **spells_compute/** - 计算法术（数字运算、向量运算、逻辑运算）
- **spells_adjust/** - 调整法术（条件判断、循环控制、触发器）
- **spells_control/** - 控制法术（流程控制）

#### 核心机制
- 使用调度场算法（Shunting Yard Algorithm）进行表达式解析
- 支持法术参数类型检查和自动类型推导
- 实现法术链表结构，支持动态插入和删除

### 3. GUI系统 (`gui/`)
现代化的用户界面系统：

#### 核心框架
- **lib/** - UI框架核心
  - `Widget.java` - 控件基类
  - `Screen.java` - 屏幕基类
  - 动画系统、生命周期管理
- **wand/** - 魔杖GUI实现
  - `WandScreen.java` - 魔杖主界面
  - `WandMenu.java` - 魔杖容器菜单
  - 插件系统集成

#### 特色功能
- 平滑动画过渡效果
- 响应式布局设计
- 插件化UI组件
- 实时调试功能

### 4. 网络通信 (`network/`)
- 数据包编解码器
- 客户端/服务端同步机制
- 实时数据传输

### 5. 魔杖系统 (`items/Wand.java`)
核心玩法载体：
- **充能机制** - 按住右键蓄力释放
- **插件系统** - 可装配功能扩展
- **法术存储** - 1024槽位法术序列
- **主题定制** - 可自定义界面颜色

### 6. 实体系统 (`entities/SpellEntity.java`)
法术执行的核心载体：
- 独立的实体生命周期
- 实时法术序列执行
- 调试模式支持
- 粒子效果生成

## 🎨 技术特色

### 1. 四维魔力系统
```java
public class Mana {
    public static final String RADIATION = "radiation";    // 辐射系
    public static final String TEMPERATURE = "temperature"; // 温度系
    public static final String MOMENTUM = "momentum";      // 动量系
    public static final String PRESSURE = "pressure";      // 压力系
}
```

### 2. 插件化架构
- 魔杖支持多种插件装配
- 每个插件可独立调整魔杖属性
- 支持客户端和服务端分离逻辑

### 3. 现代化渲染
- 自定义渲染管线
- 贝塞尔曲线连接线绘制
- 特殊模型渲染器
- 粒子系统集成

## 📁 目录结构

```
src/
├── main/
│   ├── java/
│   │   └── org/creepebucket/programmable_magic/
│   │       ├── client/              # 客户端专用代码
│   │       ├── compat/              # 兼容性模块
│   │       ├── data/                # 数据生成器
│   │       ├── entities/            # 实体定义
│   │       ├── events/              # 事件处理器
│   │       ├── gui/                 # GUI系统
│   │       ├── items/               # 物品定义
│   │       ├── mananet/             # 魔力网络
│   │       ├── network/             # 网络通信
│   │       ├── particles/           # 粒子效果
│   │       ├── recipes/             # 合成配方
│   │       ├── registries/          # 注册系统
│   │       ├── renderer/            # 渲染器
│   │       ├── spells/              # 法术系统
│   │       ├── ModUtils.java        # 工具类
│   │       └── Programmable_magic.java # 主类
│   └── resources/
│       ├── assets/                  # 资源文件
│       ├── build_assets.py          # 资源构建脚本
│       └── buildconfig.json         # 构建配置
└── generated/                       # 自动生成的资源
```

## 🔧 开发工具

### 资源构建系统
- **build_assets.py** - Python脚本自动生成材质
- **buildconfig.json** - 配置文件定义切片规则
- 跨平台支持（Windows/Linux/macOS）

### 构建配置
- Gradle自动化构建
- 数据生成器集成
- 资源打包优化

## 🎯 核心设计理念

1. **可编程性** - 法术组合如同编写程序
2. **模块化** - 插件系统提供功能扩展
3. **可视化** - 直观的GUI操作界面
4. **高性能** - 优化的渲染和执行效率
5. **易扩展** - 清晰的架构便于添加新功能

---
*文档版本：1.0 | 更新时间：2026-03-01*