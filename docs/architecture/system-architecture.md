# 系统架构设计

深入了解项目的整体架构设计和技术选型。

## 🏗️ 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Minecraft Client                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Render    │  │    Input    │  │      Network        │ │
│  │   System    │  │   Handler   │  │    Synchronization  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│           │               │                   │              │
│           ▼               ▼                   ▼              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Programmable Magic Mod                   │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐  │  │
│  │  │  GUI    │ │ Spells  │ │ Plugins │ │  Entities   │  │  │
│  │  │ System  │ │ System  │ │ System  │ │   System    │  │  │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Minecraft Server                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   World     │  │   Player    │  │      Network        │ │
│  │ Management  │  │ Management  │  │    Synchronization  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│           │               │                   │              │
│           ▼               ▼                   ▼              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Programmable Magic Mod                   │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐  │  │
│  │  │  Logic  │ │ Spells  │ │ Plugins │ │  Entities   │  │  │
│  │  │ Engine  │ │ System  │ │ System  │ │   System    │  │  │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 📦 模块划分详解

### 1. 注册系统模块 (registries/)

#### 职责范围
- 管理所有游戏对象的注册
- 维护对象间的依赖关系
- 提供统一的访问接口

#### 核心组件
```java
// 物品注册器
public class ModItems {
    public static final DeferredRegister.Items ITEMS = 
        DeferredRegister.createItems(MODID);
    
    public static final Supplier<Item> WAND = 
        ITEMS.register("wand", () -> new Wand(...));
}

// 法术注册器
public class SpellRegistry {
    private static final Map<Identifier, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS;
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS;
    
    // 按子类别组织法术
    public static Map<String, List<Supplier<Item>>> SPELLS_BY_SUBCATEGORY;
}
```

#### 设计优势
- ✅ 延迟注册避免初始化顺序问题
- ✅ 依赖注入简化对象创建
- ✅ 统一管理便于维护

### 2. 法术执行引擎 (spells/)

#### 核心执行流程
```
玩家释放 → 法术编译 → 类型检查 → 实体创建 → 逐帧执行 → 结果反馈
    ↓         ↓         ↓         ↓         ↓         ↓
 [Wand.use()] [SpellCompiler] [TypeChecker] [SpellEntity] [tick()] [Effects]
```

#### 关键数据结构
```java
// 法术序列 - 双向链表实现
public class SpellSequence {
    public SpellItemLogic head;  // 链表头部
    public SpellItemLogic tail;  // 链表尾部
    
    public void pushLeft(SpellItemLogic spell) { /* 前插 */ }
    public void pushRight(SpellItemLogic spell) { /* 后插 */ }
    public SpellItemLogic popLeft() { /* 前删 */ }
    public SpellItemLogic popRight() { /* 后删 */ }
}

// 执行结果封装
public class ExecutionResult {
    public SpellItemLogic nextSpell;    // 下一个执行的法术
    public int delayTicks;              // 延迟刻数
    public boolean doStop;              // 是否停止执行
    public List<Object> returnValue;    // 返回值
    public List<SpellValueType> returnTypes; // 返回值类型
}
```

### 3. GUI框架系统 (gui/lib/)

#### 组件层次结构
```
Screen (屏幕基类)
├── Root Widget (根控件)
│   ├── Topbar Widgets (顶部栏组件)
│   ├── Main Content (主要内容区)
│   └── Bottom Widgets (底部组件)
│       ├── Widget Tree (控件树)
│       │   ├── ButtonWidget
│       │   ├── TextWidget  
│       │   ├── SlotWidget
│       │   └── Custom Widgets
│       └── Animation System (动画系统)
```

#### 生命周期管理
```java
public abstract class Widget {
    // 初始化阶段
    public void onInitialize() { /* 组件创建后调用 */ }
    
    // 渲染阶段
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染逻辑
    }
    
    // 更新阶段
    public void tick(double deltaTime) { /* 每帧更新 */ }
    
    // 销毁阶段
    public void onDestroy() { /* 组件销毁前调用 */ }
    
    // 尺寸变更
    public void onResize(int width, int height) { /* 屏幕尺寸变化时 */ }
}
```

### 4. 网络通信层 (network/)

#### 通信协议设计
```java
// 自定义数据包基类
public abstract class BasePacket {
    public abstract void handle(IPayloadContext context);
    
    // 编解码器
    public static final StreamCodec<RegistryFriendlyByteBuf, T> STREAM_CODEC = 
        StreamCodec.composite(
            // 字段编码解码定义
        );
}

// 双向通信
Client → Server: 请求执行法术
Server → Client: 同步执行状态
Client → Server: 调试控制命令
Server → Client: 实时数据更新
```

#### 同步机制
```java
// 数据同步包装器
public class SyncedValue<T> {
    private final DataManager manager;
    private final String key;
    
    public T get() { /* 获取值 */ }
    public void set(T value) { /* 设置值并自动同步 */ }
}

// 数据管理器
public class DataManager {
    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> dirtyKeys = new HashSet<>();
    
    public void update(String key, Object value) {
        values.put(key, value);
        dirtyKeys.add(key);
        // 触发同步
    }
}
```

## 🔧 技术架构决策

### 为什么选择NeoForge?

#### 优势分析
- **成熟稳定**：经过长期验证的API
- **社区支持**：丰富的文档和教程
- **兼容性好**：与大多数模组兼容
- **开发工具**：完善的开发环境支持

#### 架构适配
```java
// 利用NeoForge的事件系统
@Mod.EventBusSubscriber(modid = MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        // 游戏事件处理
    }
}

// 数据生成器集成
public class SpellItemModelProvider implements DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 自动生成资源文件
    }
}
```

### 自研GUI框架的原因

#### 现有方案的局限
- **ScreenAPI限制**：不够灵活的布局系统
- **缺乏动画支持**：没有内置的动画框架
- **组件复用困难**：难以构建复杂的UI组件

#### 自研框架优势
```java
// 声明式UI构建
Widget button = new ButtonWidget(
    Coordinate.fromTopLeft(10, 10),
    Coordinate.fromTopLeft(100, 30)
).onClick(() -> {
    // 点击事件处理
}).tooltip(Component.literal("点击我"));

// 链式调用配置
button.mainColor(Color.RED)
      .textColor(Color.WHITE)
      .addAnimation(new FadeIn(0.3), 0);
```

### 实体驱动的法术执行

#### 设计考量
```
为什么不直接在物品上执行?
├── ✅ 独立的生命周期管理
├── ✅ 更好的调试支持
├── ✅ 网络同步简化
├── ✅ 粒子效果自然
└── ✅ 符合Minecraft设计理念
```

#### 实现细节
```java
public class SpellEntity extends Entity {
    // 独立的执行环境
    private SpellSequence spellSequence;
    private SpellItemLogic currentSpell;
    private int delayTicks;
    
    @Override
    public void tick() {
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }
        
        // 执行当前法术
        ExecutionResult result = currentSpell.runWithCheck(...);
        
        // 更新执行状态
        delayTicks = result.delayTicks;
        currentSpell = result.nextSpell;
        
        if (result.doStop) {
            this.discard(); // 清理实体
        }
    }
}
```

## 📊 性能优化策略

### 内存管理
```java
// 对象池模式
public class SpellEntityPool {
    private final Queue<SpellEntity> pool = new ConcurrentLinkedQueue<>();
    
    public SpellEntity acquire(Level level) {
        SpellEntity entity = pool.poll();
        if (entity == null) {
            entity = new SpellEntity(ModEntityTypes.SPELL_ENTITY.get(), level);
        }
        return entity;
    }
    
    public void release(SpellEntity entity) {
        entity.reset(); // 重置状态
        pool.offer(entity);
    }
}
```

### 渲染优化
```java
// 批量渲染
public class RenderBatch {
    private final List<RenderOperation> operations = new ArrayList<>();
    
    public void addOperation(RenderOperation op) {
        operations.add(op);
    }
    
    public void execute(GuiGraphics graphics) {
        // 一次性执行所有渲染操作
        for (RenderOperation op : operations) {
            op.render(graphics);
        }
        operations.clear();
    }
}
```

### 计算优化
```java
// 缓存机制
public class SpellCompiler {
    private final Map<String, CompiledSpell> cache = new ConcurrentHashMap<>();
    
    public CompiledSpell compile(Container spells) {
        String key = generateCacheKey(spells);
        
        return cache.computeIfAbsent(key, k -> {
            // 首次编译
            return doCompile(spells);
        });
    }
}
```

## 🛡️ 错误处理机制

### 分层错误处理
```java
// 法术异常体系
public abstract class SpellException extends RuntimeException {
    public abstract void throwIt(Player player);
}

// 具体异常类型
public class InvalidInputException extends SpellException {
    private final SpellItemLogic spell;
    private final String message;
    
    @Override
    public void throwIt(Player player) {
        player.sendSystemMessage(
            Component.literal("法术参数错误: " + message)
                .withStyle(ChatFormatting.RED)
        );
    }
}

// 编译时检查
public class SpellCompiler {
    public List<SpellException> errors = new ArrayList<>();
    
    public SpellSequence compile(Container spells) {
        // 类型检查
        checkTypes(spells);
        
        // 语法检查
        checkSyntax(spells);
        
        // 语义检查
        checkSemantics(spells);
        
        if (!errors.isEmpty()) {
            return null; // 编译失败
        }
        
        return buildSequence(spells);
    }
}
```

## 🔮 未来架构演进

### 可扩展性设计
```java
// 插件化架构
public interface SpellModule {
    void initialize(ModContainer container);
    void registerSpells(SpellRegistry registry);
    void registerPlugins(PluginRegistry registry);
}

// 动态加载
public class ModuleManager {
    public void loadModule(String moduleId) {
        // 动态加载模组扩展
    }
}
```

### 微服务化趋势
```
当前架构: 单体应用
未来可能: 
├── Core Service (核心服务)
├── Spell Service (法术服务)  
├── GUI Service (界面服务)
└── Network Service (网络服务)
```

---
*理解了架构设计后，建议实践 [法术开发](../development/spell-development.md) 来加深理解。*