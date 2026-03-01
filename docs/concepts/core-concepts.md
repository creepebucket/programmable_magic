# 核心概念详解

作为新开发者，理解这些核心概念对于掌握项目至关重要。

## 🧠 法术系统原理

### 法术的本质
法术在本系统中本质上是一个**可执行的函数对象**：

```java
public abstract class SpellItemLogic implements Cloneable {
    // 输入参数类型定义
    public List<List<SpellValueType>> inputTypes;
    // 输出参数类型定义  
    public List<List<SpellValueType>> outputTypes;
    // 运算优先级
    public int precedence;
    // 结合性（左结合/右结合）
    public boolean rightConnectivity;
    
    // 核心执行方法
    public abstract ExecutionResult run(Player caster, SpellSequence spellSequence, 
                                      List<Object> paramsList, SpellEntity spellEntity);
}
```

### 法术链表结构
所有法术通过双向链表连接：

```
[法术A] ↔ [法术B] ↔ [法术C] ↔ [法术D]
   ↑         ↑         ↑         ↑
 prev      prev      prev      prev
 next      next      next      next
```

这种设计的优势：
- ✅ 支持动态插入和删除
- ✅ 便于表达式求值
- ✅ 内存局部性好
- ✅ 易于实现调试功能

### 调度场算法应用
法术表达式的解析使用经典的调度场算法：

**中缀表达式**：`1 + 2 * 3`
**转换过程**：
```
输入: 1 + 2 * 3
输出队列: [1]
操作符栈: [+]

输入: 1 + 2 * 3  
输出队列: [1, 2]
操作符栈: [+, *]

最终后缀表达式: [1, 2, 3, *, +]
```

## 🔮 四维魔力系统深度解析

### 设计理念
为什么选择四维而非传统的单一魔力？

```
传统魔力: 🔵 Mana → 数值消耗
四维魔力: 🌈 [辐射][温度][动量][压力] → 多维度能量平衡
```

### 各维度含义

#### 辐射系 (Radiation) ⚡
- **用途**：光效、能量释放、视觉魔法
- **典型消耗**：`spawn_particles`、`debug_print`
- **特性**：通常是次要消耗，主要用于视觉反馈

#### 温度系 (Temperature) 🔥
- **用途**：热力学效应、燃烧、熔化
- **典型消耗**：`ignite`、`explosion`
- **特性**：高温度法术消耗较大

#### 动量系 (Momentum) 🏃
- **用途**：移动、推动、动能相关
- **典型消耗**：`teleport`、`gain_velocity`
- **特性**：距离和速度影响消耗量

#### 压力系 (Pressure) 💪
- **用途**：物理破坏、压缩、结构改变
- **典型消耗**：`break_block`、`place_block`
- **特性**：材料硬度影响消耗

### 魔力消耗策略
```java
public class Mana {
    // 检查是否任一维度不足
    public boolean anyGreaterThan(Mana available) {
        return radiation > available.radiation ||
               temperature > available.temperature ||
               momentum > available.momentum ||
               pressure > available.pressure;
    }
    
    // 扣除魔力
    public void subtract(Mana cost) {
        radiation -= cost.radiation;
        temperature -= cost.temperature;
        momentum -= cost.momentum;
        pressure -= cost.pressure;
    }
}
```

## 🎛️ 插件系统机制

### 插件的双重性质
插件同时存在于**服务端**和**客户端**，但职责不同：

```
服务端插件 (WandPluginLogic)
├── 调整魔杖数值属性
├── 处理法术执行逻辑
└── 管理游戏状态

客户端插件 (BasePlugin)
├── 创建UI界面组件
├── 处理用户交互
└── 提供视觉反馈
```

### 插件生命周期
```java
public abstract class BasePlugin {
    // 插件被添加到魔杖时
    public void onAdd(WandScreen screen) {
        // 创建UI组件
        // 注册事件监听器
        // 初始化状态
    }
    
    // 插件被移除时
    public void onRemove(WandScreen screen) {
        // 清理资源
        // 移除UI组件
        // 保存状态
    }
    
    // 法术执行钩子
    public void beforeSpellExecution(...) { /* 执行前 */ }
    public void afterSpellExecution(...) { /* 执行后 */ }
}
```

## 🎨 GUI系统架构

### 组件化设计思想
借鉴现代前端框架的理念：

```
Screen (屏幕)
└── Widget (控件树)
    ├── ButtonWidget (按钮)
    ├── TextWidget (文本)
    ├── SlotWidget (槽位)
    ├── ContainerWidget (容器)
    └── CustomWidget (自定义)
```

### 动画系统
```java
// 平滑值动画
public class SmoothedValue {
    private double target;
    private double current;
    private double speed;
    
    public void doStep(double deltaTime) {
        current += (target - current) * speed * deltaTime;
    }
}

// 预定义动画
animations.add(new Animation.FadeIn(0.3));     // 0.3秒淡入
animations.add(new Animation.Scale(1.0, 2.0)); // 缩放动画
```

### 坐标系统
```java
// 相对坐标定位
Coordinate.fromTopLeft(10, 20);     // 距离左上角(10,20)
Coordinate.fromBottomRight(-5, -5); // 距离右下角(5,5)
Coordinate.fromCenter(0, 0);        // 相对于中心点
```

## 🔄 数据同步机制

### 客户端-服务端通信

#### 状态同步
```java
// 服务端状态变更
public class WandMenu extends Menu {
    public SyncedValue<Boolean> debugMode;
    
    // 状态改变时自动同步
    debugMode.set(true); // 自动发送到客户端
}

// 客户端接收同步
public class WandScreen extends Screen {
    // debugMode值会自动更新
    if (menu.debugMode.get()) {
        // 显示调试界面
    }
}
```

#### 数据包通信
```java
// 自定义数据包
public class SpellDebugPacket {
    private final BlockPos position;
    private final String spellName;
    private final List<Object> parameters;
    
    // 编码解码
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellDebugPacket> CODEC = 
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, SpellDebugPacket::position,
            ByteBufCodecs.STRING_UTF8, SpellDebugPacket::spellName,
            // ... 其他字段
            SpellDebugPacket::new
        );
}
```

## 🎯 调试系统

### 实体级调试
每个法术都在独立的 `SpellEntity` 中执行：

```java
public class SpellEntity extends Entity {
    public boolean debugMode = false;
    public boolean doStep = false;    // 单步执行
    public boolean doRun = false;     // 继续执行
    public SpellItemLogic currentSpell; // 当前执行的法术
    
    @Override
    public void tick() {
        if (debugMode && !doStep && !doRun) {
            return; // 暂停执行等待调试命令
        }
        
        // 执行当前法术
        ExecutionResult result = currentSpell.runWithCheck(...);
        
        // 更新调试状态
        doStep = false;
    }
}
```

### 调试UI组件
```java
// 调试控制面板
public class DebugControlWidget extends Widget {
    private ImageButtonWidget stepButton;    // 单步
    private ImageButtonWidget runButton;     // 运行
    private ImageButtonWidget pauseButton;   // 暂停
    private TextWidget statusText;           // 状态显示
}
```

## 💡 设计哲学总结

### KISS原则贯彻
- **简单直接**：每个类职责单一
- **避免过度设计**：够用就好
- **易于理解**：代码即文档

### 面向对象设计
- **继承层次清晰**：合理的抽象层级
- **接口隔离**：小而专的接口
- **组合优于继承**：灵活的功能组合

### 性能考虑
- **延迟加载**：按需创建资源
- **对象池**：重用频繁创建的对象
- **批量操作**：减少系统调用次数

---
*理解了这些核心概念后，建议继续阅读 [系统架构](../architecture/system-architecture.md) 了解整体设计。*