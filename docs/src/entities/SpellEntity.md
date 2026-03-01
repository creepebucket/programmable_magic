# SpellEntity 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/entities/SpellEntity.java`  
**包名**: `org.creepebucket.programmable_magic.entities`  
**继承关系**: `Entity` → `SpellEntity`  
**实体类型**: 功能性实体（不渲染，不碰撞）

## 🎯 类设计目的

`SpellEntity` 是法术执行的**运行时环境载体**，为法术序列提供独立的执行上下文。它将法术逻辑与Minecraft的游戏循环紧密结合，实现了法术的逐帧执行和状态管理。

## 🏗️ 核心数据结构

### 执行状态管理
```java
public class SpellEntity extends Entity {
    // 当前执行状态
    public SpellItemLogic currentSpell;     // 当前正在执行的法术
    public SpellSequence spellSequence;     // 完整的法术序列
    
    // 调试相关状态
    public boolean debugMode = false;       // 调试模式开关
    public boolean doStep = false;          // 单步执行标志
    public boolean doRun = false;           // 连续执行标志
    
    // 执行控制参数
    public int delayTicks = 0;              // 执行延迟计数器
    public boolean doStop = false;          // 停止执行标志
}
```

### 魔力和插件系统
```java
// 执行环境数据
private final CompoundTag spellData;        // 法术自定义数据
private ModUtils.Mana mana;                 // 当前魔力状态
private List<WandPluginLogic> plugins;      // 生效的插件列表

// 玩家关联
private final UUID casterUUID;              // 施法者UUID
private Player cachedCaster;                // 施法者缓存引用
```

## 🔧 核心方法详解

### 构造函数和初始化
```java
public SpellEntity(EntityType<? extends SpellEntity> entityType, Level level) {
    super(entityType, level);
    this.noPhysics = true;        // 无视物理碰撞
    this.noCulling = true;        // 不进行视锥剔除
}

// 主要构造函数
public SpellEntity(Level level, Player caster, SpellSequence sequence, 
                  CompoundTag spellData, ModUtils.Mana initialMana,
                  List<WandPluginLogic> plugins, boolean debugMode) {
    this(ModEntityTypes.SPELL_ENTITY.get(), level);
    
    this.casterUUID = caster.getUUID();
    this.spellSequence = sequence;
    this.currentSpell = sequence.head;  // 从序列头部开始执行
    this.spellData = spellData.copy();  // 深拷贝数据
    this.mana = initialMana.copy();     // 深拷贝魔力
    this.plugins = new ArrayList<>(plugins); // 复制插件列表
    this.debugMode = debugMode;
    
    // 初始化位置（在玩家脚下）
    this.setPos(caster.getX(), caster.getY(), caster.getZ());
}
```

### 核心执行循环 - `tick()` 方法
```java
@Override
public void tick() {
    // 调试模式下的执行控制
    if (debugMode) {
        handleDebugControl();
        if (!shouldContinueExecution()) {
            return; // 暂停执行等待调试指令
        }
    }
    
    // 执行延迟处理
    if (delayTicks > 0) {
        delayTicks--;
        return;
    }
    
    // 获取施法者引用
    Player caster = getCaster();
    if (caster == null || !caster.isAlive()) {
        discard(); // 施法者不存在或死亡时清理实体
        return;
    }
    
    // 执行当前法术
    executeCurrentSpell(caster);
    
    // 处理执行结果
    handleExecutionResult();
}
```

### 调试控制逻辑
```java
private void handleDebugControl() {
    if (doStep) {
        // 单步执行模式
        doStep = false;  // 消费单步标志
        doRun = false;   // 确保不会连续执行
    } else if (doRun) {
        // 连续执行模式 - 继续执行直到遇到断点
        if (shouldPauseAtBreakpoint()) {
            doRun = false;  // 在断点处暂停
        }
    } else {
        // 暂停模式 - 等待用户指令
        return;
    }
}

private boolean shouldContinueExecution() {
    return doStep || doRun || !debugMode;
}
```

### 法术执行核心逻辑
```java
private void executeCurrentSpell(Player caster) {
    try {
        // 调用法术的安全执行方法
        ExecutionResult result = currentSpell.runWithCheck(
            caster, spellSequence, this
        );
        
        // 更新执行状态
        delayTicks = result.delayTicks;
        doStop = result.doStop;
        
        // 处理返回值
        if (result.returnValue != null && !result.returnValue.isEmpty()) {
            pushReturnValueToSequence(result);
        }
        
        // 更新下一个要执行的法术
        currentSpell = result.nextSpell;
        
    } catch (Exception e) {
        // 异常处理
        handleSpellException(e, caster);
        doStop = true;
        currentSpell = null;
    }
}
```

### 返回值处理机制
```java
private void pushReturnValueToSequence(ExecutionResult result) {
    // 将返回值转换为法术节点插入到序列中
    for (int i = 0; i < result.returnValue.size(); i++) {
        Object value = result.returnValue.get(i);
        SpellValueType valueType = result.returnTypes.get(i);
        
        // 创建对应的字面量法术
        SpellItemLogic literalSpell = createLiteralSpell(value, valueType);
        if (literalSpell != null) {
            // 插入到当前法术之后
            insertAfterCurrent(literalSpell);
        }
    }
}

private SpellItemLogic createLiteralSpell(Object value, SpellValueType type) {
    return switch (type) {
        case NUMBER -> new NumberDigitSpell(((Double) value).floatValue());
        case STRING -> new StringLiteralSpell((String) value);
        case BOOLEAN -> new BooleanLiteralSpell((Boolean) value);
        case VECTOR3 -> new VectorLiteralSpell((Vec3) value);
        case ENTITY -> new EntityReferenceSpell((Entity) value);
        default -> null;
    };
}
```

## 🔄 生命周期管理

### 创建和启动
```java
// 标准创建流程
public static SpellEntity createAndLaunch(Level level, Player caster, 
                                        SpellSequence sequence,
                                        CompoundTag spellData, ModUtils.Mana mana,
                                        List<WandPluginLogic> plugins, 
                                        boolean debugMode) {
    SpellEntity entity = new SpellEntity(level, caster, sequence, spellData, 
                                       mana, plugins, debugMode);
    
    // 添加到世界
    level.addFreshEntity(entity);
    
    // 通知插件
    for (WandPluginLogic plugin : plugins) {
        plugin.onSpellEntityCreated(entity);
    }
    
    return entity;
}
```

### 终止和清理
```java
@Override
public void discard() {
    // 通知插件法术实体即将销毁
    for (WandPluginLogic plugin : plugins) {
        plugin.onSpellEntityDestroyed(this);
    }
    
    // 清理资源
    cleanupResources();
    
    // 调用父类方法
    super.discard();
}

private void cleanupResources() {
    // 清理引用避免内存泄漏
    currentSpell = null;
    spellSequence = null;
    spellData.clear();
    plugins.clear();
    cachedCaster = null;
}
```

## 🎯 调试系统集成

### 调试数据收集
```java
public class DebugInfo {
    public SpellItemLogic currentSpell;
    public int executionStep;
    public ModUtils.Mana currentMana;
    public List<Object> lastParameters;
    public ExecutionResult lastResult;
    public BlockPos currentPosition;
}

public DebugInfo getDebugInfo() {
    return new DebugInfo(
        currentSpell,
        tickCount,
        mana.copy(),
        lastExecutedParams,
        lastExecutionResult,
        blockPosition()
    );
}
```

### 断点和监视点
```java
public class BreakpointSystem {
    private Set<SpellItemLogic> breakpoints = new HashSet<>();
    private Map<SpellItemLogic, Condition> conditionalBreakpoints = new HashMap<>();
    
    public void addBreakpoint(SpellItemLogic spell) {
        breakpoints.add(spell);
    }
    
    public boolean shouldBreakAt(SpellItemLogic spell) {
        if (!breakpoints.contains(spell)) return false;
        
        Condition condition = conditionalBreakpoints.get(spell);
        return condition == null || condition.evaluate(this);
    }
}
```

## 🔗 网络同步机制

### 状态同步
```java
public class SpellEntityNetworking {
    // 同步重要状态到客户端
    public void syncToClients() {
        if (!level.isClientSide) {
            // 发送同步数据包
            SpellEntitySyncPacket packet = new SpellEntitySyncPacket(
                getId(),
                currentSpell.name,
                blockPosition(),
                debugMode
            );
            Networking.sendToTracking(this, packet);
        }
    }
    
    // 接收客户端调试指令
    public void handleDebugCommand(DebugCommand command) {
        switch (command.getType()) {
            case STEP -> doStep = true;
            case RUN -> doRun = true;
            case PAUSE -> doRun = false;
            case STOP -> doStop = true;
        }
    }
}
```

## ⚡ 性能优化策略

### 对象池管理
```java
public class SpellEntityManager {
    private static final ObjectPool<SpellEntity> entityPool = 
        new ObjectPool<>(() -> new SpellEntity(null, null));
    
    public static SpellEntity acquire(Level level, Player caster) {
        SpellEntity entity = entityPool.acquire();
        entity.setLevel(level);
        entity.casterUUID = caster.getUUID();
        return entity;
    }
    
    public static void release(SpellEntity entity) {
        entity.cleanupResources();
        entityPool.release(entity);
    }
}
```

### 批量执行优化
```java
public class BatchExecutor {
    public void executeBatch(List<SpellEntity> entities) {
        // 批量处理减少系统调用
        for (SpellEntity entity : entities) {
            if (entity.shouldExecute()) {
                entity.executeCurrentSpell(entity.getCaster());
            }
        }
        
        // 批量同步
        syncAll(entities);
    }
}
```

## 🛡️ 错误处理和容错

### 异常恢复机制
```java
private void handleSpellException(Exception e, Player caster) {
    // 记录错误日志
    LOGGER.error("法术执行异常: {}", currentSpell.name, e);
    
    // 通知玩家
    caster.sendSystemMessage(
        Component.literal("法术执行出错: " + e.getMessage())
            .withStyle(ChatFormatting.RED)
    );
    
    // 尝试恢复执行
    if (canRecoverFromError(e)) {
        recoverFromError();
    } else {
        // 无法恢复则终止执行
        doStop = true;
    }
}
```

### 状态一致性保证
```java
public class StateConsistencyChecker {
    public void validateState() {
        // 检查基本约束
        if (currentSpell == null && !doStop) {
            throw new IllegalStateException("currentSpell为null但未标记停止");
        }
        
        if (spellSequence == null) {
            throw new IllegalStateException("spellSequence不能为空");
        }
        
        // 检查链表完整性
        validateSequenceIntegrity();
        
        // 检查魔力状态
        validateManaState();
    }
}
```

## 🔧 实际使用示例

### 基本法术执行
```java
// 创建并执行简单法术序列
public void castSimpleSpell(Player player) {
    SpellSequence sequence = new SpellSequence();
    sequence.pushRight(new DebugPrintSpell());
    
    SpellEntity.createAndLaunch(
        player.level(),
        player,
        sequence,
        new CompoundTag(),
        new ModUtils.Mana(10, 10, 10, 10),
        Collections.emptyList(),
        false
    );
}
```

### 复杂序列执行
```java
// 执行带参数和返回值的复杂序列
public void castComplexSpell(Player player, Vec3 targetPos) {
    SpellSequence sequence = new SpellSequence();
    
    // 构建: position_of(player) + (0, 1, 0) → teleport(entity, vector)
    sequence.pushRight(new PlayerPositionSpell());
    sequence.pushRight(new VectorLiteralSpell(new Vec3(0, 1, 0)));
    sequence.pushRight(new AdditionSpell());
    sequence.pushRight(new SelfReferenceSpell());
    sequence.pushRight(new TeleportSpell());
    
    SpellEntity.createAndLaunch(
        player.level(),
        player,
        sequence,
        createSpellData(targetPos),
        calculateRequiredMana(sequence),
        getActivePlugins(player),
        isInDebugMode(player)
    );
}
```

### 调试模式使用
```java
// 启用调试模式执行
public void debugSpellSequence(Player player, SpellSequence sequence) {
    SpellEntity entity = SpellEntity.createAndLaunch(
        player.level(),
        player,
        sequence,
        new CompoundTag(),
        new ModUtils.Mana(100, 100, 100, 100),
        getDebugPlugins(),
        true  // 启用调试模式
    );
    
    // 注册调试监听器
    registerDebugListener(entity, player);
}
```

## 📊 监控和统计

### 执行性能监控
```java
public class ExecutionProfiler {
    private long startTime;
    private int spellCount;
    private Map<String, Integer> spellExecutionCount = new HashMap<>();
    
    public void startProfiling() {
        startTime = System.nanoTime();
        spellCount = 0;
    }
    
    public void recordSpellExecution(SpellItemLogic spell) {
        spellCount++;
        spellExecutionCount.merge(spell.name, 1, Integer::sum);
    }
    
    public ExecutionReport generateReport() {
        long duration = System.nanoTime() - startTime;
        return new ExecutionReport(duration, spellCount, spellExecutionCount);
    }
}
```

---
*相关文档链接：*
- [SpellItemLogic 详解](../spells/api/SpellItemLogic.md)
- [SpellSequence 详解](../spells/api/SpellSequence.md)
- [ExecutionResult 详解](../spells/api/ExecutionResult.md)
- [调试系统设计](../../development/debugging-system.md)