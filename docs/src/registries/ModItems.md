# ModItems 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/registries/ModItems.java`  
**包名**: `org.creepebucket.programmable_magic.registries`  
**设计模式**: 注册器模式 + 延迟初始化

## 🎯 类设计目的

`ModItems` 是项目的**物品注册管理中心**，负责所有自定义物品的注册、分组和管理。它提供了统一的接口来访问游戏中所有的物品类型。

## 🏗️ 核心数据结构

### 注册器定义
```java
public class ModItems {
    // 主要物品注册器
    public static final DeferredRegister.Items ITEMS = 
        DeferredRegister.createItems(Programmable_magic.MODID);
    
    // 创造模式物品栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PROGRAMMABLE_MAGIC_TAB =
        ModCreativeTabs.PROGRAMMABLE_MAGIC_TAB;
    
    // 物品分组映射
    public static final Map<String, List<DeferredItem<? extends Item>>> ITEM_GROUPS = 
        new ConcurrentHashMap<>();
}
```

### 核心物品定义
```java
// 基础物品
public static final DeferredItem<Item> WAND = 
    ITEMS.register("wand", () -> new Wand(new Item.Properties()
        .stacksTo(1)
        .rarity(Rarity.RARE)
        .component(ModDataComponents.WAND_DATA, new Wand.WandData())
    ));

// 法术物品模板
public static final DeferredItem<Item> SPELL_TEMPLATE = 
    ITEMS.register("spell_template", () -> new SpellTemplateItem(
        new Item.Properties().stacksTo(16)
    ));

// 调试物品
public static final DeferredItem<Item> DEBUG_TOOL = 
    ITEMS.register("debug_tool", () -> new DebugToolItem(
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    ));

// 材料物品
public static final DeferredItem<Item> MAGIC_CRYSTAL = 
    ITEMS.register("magic_crystal", () -> new Item(
        new Item.Properties().rarity(Rarity.UNCOMMON)
    ));

public static final DeferredItem<Item> ENCHANTED_INGOT = 
    ITEMS.register("enchanted_ingot", () -> new Item(
        new Item.Properties().rarity(Rarity.RARE)
    ));
```

## 🔧 核心方法详解

### 物品注册方法

#### `register()` - 基础物品注册
```java
public static <T extends Item> DeferredItem<T> register(String name, 
                                                      Supplier<T> itemSupplier) {
    DeferredItem<T> registeredItem = ITEMS.register(name, itemSupplier);
    
    // 自动添加到默认物品栏
    addToDefaultGroup(registeredItem);
    
    // 记录注册日志
    LOGGER.info("Registered item: {}", name);
    
    return registeredItem;
}

private static void addToDefaultGroup(DeferredItem<? extends Item> item) {
    // 添加到创造模式物品栏
    if (PROGRAMMABLE_MAGIC_TAB.isBound()) {
        PROGRAMMABLE_MAGIC_TAB.get().addItem(item);
    }
}
```

#### `registerSpell()` - 法术物品专用注册
```java
public static DeferredItem<SpellItem> registerSpell(String spellName,
                                                  Supplier<SpellItemLogic> spellLogic,
                                                  String subCategory) {
    String itemName = "spell_" + spellName;
    
    DeferredItem<SpellItem> spellItem = ITEMS.register(itemName, 
        () -> new SpellItem(spellLogic, subCategory, 
            new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)
        )
    );
    
    // 按子类别分组管理
    ITEM_GROUPS.computeIfAbsent(subCategory, k -> new ArrayList<>())
               .add(spellItem);
    
    // 添加本地化键名
    addLocalizationKey(itemName, "spell." + Programmable_magic.MODID + "." + spellName);
    
    return spellItem;
}
```

### 物品查询方法

#### `getByName()` - 按名称获取物品
```java
public static Item getByName(String itemName) {
    try {
        return ITEMS.getRegistry().get()
            .getValue(new ResourceLocation(Programmable_magic.MODID, itemName));
    } catch (Exception e) {
        LOGGER.warn("Item not found: {}", itemName);
        return Items.AIR; // 返回空气物品作为默认值
    }
}

public static <T extends Item> Optional<T> getByName(String itemName, Class<T> itemType) {
    Item item = getByName(itemName);
    if (itemType.isInstance(item)) {
        return Optional.of(itemType.cast(item));
    }
    return Optional.empty();
}
```

#### `getItemsByGroup()` - 按分组获取物品
```java
public static List<Item> getItemsByGroup(String groupKey) {
    List<DeferredItem<? extends Item>> deferredItems = ITEM_GROUPS.get(groupKey);
    if (deferredItems == null) {
        return Collections.emptyList();
    }
    
    return deferredItems.stream()
        .map(DeferredItem::get)
        .collect(Collectors.toList());
}

// 预定义的分组键
public static final String GROUP_SPELLS = "spells";
public static final String GROUP_TOOLS = "tools";
public static final String GROUP_MATERIALS = "materials";
public static final String GROUP_BLOCKS = "blocks";
```

## 🔄 注册流程详解

### 初始化注册
```java
public static void register(IEventBus eventBus) {
    // 注册所有物品
    ITEMS.register(eventBus);
    
    // 执行后期初始化
    eventBus.addListener(ModItems::onCommonSetup);
    eventBus.addListener(ModItems::onClientSetup);
    
    LOGGER.info("Item registration completed");
}

private static void onCommonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
        // 注册配方
        registerRecipes();
        
        // 初始化物品属性
        initializeItemProperties();
        
        // 建立物品关系
        establishItemRelationships();
    });
}

private static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
        // 注册渲染器
        registerItemRenderers();
        
        // 注册颜色处理器
        registerItemColors();
    });
}
```

### 配方注册
```java
private static void registerRecipes() {
    // 魔杖合成配方
    RecipeBuilder.shaped(ModItems.WAND.get())
        .pattern(" MS")
        .pattern(" SM")
        .pattern("S  ")
        .define('M', ModItems.MAGIC_CRYSTAL.get())
        .define('S', Items.STICK)
        .unlockedBy("has_crystal", 
            InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MAGIC_CRYSTAL.get()))
        .save(recipeOutput, "wand");

    // 魔法水晶配方
    RecipeBuilder.shapeless(ModItems.MAGIC_CRYSTAL.get())
        .requires(Items.QUARTZ)
        .requires(Items.GLOWSTONE_DUST)
        .requires(Items.REDSTONE)
        .unlockedBy("has_quartz", 
            InventoryChangeTrigger.TriggerInstance.hasItems(Items.QUARTZ))
        .save(recipeOutput, "magic_crystal");
}
```

## ⚡ 性能优化特性

### 物品缓存系统
```java
public class ItemCache {
    private static final Map<String, Item> NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, String> REVERSE_CACHE = new ConcurrentHashMap<>();
    private static final LoadingCache<ResourceLocation, Optional<Item>> LOADING_CACHE = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(location -> loadItem(location));
    
    public static Item getCachedItem(String name) {
        return NAME_CACHE.computeIfAbsent(name, 
            n -> getByName(n)
        );
    }
    
    public static String getItemName(Item item) {
        return REVERSE_CACHE.computeIfAbsent(item, 
            i -> ITEMS.getRegistry().get()
                .getKey(i)
                .getPath()
        );
    }
    
    private static Optional<Item> loadItem(ResourceLocation location) {
        return Optional.ofNullable(ITEMS.getRegistry().get().getValue(location));
    }
}
```

### 批量操作优化
```java
public class BatchItemOperations {
    public static void registerItemsBatch(Map<String, Supplier<? extends Item>> items) {
        items.forEach((name, supplier) -> {
            DeferredItem<? extends Item> item = ITEMS.register(name, supplier);
            addToDefaultGroup(item);
        });
        
        LOGGER.info("Batch registered {} items", items.size());
    }
    
    public static Map<String, Item> getItemsBatch(Collection<String> itemNames) {
        return itemNames.parallelStream()
            .collect(Collectors.toConcurrentMap(
                name -> name,
                ModItems::getByName
            ));
    }
}
```

## 🛡️ 错误处理和验证

### 注册验证机制
```java
public class ItemRegistrationValidator {
    public static ValidationResult validateItemRegistration(String name, Item item) {
        List<ValidationError> errors = new ArrayList<>();
        
        // 验证物品名称
        if (name == null || name.isEmpty()) {
            errors.add(new ValidationError("Item name cannot be null or empty"));
        }
        
        if (!name.matches("[a-z0-9_]+")) {
            errors.add(new ValidationError("Item name must contain only lowercase letters, numbers, and underscores"));
        }
        
        // 验证物品属性
        if (item.getMaxStackSize() <= 0) {
            errors.add(new ValidationError("Item max stack size must be positive"));
        }
        
        if (item.getRarity(ItemStack.EMPTY) == null) {
            errors.add(new ValidationError("Item rarity cannot be null"));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

### 物品兼容性检查
```java
public class ItemCompatibilityChecker {
    public static CompatibilityReport checkCompatibility(Item item) {
        List<CompatibilityIssue> issues = new ArrayList<>();
        
        // 检查与现有物品的冲突
        checkNamingConflicts(item, issues);
        
        // 检查配方冲突
        checkRecipeConflicts(item, issues);
        
        // 检查标签冲突
        checkTagConflicts(item, issues);
        
        return new CompatibilityReport(issues.isEmpty(), issues);
    }
    
    private static void checkNamingConflicts(Item item, List<CompatibilityIssue> issues) {
        String itemName = getItemRegistryName(item);
        if (itemName != null && ITEMS.getRegistry().get().containsKey(
                new ResourceLocation(Programmable_magic.MODID, itemName))) {
            issues.add(new CompatibilityIssue(
                "Duplicate item name: " + itemName,
                CompatibilityLevel.WARNING
            ));
        }
    }
}
```

## 🔧 实际使用示例

### 动态物品创建
```java
public class DynamicItemFactory {
    public static Item createDynamicItem(String name, ItemProperties properties) {
        return new Item(properties) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, 
                                      List<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, tooltip, flag);
                tooltip.add(Component.literal("Dynamic Item: " + name));
            }
            
            @Override
            public InteractionResult use(Level level, Player player, 
                                       InteractionHand hand) {
                player.sendSystemMessage(Component.literal("Using dynamic item: " + name));
                return InteractionResult.SUCCESS;
            }
        };
    }
    
    public static DeferredItem<Item> registerDynamicItem(String name) {
        return ModItems.register(name, 
            () -> createDynamicItem(name, new Item.Properties().stacksTo(1))
        );
    }
}
```

### 物品组管理
```java
public class ItemGroupManager {
    private static final Map<String, CustomItemGroup> customGroups = new ConcurrentHashMap<>();
    
    public static class CustomItemGroup {
        private final String groupId;
        private final Component displayName;
        private final List<Supplier<Item>> items;
        private final ItemStack iconItem;
        
        public CustomItemGroup(String groupId, Component displayName, ItemStack iconItem) {
            this.groupId = groupId;
            this.displayName = displayName;
            this.iconItem = iconItem;
            this.items = new ArrayList<>();
        }
        
        public void addItem(Supplier<Item> itemSupplier) {
            items.add(itemSupplier);
        }
        
        public List<Item> getItems() {
            return items.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }
    
    public static CustomItemGroup createItemGroup(String groupId, 
                                                Component displayName, 
                                                ItemStack icon) {
        CustomItemGroup group = new CustomItemGroup(groupId, displayName, icon);
        customGroups.put(groupId, group);
        return group;
    }
}
```

### 物品数据组件管理
```java
public class ItemDataComponentManager {
    public static <T> void registerDataComponent(Item item, 
                                               DataComponentType<T> componentType,
                                               T defaultValue) {
        // 为物品注册数据组件
        item.components().set(componentType, defaultValue);
    }
    
    public static <T> T getDataComponent(ItemStack stack, 
                                       DataComponentType<T> componentType) {
        return stack.get(componentType);
    }
    
    public static <T> void setDataComponent(ItemStack stack, 
                                          DataComponentType<T> componentType,
                                          T value) {
        stack.set(componentType, value);
    }
}
```

---
*相关文档链接：*
- [物品系统架构](../../architecture/item-system.md)
- [注册器模式实践](../../patterns/registry-pattern.md)
- [数据组件系统](../../development/data-components.md)