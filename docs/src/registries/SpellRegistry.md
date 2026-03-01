# SpellRegistry 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/registries/SpellRegistry.java`  
**包名**: `org.creepebucket.programmable_magic.registries`  
**设计模式**: 注册器模式 + 单例模式

## 🎯 类设计目的

`SpellRegistry` 是法术系统的**中央注册管理器**，负责所有法术逻辑的注册、管理和分发。它提供了一个统一的接口来访问和操作游戏中所有的法术类型。

## 🏗️ 核心数据结构

### 注册表结构
```java
public class SpellRegistry {
    // 法术逻辑供应器映射
    private static final Map<Identifier, Supplier<SpellItemLogic>> LOGIC_SUPPLIERS = 
        new ConcurrentHashMap<>();
    
    // 物品到法术逻辑的映射
    private static final Map<Supplier<Item>, Supplier<SpellItemLogic>> REGISTERED_SPELLS = 
        new ConcurrentHashMap<>();
    
    // 按子类别组织的法术列表
    public static Map<String, List<Supplier<Item>>> SPELLS_BY_SUBCATEGORY = 
        new ConcurrentHashMap<>();
    
    // 法术名称到物品的反向映射
    private static final Map<String, Supplier<Item>> NAME_TO_ITEM = 
        new ConcurrentHashMap<>();
}
```

### 法术分类系统
```java
// 支持的法术子类别
public static final String SUBCATEGORY_BASE = "spell." + MODID + ".subcategory.base";
public static final String SUBCATEGORY_OPERATIONS = "spell." + MODID + ".subcategory.operations";
public static final String SUBCATEGORY_CONTROL = "spell." + MODID + ".subcategory.control";
public static final String SUBCATEGORY_ADJUST = "spell." + MODID + ".subcategory.adjust";
public static final String SUBCATEGORY_VISUAL = "spell." + MODID + ".subcategory.visual";
public static final String SUBCATEGORY_ENTITY = "spell." + MODID + ".subcategory.entity";
```

## 🔧 核心方法详解

### 法术注册方法

#### `registerSpell()` - 基础法术注册
```java
public static <T extends SpellItemLogic> void registerSpell(Supplier<T> spellSupplier) {
    T spellInstance = spellSupplier.get();
    Identifier spellId = new Identifier(MODID, spellInstance.name);
    
    // 验证法术名称唯一性
    if (LOGIC_SUPPLIERS.containsKey(spellId)) {
        throw new IllegalArgumentException("Duplicate spell registration: " + spellId);
    }
    
    // 注册法术逻辑供应器
    LOGIC_SUPPLIERS.put(spellId, (Supplier<SpellItemLogic>) spellSupplier);
    
    // 创建对应的法术物品
    Supplier<Item> spellItem = ModItems.SPELLS.register(
        "spell_" + spellInstance.name,
        () -> new SpellItem(spellSupplier, spellInstance.subCategory)
    );
    
    // 建立双向映射关系
    REGISTERED_SPELLS.put(spellItem, (Supplier<SpellItemLogic>) spellSupplier);
    NAME_TO_ITEM.put(spellInstance.name, spellItem);
    
    // 按子类别分类
    SPELLS_BY_SUBCATEGORY.computeIfAbsent(
        spellInstance.subCategory, 
        k -> new ArrayList<>()
    ).add(spellItem);
    
    LOGGER.info("Registered spell: {} ({})", spellInstance.name, spellInstance.subCategory);
}
```

#### `registerSpellWithItem()` - 自定义物品注册
```java
public static <T extends SpellItemLogic> void registerSpellWithItem(
        Supplier<T> spellSupplier, 
        Supplier<Item> itemSupplier) {
    T spellInstance = spellSupplier.get();
    Identifier spellId = new Identifier(MODID, spellInstance.name);
    
    // 注册逻辑供应器
    LOGIC_SUPPLIERS.put(spellId, (Supplier<SpellItemLogic>) spellSupplier);
    
    // 建立映射关系
    REGISTERED_SPELLS.put(itemSupplier, (Supplier<SpellItemLogic>) spellSupplier);
    NAME_TO_ITEM.put(spellInstance.name, itemSupplier);
    
    // 分类管理
    SPELLS_BY_SUBCATEGORY.computeIfAbsent(
        spellInstance.subCategory,
        k -> new ArrayList<>()
    ).add(itemSupplier);
}
```

### 法术查询方法

#### `getSpellById()` - 通过ID获取法术
```java
public static SpellItemLogic getSpellById(String spellName) {
    Identifier spellId = new Identifier(MODID, spellName);
    Supplier<SpellItemLogic> supplier = LOGIC_SUPPLIERS.get(spellId);
    
    if (supplier != null) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error("Failed to instantiate spell: " + spellName, e);
        }
    }
    
    return null;
}

public static SpellItemLogic getSpellById(Identifier spellId) {
    Supplier<SpellItemLogic> supplier = LOGIC_SUPPLIERS.get(spellId);
    return supplier != null ? supplier.get() : null;
}
```

#### `getItemBySpellName()` - 获取法术对应物品
```java
public static Item getItemBySpellName(String spellName) {
    Supplier<Item> itemSupplier = NAME_TO_ITEM.get(spellName);
    return itemSupplier != null ? itemSupplier.get() : null;
}

public static List<Item> getItemsBySubcategory(String subcategory) {
    List<Supplier<Item>> suppliers = SPELLS_BY_SUBCATEGORY.get(subcategory);
    if (suppliers == null) return Collections.emptyList();
    
    return suppliers.stream()
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}
```

### 批量操作方法

#### `getAllRegisteredSpells()` - 获取所有注册的法术
```java
public static Collection<SpellItemLogic> getAllRegisteredSpells() {
    return LOGIC_SUPPLIERS.values().stream()
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}

public static List<Item> getAllSpellItems() {
    return NAME_TO_ITEM.values().stream()
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}
```

#### `getSpellsByType()` - 按类型筛选法术
```java
public static <T extends SpellItemLogic> List<T> getSpellsByType(Class<T> spellType) {
    return getAllRegisteredSpells().stream()
        .filter(spellType::isInstance)
        .map(spellType::cast)
        .collect(Collectors.toList());
}

// 使用示例
List<SpellItemLogic.ComputeMod> computeSpells = 
    SpellRegistry.getSpellsByType(SpellItemLogic.ComputeMod.class);

List<SpellItemLogic.ControlMod> controlSpells = 
    SpellRegistry.getSpellsByType(SpellItemLogic.ControlMod.class);
```

## 🔄 注册流程详解

### 初始化注册
```java
public static void registerSpells(IEventBus eventBus) {
    // 基础法术注册
    registerSpell(DebugPrintSpell::new);
    registerSpell(ErrorSpell::new);
    
    // 数值运算法术
    registerSpell(NumberDigitSpell::new);
    registerSpell(AdditionSpell::new);
    registerSpell(SubtractionSpell::new);
    registerSpell(MultiplicationSpell::new);
    registerSpell(DivisionSpell::new);
    
    // 向量运算法术
    registerSpell(VectorLiteralSpell::new);
    registerSpell(VectorAdditionSpell::new);
    registerSpell(VectorSubtractionSpell::new);
    
    // 控制流法术
    registerSpell(LParenSpell::new);
    registerSpell(RParenSpell::new);
    registerSpell(IfStartSpell::new);
    registerSpell(IfEndSpell::new);
    
    // 实体操作法术
    registerSpell(SelfReferenceSpell::new);
    registerSpell(PlayerPositionSpell::new);
    registerSpell(TeleportSpell::new);
    
    LOGGER.info("Registered {} spells in total", LOGIC_SUPPLIERS.size());
}
```

### 数据生成器集成
```java
public class SpellDataGenerator extends DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<DataProvider.Future<?>> futures = new ArrayList<>();
        
        // 为每个法术生成模型文件
        for (Map.Entry<String, Supplier<Item>> entry : NAME_TO_ITEM.entrySet()) {
            String spellName = entry.getKey();
            Item spellItem = entry.getValue().get();
            
            // 生成物品模型
            JsonObject modelJson = generateSpellModel(spellName);
            Path modelPath = getModelPath(spellItem);
            futures.add(DataProvider.saveStable(cache, modelJson, modelPath));
            
            // 生成语言文件条目
            JsonObject langJson = generateLanguageEntry(spellName);
            Path langPath = getLanguagePath();
            futures.add(DataProvider.saveStable(cache, langJson, langPath));
        }
        
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
```

## ⚡ 性能优化特性

### 缓存机制
```java
public class SpellRegistryCache {
    private static final Map<String, SpellItemLogic> INSTANTIATED_CACHE = 
        new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<SpellItemLogic>> TYPE_CACHE = 
        new ConcurrentHashMap<>();
    
    public static SpellItemLogic getCachedSpell(String spellName) {
        return INSTANTIATED_CACHE.computeIfAbsent(spellName, 
            name -> getSpellById(name)
        );
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends SpellItemLogic> List<T> getCachedSpellsByType(Class<T> type) {
        return (List<T>) TYPE_CACHE.computeIfAbsent(type, 
            t -> (List<SpellItemLogic>) getSpellsByType(t)
        );
    }
}
```

### 延迟初始化
```java
public class LazySpellRegistry {
    private static volatile boolean initialized = false;
    private static final Object initLock = new Object();
    
    public static void ensureInitialized() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    performInitialization();
                    initialized = true;
                }
            }
        }
    }
    
    private static void performInitialization() {
        // 执行必要的初始化逻辑
        validateRegistryIntegrity();
        buildOptimizationIndexes();
        registerDefaultSpellGroups();
    }
}
```

## 🛡️ 错误处理和验证

### 注册验证机制
```java
public class RegistryValidator {
    public static ValidationResult validateSpellRegistration(SpellItemLogic spell) {
        List<ValidationError> errors = new ArrayList<>();
        
        // 验证基本属性
        if (spell.name == null || spell.name.isEmpty()) {
            errors.add(new ValidationError("Spell name cannot be null or empty"));
        }
        
        if (spell.subCategory == null || spell.subCategory.isEmpty()) {
            errors.add(new ValidationError("Spell subcategory cannot be null or empty"));
        }
        
        // 验证类型定义
        if (spell.inputTypes == null || spell.inputTypes.isEmpty()) {
            errors.add(new ValidationError("Input types cannot be null or empty"));
        }
        
        if (spell.outputTypes == null || spell.outputTypes.isEmpty()) {
            errors.add(new ValidationError("Output types cannot be null or empty"));
        }
        
        // 验证优先级设置
        if (spell.precedence < -100 || spell.precedence > 100) {
            errors.add(new ValidationError("Precedence must be between -100 and 100"));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

### 异常恢复机制
```java
public class RegistryRecovery {
    private static final List<FailedRegistration> failedRegistrations = 
        new CopyOnWriteArrayList<>();
    
    public static void handleRegistrationFailure(String spellName, Exception error) {
        failedRegistrations.add(new FailedRegistration(spellName, error));
        LOGGER.error("Failed to register spell: " + spellName, error);
        
        // 尝试恢复或降级处理
        if (canRecover(error)) {
            attemptRecovery(spellName, error);
        }
    }
    
    private static boolean canRecover(Exception error) {
        return error instanceof IllegalArgumentException || 
               error instanceof NullPointerException;
    }
}
```

## 🔧 实际使用示例

### 动态法术创建
```java
public class DynamicSpellCreator {
    public static SpellItemLogic createDynamicSpell(String name, SpellValueType returnType) {
        return new SpellItemLogic() {
            {
                this.name = name;
                this.subCategory = SpellRegistry.SUBCATEGORY_BASE;
                this.inputTypes = List.of(List.of());
                this.outputTypes = List.of(List.of(returnType));
                this.precedence = 0;
            }
            
            @Override
            public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                                     List<Object> paramsList, SpellEntity spellEntity) {
                // 动态逻辑实现
                return ExecutionResult.RETURNED(this, 
                    List.of(createDefaultValue(returnType)), 
                    List.of(returnType));
            }
            
            @Override
            public boolean canRun(Player caster, SpellSequence spellSequence, 
                                List<Object> paramsList, SpellEntity spellEntity) {
                return true;
            }
            
            @Override
            public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                           List<Object> paramsList, SpellEntity spellEntity) {
                return new ModUtils.Mana(1, 1, 1, 1);
            }
        };
    }
}
```

### 法术组管理
```java
public class SpellGroupManager {
    private static final Map<String, SpellGroup> spellGroups = new ConcurrentHashMap<>();
    
    public static class SpellGroup {
        private final String groupName;
        private final List<Supplier<Item>> spells;
        private final String description;
        
        public SpellGroup(String groupName, String description) {
            this.groupName = groupName;
            this.description = description;
            this.spells = new ArrayList<>();
        }
        
        public void addSpell(String spellName) {
            Item spellItem = SpellRegistry.getItemBySpellName(spellName);
            if (spellItem != null) {
                spells.add(() -> spellItem);
            }
        }
    }
    
    public static SpellGroup createSpellGroup(String groupName, String description) {
        SpellGroup group = new SpellGroup(groupName, description);
        spellGroups.put(groupName, group);
        return group;
    }
}
```

---
*相关文档链接：*
- [法术系统架构](../../architecture/spell-system.md)
- [SpellItemLogic 详解](../spells/api/SpellItemLogic.md)
- [注册器模式实践](../../patterns/registry-pattern.md)