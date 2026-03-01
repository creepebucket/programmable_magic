# SpellItemLogic 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/spells/api/SpellItemLogic.java`  
**包名**: `org.creepebucket.programmable_magic.spells.api`  
**继承关系**: `Object` → `SpellItemLogic` (抽象类)  
**实现接口**: `Cloneable`

## 🎯 类设计目的

`SpellItemLogic` 是整个法术系统的**核心抽象基类**，定义了所有法术必须遵循的契约和通用行为。它是连接法术逻辑与游戏系统的桥梁。

## 🏗️ 核心数据结构

### 链表指针系统
```java
// 双向链表节点指针
public SpellItemLogic next;  // 指向链表中的下一个法术节点
public SpellItemLogic prev;  // 指向链表中的前一个法术节点
```

**设计意义**：
- 构成法术序列的双向链表结构
- 支持法术间的上下文感知
- 实现表达式求值和调试功能的基础

### 运算属性定义
```java
public int precedence = 0;                    // 运算优先级 (数值越大优先级越高)
public boolean rightConnectivity = false;     // 右结合性标志
public boolean bypassShunting = false;        // 是否跳过调度场算法转换
```

**优先级示例**：
```java
// 常见法术优先级设置
加法/减法: precedence = 1        // 低优先级
乘法/除法: precedence = 2        // 中优先级
指数运算: precedence = 4         // 高优先级
函数调用: precedence = 3         // 中高优先级
括号标记: precedence = -99       // 强制最高优先级
```

### 类型系统
```java
// 输入输出类型定义（支持多重载）
public List<List<SpellValueType>> inputTypes = List.of(List.of(SpellValueType.EMPTY));
public List<List<SpellValueType>> outputTypes = List.of(List.of(SpellValueType.EMPTY));
```

**类型系统设计**：
```java
// 单一签名示例
inputTypes = List.of(List.of(SpellValueType.NUMBER))  // 接受一个数字

// 多重载签名示例
inputTypes = List.of(
    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),     // 数字+数字
    List.of(SpellValueType.NUMBER, SpellValueType.VECTOR3),    // 数字+向量
    List.of(SpellValueType.VECTOR3, SpellValueType.NUMBER)     // 向量+数字
)
```

### 基本属性
```java
public String name;                    // 法术唯一标识名
public String subCategory;             // 法术所属子类别
```

## 🔧 核心方法详解

### 抽象方法（必须实现）

#### `run()` - 法术执行核心
```java
public abstract ExecutionResult run(Player caster, SpellSequence spellSequence, 
                                  List<Object> paramsList, SpellEntity spellEntity);
```

**参数详解**：
- `caster`: `Player` - 法术的实际施法者玩家对象
- `spellSequence`: `SpellSequence` - 当前完整的法术序列上下文
- `paramsList`: `List<Object>` - 经过类型检查的参数列表
- `spellEntity`: `SpellEntity` - 执行法术的实体环境

**设计要点**：
- 这是法术逻辑的具体实现位置
- 参数已经过验证，可直接使用
- 必须返回 `ExecutionResult` 对象控制执行流程

#### `canRun()` - 执行前置条件检查
```java
public abstract boolean canRun(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity);
```

**典型检查内容**：
```java
@Override
public boolean canRun(Player caster, SpellSequence spellSequence, 
                     List<Object> paramsList, SpellEntity spellEntity) {
    // 环境检查
    if (caster.isUnderWater()) return false;
    
    // 状态检查
    if (caster.getHealth() < 5.0f) return false;
    
    // 权限检查
    if (!caster.hasPermissions(2)) return false;
    
    return true;
}
```

#### `getManaCost()` - 动态魔力消耗计算
```java
public abstract ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                         List<Object> paramsList, SpellEntity spellEntity);
```

**动态消耗示例**：
```java
@Override
public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                List<Object> paramsList, SpellEntity spellEntity) {
    // 根据参数值计算消耗
    Double distance = (Double) paramsList.get(0);
    double baseCost = Math.abs(distance) * 0.5;
    
    // 根据施法者状态调整
    double multiplier = caster.hasEffect(MobEffects.STRENGTH) ? 0.8 : 1.0;
    
    return new ModUtils.Mana(
        baseCost * multiplier,    // 辐射系
        0,                        // 温度系
        baseCost * 0.3,           // 动量系
        0                         // 压力系
    );
}
```

### 重要实现方法

#### `runWithCheck()` - 带完整验证的安全执行
```java
public ExecutionResult runWithCheck(Player caster, SpellSequence spellSequence, 
                                   SpellEntity spellEntity) {
    // 1. 参数类型匹配检查
    // 2. 执行条件验证 (canRun)
    // 3. 魔力充足性检查
    // 4. 插件前置钩子调用
    // 5. 实际法术执行 (run)
    // 6. 插件后置钩子调用
    // 7. 魔力扣除
    // 8. 返回值处理和序列更新
}
```

**执行流程图**：
```
开始 → 参数匹配 → 条件检查 → 魔力检查 → 插件前置 → 法术执行 → 插件后置 → 扣除魔力 → 处理返回值 → 结束
  ↓       ↓         ↓         ↓         ↓         ↓         ↓         ↓         ↓           ↓
 参数    类型      执行      魔力      自定义    核心      自定义    四维      返回值      序列
 不匹配   匹配     不满足     不足      逻辑      逻辑      逻辑      扣除      处理        更新
  ↓       ↓         ↓         ↓         ↓         ↓         ↓         ↓         ↓           ↓
错误提示  继续    错误提示  错误提示    执行      执行      执行      完成      完成        完成
```

### 克隆机制
```java
@Override
public SpellItemLogic clone() {
    try {
        SpellItemLogic clone = (SpellItemLogic) super.clone();
        // 清理链表引用
        clone.next = null;
        clone.prev = null;
        // 深拷贝类型信息
        clone.inputTypes = new ArrayList<>();
        for (List<SpellValueType> overload : inputTypes) {
            clone.inputTypes.add(new ArrayList<>(overload));
        }
        clone.outputTypes = new ArrayList<>();
        for (List<SpellValueType> overload : outputTypes) {
            clone.outputTypes.add(new ArrayList<>(overload));
        }
        return clone;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // 不应该发生
    }
}
```

## 🔗 内部类型定义

### 法术分类标记接口
```java
// 语义化分类接口，用于IDE提示和插件识别
public interface BaseSpell {}      // 基础操作法术
public interface ComputeMod {}     // 计算处理法术
public interface ControlMod {}     // 流程控制法术
public interface AdjustMod {}      // 状态调整法术
```

### 配对法术支持
```java
// 左配对法术基类
public abstract static class PairedLeftSpell extends SpellItemLogic {
    public PairedRightSpell rightSpell;                    // 对应的右配对法术
    public Class<? extends PairedRightSpell> rightSpellType; // 右法术类型约束
    public PairedLeftSpell cloned;                         // 克隆缓存
    public boolean expired = true;                         // 过期状态标记
}

// 右配对法术基类
public abstract static class PairedRightSpell extends SpellItemLogic {
    public PairedLeftSpell leftSpell;                      // 对应的左配对法术
    public Class<? extends PairedLeftSpell> leftSpellType; // 左法术类型约束
    public PairedRightSpell cloned;                        // 克隆缓存
    public boolean expired = true;                         // 过期状态标记
}
```

**配对机制示例**：
```java
// 括号配对
LParenSpell ↔ RParenSpell
IfStartSpell ↔ IfEndSpell  
LoopStartSpell ↔ LoopEndSpell

// 克隆时自动维护配对关系
PairedLeftSpell.clone() → 自动克隆对应的rightSpell
PairedRightSpell.clone() → 自动克隆对应的leftSpell
```

## 🔄 与其他核心类的关系

### 与 SpellSequence 的协作
```java
// SpellItemLogic 作为 SpellSequence 的节点元素
SpellSequence sequence = new SpellSequence();
sequence.pushRight(new AdditionSpell());    // 添加法术节点
sequence.pushRight(new NumberDigitSpell(5));

// 通过 next/prev 指针形成链式结构
AdditionSpell.next = NumberDigitSpell(5)
NumberDigitSpell(5).prev = AdditionSpell
```

### 与 SpellEntity 的交互
```java
// SpellEntity 持有序列并执行其中的法术
SpellEntity entity = new SpellEntity(level, player, sequence, spellData, mana, plugins, debugMode);
// 在 entity.tick() 中调用
currentSpell.runWithCheck(caster, spellSequence, entity);
```

### 与 ExecutionResult 的数据流
```java
// 法术执行返回 ExecutionResult 控制后续行为
ExecutionResult result = spell.runWithCheck(...);

// 控制执行流程的关键字段
result.nextSpell    // 下一个执行的法术
result.delayTicks   // 执行延迟刻数
result.doStop       // 是否终止执行
result.returnValue  // 法术返回值
```

## 💡 实际使用示例

### 简单法术实现
```java
public class DebugPrintSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {
    public DebugPrintSpell() {
        name = "debug_print";
        subCategory = "spell." + MODID + ".subcategory.visual";
        inputTypes = List.of(List.of(SpellValueType.STRING));
        outputTypes = List.of(List.of());
        precedence = -99;        // 立即执行
        bypassShunting = true;   // 跳过调度场转换
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        String message = (String) paramsList.get(0);
        caster.sendSystemMessage(Component.literal("[DEBUG] " + message)
            .withStyle(ChatFormatting.GREEN));
        return ExecutionResult.SUCCESS(this);
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, 
                         List<Object> paramsList, SpellEntity spellEntity) {
        return true; // 总是可以执行
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                    List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana(0.1, 0.1, 0.1, 0.1); // 微量消耗
    }
}
```

### 复杂计算法术
```java
public class VectorCrossProductSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    public VectorCrossProductSpell() {
        name = "cross_product";
        subCategory = "spell." + MODID + ".subcategory.operations.vector";
        inputTypes = List.of(List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3));
        outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        precedence = 3; // 中高优先级
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        Vec3 vec1 = (Vec3) paramsList.get(0);
        Vec3 vec2 = (Vec3) paramsList.get(1);
        
        // 计算叉积
        Vec3 result = vec1.cross(vec2);
        
        return ExecutionResult.RETURNED(this, List.of(result), 
                                       List.of(SpellValueType.VECTOR3));
    }

    // ... 其他方法实现
}
```

## ⚠️ 开发注意事项

### 必须遵守的规范
1. **线程安全性**：法术对象可能被多个SpellEntity并发访问
2. **状态管理**：避免在法术对象中保存玩家特定状态
3. **克隆实现**：正确处理链表引用和复杂对象的深拷贝
4. **类型准确性**：inputTypes/outputTypes必须准确反映实际行为

### 性能优化建议
1. **魔力计算**：getManaCost()应尽量轻量化，避免复杂计算
2. **对象复用**：考虑实现对象池减少垃圾回收压力
3. **缓存策略**：对于不变的计算结果可以考虑缓存
4. **早期返回**：在canRun()中尽早拒绝不可能成功的执行

### 调试支持
```java
// 在开发阶段可以添加详细日志
@Override
public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                          List<Object> paramsList, SpellEntity spellEntity) {
    if (ModUtils.DEBUG_MODE) {
        System.out.println("Executing " + name + " with params: " + paramsList);
    }
    // ... 实际逻辑
}
```

---
*相关文档链接：*
- [SpellSequence 详解](../api/SpellSequence.md)
- [ExecutionResult 详解](../api/ExecutionResult.md)  
- [SpellEntity 详解](../../entities/SpellEntity.md)
- [法术开发指南](../../../development/spell-development.md)