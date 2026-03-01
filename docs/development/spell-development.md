# 法术开发指南

详细指导如何开发新的法术功能。

## 🎯 法术开发准备工作

### 环境检查清单
- [ ] JDK 17+ 安装并配置
- [ ] IDE 配置完成（推荐 IntelliJ IDEA）
- [ ] 项目能够正常编译运行
- [ ] 理解 [核心概念](../concepts/core-concepts.md)

### 开发流程概览
```
需求分析 → 设计法术逻辑 → 实现核心功能 → 注册法术 → 测试验证 → 文档完善
    ↓         ↓            ↓          ↓        ↓         ↓
 [明确功能] [确定参数]  [编写代码] [添加注册] [功能测试] [更新文档]
```

## 📝 第一个法术：Hello World

让我们从最简单的法术开始——在聊天框输出消息。

### 1. 创建法术类
```java
package org.creepebucket.programmable_magic.spells.spells_base;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;
import org.creepebucket.programmable_magic.spells.api.SpellValueType;

import java.util.List;

public class HelloWorldSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {
    
    public HelloWorldSpell() {
        // 基本属性设置
        name = "hello_world";                           // 法术唯一标识名
        subCategory = "spell." + MODID + ".subcategory.visual"; // 所属子类别
        
        // 输入输出类型定义（这个法术不需要输入，没有输出）
        inputTypes = List.of(List.of(SpellValueType.EMPTY));                // 无输入参数
        outputTypes = List.of(List.of(SpellValueType.EMPTY));               // 无返回值
        
        // 运算属性
        precedence = -99;                               // 低优先级（立即执行）
        bypassShunting = true;                          // 跳过后缀转换
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 核心执行逻辑
        caster.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("Hello, Programmable Magic!")
                .withStyle(net.minecraft.ChatFormatting.GREEN)
        );
        
        // 返回执行成功
        return ExecutionResult.SUCCESS(this);
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, 
                         List<Object> paramsList, SpellEntity spellEntity) {
        // 检查是否可以执行（这里总是可以执行）
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                    List<Object> paramsList, SpellEntity spellEntity) {
        // 魔力消耗（几乎不消耗魔力）
        return new ModUtils.Mana(0.1, 0.1, 0.1, 0.1);
    }
}
```

### 2. 注册法术
在 `SpellRegistry.java` 中添加：

```java
public static void registerSpells(IEventBus eventBus) {
    // ... 现有的法术注册
    
    // 注册我们的新法术
    registerSpell(HelloWorldSpell::new);
    
    // ... 其他法术
}
```

### 3. 添加本地化文本
在 `src/main/resources/assets/programmable_magic/lang/en_us.json` 中添加：

```json
{
    "item.programmable_magic.spell_display_hello_world": "Hello World",
    "spell.programmable_magic.subcategory.visual": "Visual Effects"
}
```

对应的中文翻译：
```json
{
    "item.programmable_magic.spell_display_hello_world": "你好世界",
    "spell.programmable_magic.subcategory.visual": "视觉效果"
}
```

### 4. 测试法术
1. 运行游戏 `./gradlew runClient`
2. 给自己一个魔杖 `/give @p programmable_magic:wand`
3. 打开魔杖界面（潜行+右键）
4. 从法术供应区找到"Hello World"法术
5. 拖拽到存储区
6. 释放法术查看效果

## 🔧 法术参数处理

### 带参数的法术示例

让我们创建一个可以输出自定义消息的法术：

```java
public class PrintMessageSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    
    public PrintMessageSpell() {
        name = "print_message";
        subCategory = "spell." + MODID + ".subcategory.visual";
        
        // 定义输入参数：需要一个字符串
        inputTypes = List.of(List.of(SpellValueType.STRING));
        outputTypes = List.of(List.of(SpellValueType.EMPTY)); // 无返回值
        
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取参数（注意类型转换）
        String message = (String) paramsList.get(0);
        
        // 输出消息
        caster.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("消息: " + message)
                .withStyle(net.minecraft.ChatFormatting.AQUA)
        );
        
        return ExecutionResult.SUCCESS(this);
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, 
                         List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                    List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana(0.5, 0.5, 0.5, 0.5);
    }
}
```

### 参数来源说明
参数来自法术链表中前面的法术：
```
"Hello" → print_message
   ↑         ↑
字符串字面量  需要字符串参数的法术
```

## 🎯 复杂法术开发

### 数学运算法术示例

创建一个计算两个数平均值的法术：

```java
public class AverageSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    
    public AverageSpell() {
        name = "average";
        subCategory = "spell." + MODID + ".subcategory.operations.number";
        
        // 需要两个数字参数
        inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        // 返回一个数字结果
        outputTypes = List.of(List.of(SpellValueType.NUMBER));
        
        precedence = 1; // 低优先级
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取两个参数
        Double num1 = (Double) paramsList.get(0);
        Double num2 = (Double) paramsList.get(1);
        
        // 计算平均值
        Double average = (num1 + num2) / 2.0;
        
        // 返回结果（注意返回值的格式）
        return ExecutionResult.RETURNED(
            this,                           // 当前法术
            List.of(average),              // 返回值列表
            List.of(SpellValueType.NUMBER) // 返回值类型列表
        );
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, 
                         List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                    List<Object> paramsList, SpellEntity spellEntity) {
        // 根据参数大小计算消耗
        Double num1 = (Double) paramsList.get(0);
        Double num2 = (Double) paramsList.get(1);
        double baseCost = Math.abs(num1) + Math.abs(num2);
        
        return new ModUtils.Mana(baseCost * 0.1, 0, 0, 0);
    }
}
```

### 使用示例
```
法术序列: 10 → 20 → average
执行过程:
1. 10 返回数值 10.0
2. 20 返回数值 20.0  
3. average 获取两个参数 (10.0, 20.0)
4. 计算平均值 15.0
5. 返回结果 15.0
```

## 🔄 控制流法术

### 条件判断法术

```java
public class GreaterThanSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
    
    public GreaterThanSpell() {
        name = "greater_than";
        subCategory = "spell." + MODID + ".subcategory.operations.boolean";
        
        // 需要两个数字比较
        inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        outputTypes = List.of(List.of(SpellValueType.BOOLEAN));
        
        precedence = 2; // 中等优先级
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        Double left = (Double) paramsList.get(0);
        Double right = (Double) paramsList.get(1);
        
        boolean result = left > right;
        
        return ExecutionResult.RETURNED(
            this,
            List.of(result),
            List.of(SpellValueType.BOOLEAN)
        );
    }

    // ... 其他方法实现
}
```

### 循环控制法术

```java
public class LoopStartSpell extends SpellItemLogic.PairedLeftSpell 
                           implements SpellItemLogic.ControlMod {
    
    public LoopStartSpell() {
        name = "loop_start";
        rightSpellType = LoopEndSpell.class; // 对应的结束标记
        subCategory = "spell." + MODID + ".subcategory.flow_control";
        
        // 无输入输出（控制标记）
        inputTypes = List.of(List.of(SpellValueType.EMPTY));
        outputTypes = List.of(List.of(SpellValueType.EMPTY));
        
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 循环开始标记，不执行实际逻辑
        return ExecutionResult.SUCCESS(this);
    }

    // ... 其他方法
}

public class LoopEndSpell extends SpellItemLogic.PairedRightSpell 
                         implements SpellItemLogic.ControlMod {
    
    public LoopEndSpell() {
        name = "loop_end";
        leftSpellType = LoopStartSpell.class;
        subCategory = "spell." + MODID + ".subcategory.flow_control";
        
        inputTypes = List.of(List.of(SpellValueType.BOOLEAN));
        outputTypes = List.of(List.of(SpellValueType.EMPTY));
        
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取条件判断结果
        Boolean condition = (Boolean) paramsList.get(0);
        
        if (condition) {
            // 条件为真，跳转回循环开始
            return new ExecutionResult(leftSpell, 0, false, null, null);
        } else {
            // 条件为假，继续向下执行
            return ExecutionResult.SUCCESS(this);
        }
    }

    // ... 其他方法
}
```

## 🎨 实体交互法术

### 传送法术实现

```java
public class TeleportSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {
    
    public TeleportSpell() {
        name = "teleport";
        subCategory = "spell." + MODID + ".subcategory.entity";
        
        // 需要位置向量和目标实体
        inputTypes = List.of(List.of(SpellValueType.VECTOR3, SpellValueType.ENTITY));
        outputTypes =List.of(List.of(SpellValueType.EMPTY));
        
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取参数
        Vec3 offset = (Vec3) paramsList.get(0);
        Entity target = (Entity) paramsList.get(1);
        
        // 计算目标位置
        Vec3 targetPos = target.position().add(offset);
        
        // 执行传送
        target.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        target.hurtMarked = true; // 标记位置已改变
        
        // 生成传送特效
        spawnTeleportParticles(target.level(), targetPos);
        
        return ExecutionResult.SUCCESS(this);
    }

    private void spawnTeleportParticles(Level level, Vec3 position) {
        // 在目标位置生成粒子效果
        for (int i = 0; i < 20; i++) {
            level.addParticle(
                ParticleTypes.PORTAL,
                position.x + (level.random.nextDouble() - 0.5) * 2.0,
                position.y + level.random.nextDouble() * 2.0,
                position.z + (level.random.nextDouble() - 0.5) * 2.0,
                (level.random.nextDouble() - 0.5) * 2.0,
                -level.random.nextDouble(),
                (level.random.nextDouble() - 0.5) * 2.0
            );
        }
    }

    // ... 其他方法实现
}
```

## ⚡ 高级技巧

### 1. 状态保持
```java
public class CounterSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    // 使用静态变量保持状态（注意线程安全）
    private static final Map<UUID, Integer> counters = new ConcurrentHashMap<>();
    
    public CounterSpell() {
        name = "counter";
        // ... 其他属性
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        UUID playerId = caster.getUUID();
        int currentCount = counters.getOrDefault(playerId, 0);
        counters.put(playerId, currentCount + 1);
        
        return ExecutionResult.RETURNED(
            this,
            List.of((double) currentCount),
            List.of(SpellValueType.NUMBER)
        );
    }
}
```

### 2. 异步操作
```java
public class DelayedSpell extends SpellItemLogic implements SpellItemLogic.AdjustMod {
    
    public DelayedSpell() {
        name = "delayed_action";
        inputTypes = List.of(List.of(SpellValueType.NUMBER));
        outputTypes = List.of(List.of(SpellValueType.EMPTY));
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取延迟时间（刻）
        int delayTicks = ((Double) paramsList.get(0)).intValue();
        
        // 返回延迟执行结果
        return new ExecutionResult(next, delayTicks, false, null, null);
    }
}
```

### 3. 错误处理
```java
public class SafeDivisionSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {
    
    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        Double dividend = (Double) paramsList.get(0);
        Double divisor = (Double) paramsList.get(1);
        
        // 检查除零错误
        if (Math.abs(divisor) < 1e-10) {
            SpellExceptions.DIVISION_BY_ZERO(this).throwIt(caster);
            return ExecutionResult.ERRORED();
        }
        
        Double result = dividend / divisor;
        return ExecutionResult.RETURNED(this, List.of(result), List.of(SpellValueType.NUMBER));
    }
}
```

## 🧪 测试和调试

### 单元测试示例
```java
public class SpellLogicTest {
    
    @Test
    public void testAverageSpell() {
        // 准备测试数据
        AverageSpell spell = new AverageSpell();
        Player mockPlayer = Mockito.mock(Player.class);
        SpellSequence sequence = new SpellSequence();
        List<Object> params = Arrays.asList(10.0, 20.0);
        SpellEntity mockEntity = Mockito.mock(SpellEntity.class);
        
        // 执行测试
        ExecutionResult result = spell.run(mockPlayer, sequence, params, mockEntity);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.returnValue);
        assertEquals(1, result.returnValue.size());
        assertEquals(15.0, result.returnValue.get(0));
    }
}
```

### 调试技巧
```java
public class DebugSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {
    
    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 详细日志输出
        System.out.println("=== Debug Spell Execution ===");
        System.out.println("Caster: " + caster.getName().getString());
        System.out.println("Parameters: " + paramsList);
        System.out.println("Spell Position: " + spellEntity.position());
        System.out.println("============================");
        
        return ExecutionResult.SUCCESS(this);
    }
}
```

## 📋 最佳实践

### 代码规范
```java
// ✅ 好的做法
public class WellNamedSpell extends SpellItemLogic {
    // 清晰的变量命名
    private static final double BASE_COST_MULTIPLIER = 0.1;
    
    @Override
    public ExecutionResult run(...) {
        // 适当的注释
        double calculatedResult = performComplexCalculation(params);
        
        // 错误检查
        if (calculatedResult < 0) {
            return handleInvalidResult();
        }
        
        return ExecutionResult.RETURNED(this, List.of(calculatedResult), 
                                       List.of(SpellValueType.NUMBER));
    }
}

// ❌ 避免的做法
public class badspellname extends SpellItemLogic {  // 命名不规范
    @Override
    public ExecutionResult run(...) {
        double r = calc(a, b);  // 变量名不清晰
        return good(r);         // 方法名不明确
    }
}
```

### 性能优化
```java
public class OptimizedSpell extends SpellItemLogic {
    // 缓存计算结果
    private static final Map<String, Double> calculationCache = new ConcurrentHashMap<>();
    
    @Override
    public ExecutionResult run(...) {
        // 重用对象避免频繁创建
        Vec3 reusableVector = new Vec3(0, 0, 0);
        
        // 批量处理减少系统调用
        List<Vec3> positions = calculateMultiplePositions(params);
        for (Vec3 pos : positions) {
            // 处理位置
        }
        
        return ExecutionResult.SUCCESS(this);
    }
}
```

---
*掌握了法术开发后，建议学习 [插件开发](plugin-development.md) 扩展魔杖功能。*