# 数据系统综合文档

## 📋 概述

本文档整合了项目中的数据组件、配方系统和标签管理等相关的小型系统，提供统一的技术参考。

## 🧩 数据组件系统

### ModDataComponents 自定义数据组件

**文件**: `ModDataComponents.java`

#### 核心数据组件定义
```java
public class ModDataComponents {
    // 注册器定义
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = 
        DeferredRegister.createDataComponents(Programmable_magic.MODID);
    
    // 魔杖数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Wand.WandData>> WAND_DATA =
        DATA_COMPONENTS.registerComponentType("wand_data",
            builder -> builder.persistent(Wand.WandData.CODEC)
                             .networkSynchronized(Wand.WandData.STREAM_CODEC)
        );
    
    // 法术数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpellData>> SPELL_DATA =
        DATA_COMPONENTS.registerComponentType("spell_data",
            builder -> builder.persistent(SpellData.CODEC)
                             .networkSynchronized(SpellData.STREAM_CODEC)
        );
    
    // 插件数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<PluginData>>> PLUGIN_DATA =
        DATA_COMPONENTS.registerComponentType("plugin_data",
            builder -> builder.persistent(PluginData.LIST_CODEC)
                             .networkSynchronized(PluginData.LIST_STREAM_CODEC)
        );
    
    // 魔力网络节点数据
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MananetNodeData>> MANANET_NODE_DATA =
        DATA_COMPONENTS.registerComponentType("mananet_node_data",
            builder -> builder.persistent(MananetNodeData.CODEC)
                             .networkSynchronized(MananetNodeData.STREAM_CODEC)
        );
}
```

#### 数据组件使用示例
```java
public class DataComponentUsage {
    // 设置魔杖数据
    public static void setWandData(ItemStack wandStack, ModUtils.Mana currentMana, 
                                 SpellSequence spellSequence) {
        Wand.WandData wandData = new Wand.WandData(
            currentMana,
            spellSequence,
            System.currentTimeMillis()
        );
        wandStack.set(ModDataComponents.WAND_DATA.get(), wandData);
    }
    
    // 获取魔杖数据
    public static Wand.WandData getWandData(ItemStack wandStack) {
        return wandStack.get(ModDataComponents.WAND_DATA.get());
    }
    
    // 检查数据组件是否存在
    public static boolean hasWandData(ItemStack wandStack) {
        return wandStack.has(ModDataComponents.WAND_DATA.get());
    }
    
    // 移除数据组件
    public static void removeWandData(ItemStack wandStack) {
        wandStack.remove(ModDataComponents.WAND_DATA.get());
    }
}
```

### WandData 魔杖数据结构
```java
public static class WandData {
    private final ModUtils.Mana currentMana;
    private final SpellSequence spellSequence;
    private final long lastUsedTime;
    
    // Codec用于持久化存储
    public static final Codec<WandData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ModUtils.Mana.CODEC.fieldOf("current_mana").forGetter(WandData::currentMana),
            SpellSequence.CODEC.fieldOf("spell_sequence").forGetter(WandData::spellSequence),
            Codec.LONG.fieldOf("last_used_time").forGetter(WandData::lastUsedTime)
        ).apply(instance, WandData::new)
    );
    
    // StreamCodec用于网络同步
    public static final StreamCodec<RegistryFriendlyByteBuf, WandData> STREAM_CODEC = 
        StreamCodec.composite(
            ModUtils.Mana.STREAM_CODEC, WandData::currentMana,
            SpellSequence.STREAM_CODEC, WandData::spellSequence,
            ByteBufCodecs.VAR_LONG, WandData::lastUsedTime,
            WandData::new
        );
}
```

## 🍳 配方系统

### ModRecipeSerializers 配方序列化器

**文件**: `ModRecipeSerializers.java`

#### 配方类型定义
```java
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Programmable_magic.MODID);
    
    // 魔杖合成配方
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WandCraftingRecipe>> WAND_CRAFTING =
        RECIPE_SERIALIZERS.register("wand_crafting", WandCraftingRecipe.Serializer::new);
    
    // 法术注入配方
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpellInfusionRecipe>> SPELL_INFUSION =
        RECIPE_SERIALIZERS.register("spell_infusion", SpellInfusionRecipe.Serializer::new);
    
    // 魔力水晶配方
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MagicCrystalRecipe>> MAGIC_CRYSTAL =
        RECIPE_SERIALIZERS.register("magic_crystal", MagicCrystalRecipe.Serializer::new);
}
```

#### 自定义配方实现
```java
// 魔杖合成配方
public class WandCraftingRecipe implements CraftingRecipe {
    private final String group;
    private final CookingBookCategory category;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final boolean showNotification;
    
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        // 检查配方是否匹配
        return RecipeMatcher.findMatches(container, ingredients) != null;
    }
    
    @Override
    public ItemStack assemble(CraftingContainer container, HolderLookup.Provider registries) {
        // 组装结果物品
        ItemStack resultCopy = result.copy();
        
        // 添加魔杖数据
        Wand.WandData defaultData = new Wand.WandData(
            new ModUtils.Mana(50, 50, 50, 50), // 初始魔力
            new SpellSequence(), // 空法术序列
            System.currentTimeMillis()
        );
        resultCopy.set(ModDataComponents.WAND_DATA.get(), defaultData);
        
        return resultCopy;
    }
    
    public static class Serializer implements RecipeSerializer<WandCraftingRecipe> {
        @Override
        public MapCodec<WandCraftingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(WandCraftingRecipe::getGroup),
                    CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC)
                        .forGetter(WandCraftingRecipe::category),
                    ItemStack.CODEC.fieldOf("result").forGetter(WandCraftingRecipe::getResult),
                    Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .flatXmap(
                            ing -> {
                                if (ing.size() < 1 || ing.size() > 9) {
                                    return DataResult.error(() -> "Ingredients must be 1 to 9");
                                }
                                return DataResult.success(NonNullList.copyOf(ing));
                            },
                            DataResult::success
                        ).forGetter(WandCraftingRecipe::getIngredients),
                    Codec.BOOL.optionalFieldOf("show_notification", true)
                        .forGetter(WandCraftingRecipe::showNotification)
                ).apply(instance, WandCraftingRecipe::new)
            );
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WandCraftingRecipe> streamCodec() {
            return StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeUtf(recipe.group);
                    buf.writeEnum(recipe.category);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                    buf.writeCollection(recipe.ingredients, Ingredient.CONTENTS_STREAM_CODEC);
                    buf.writeBoolean(recipe.showNotification);
                },
                buf -> new WandCraftingRecipe(
                    buf.readUtf(),
                    buf.readEnum(CookingBookCategory.class),
                    ItemStack.STREAM_CODEC.decode(buf),
                    NonNullList.copyOf(buf.readList(Ingredient.CONTENTS_STREAM_CODEC)),
                    buf.readBoolean()
                )
            );
        }
    }
}
```

## 🏷️ 标签系统

### ModTagKeys 自定义标签

**文件**: `ModTagKeys.java`

#### 标签定义
```java
public class ModTagKeys {
    // 物品标签
    public static final TagKey<Item> WANDS = 
        createItemTag("wands");
    
    public static final TagKey<Item> SPELLS = 
        createItemTag("spells");
    
    public static final TagKey<Item> MAGIC_MATERIALS = 
        createItemTag("magic_materials");
    
    public static final TagKey<Item> MANA_CRYSTALS = 
        createItemTag("mana_crystals");
    
    // 方块标签
    public static final TagKey<Block> MANANET_NODES = 
        createBlockTag("mananet_nodes");
    
    public static final TagKey<Block> MAGIC_ORES = 
        createBlockTag("magic_ores");
    
    // 实体标签
    public static final TagKey<EntityType<?>> SPELL_ENTITIES = 
        createEntityTypeTag("spell_entities");
    
    // 创建标签的辅助方法
    private static TagKey<Item> createItemTag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Programmable_magic.MODID, name));
    }
    
    private static TagKey<Block> createBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(Programmable_magic.MODID, name));
    }
    
    private static TagKey<EntityType<?>> createEntityTypeTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Programmable_magic.MODID, name));
    }
}
```

### 标签提供器
```java
// 物品标签提供器
public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                            CompletableFuture<TagLookup<Block>> blockTags, String modId, 
                            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, modId, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 魔杖标签
        tag(ModTagKeys.WANDS)
            .add(ModItems.WAND.get());
        
        // 法术标签
        tag(ModTagKeys.SPELLS)
            .add(getAllSpellItems().toArray(Item[]::new));
        
        // 魔法材料标签
        tag(ModTagKeys.MAGIC_MATERIALS)
            .add(ModItems.MAGIC_CRYSTAL.get())
            .add(ModItems.ENCHANTED_INGOT.get());
        
        // 魔力水晶标签
        tag(ModTagKeys.MANA_CRYSTALS)
            .add(ModItems.MAGIC_CRYSTAL.get());
    }
    
    private List<Item> getAllSpellItems() {
        return SpellRegistry.getAllSpellItems();
    }
}

// 方块标签提供器
public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                             String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 魔力网络节点标签
        tag(ModTagKeys.MANANET_NODES)
            .add(ModBlocks.MANANET_GENERATOR.get())
            .add(ModBlocks.MANANET_STORAGE.get())
            .add(ModBlocks.MANANET_TRANSMITTER.get());
        
        // 魔法矿石标签
        tag(ModTagKeys.MAGIC_ORES)
            .add(ModBlocks.MAGIC_ORE.get());
    }
}
```

## 📦 数据生成器

### 数据生成系统整合
```java
public class ModDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        
        // 添加各种数据提供器
        generator.addProvider(event.includeServer(), 
            new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(),
            new ModItemTagProvider(packOutput, lookupProvider, 
                CompletableFuture.completedFuture(TagLookup.empty()), 
                Programmable_magic.MODID, existingFileHelper));
        generator.addProvider(event.includeServer(),
            new ModBlockTagProvider(packOutput, lookupProvider, 
                Programmable_magic.MODID, existingFileHelper));
        
        // 客户端数据
        if (event.includeClient()) {
            generator.addProvider(event.includeClient(),
                new ModItemModelProvider(packOutput, existingFileHelper));
            generator.addProvider(event.includeClient(),
                new ModLanguageProvider(packOutput, "en_us"));
            generator.addProvider(event.includeClient(),
                new ModLanguageProvider(packOutput, "zh_cn"));
        }
    }
}
```

### 配方提供器实现
```java
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registards);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 魔杖配方
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WAND.get())
            .pattern(" MC")
            .pattern(" SM")
            .pattern("S  ")
            .define('M', ModTagKeys.MANA_CRYSTALS)
            .define('S', Items.STICK)
            .define('C', Items.COPPER_INGOT)
            .unlockedBy("has_mana_crystal", 
                has(ModTagKeys.MANA_CRYSTALS))
            .save(recipeOutput, "wand");
        
        // 魔法水晶配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MAGIC_CRYSTAL.get(), 2)
            .requires(Items.QUARTZ)
            .requires(Items.GLOWSTONE_DUST)
            .requires(Items.REDSTONE)
            .requires(Items.AMETHYST_SHARD)
            .unlockedBy("has_quartz", has(Items.QUARTZ))
            .save(recipeOutput, "magic_crystal");
        
        // 法术注入配方示例
        SpecialRecipeBuilder.special(ModRecipeSerializers.SPELL_INFUSION.get())
            .save(recipeOutput, "spell_infusion");
    }
}
```

## ⚡ 性能优化

### 数据缓存系统
```java
public class DataCacheManager {
    private static final LoadingCache<ItemStack, Optional<Wand.WandData>> WAND_DATA_CACHE = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(stack -> {
                if (stack.getItem() instanceof Wand && stack.has(ModDataComponents.WAND_DATA.get())) {
                    return Optional.of(stack.get(ModDataComponents.WAND_DATA.get()));
                }
                return Optional.empty();
            });
    
    public static Optional<Wand.WandData> getCachedWandData(ItemStack stack) {
        return WAND_DATA_CACHE.get(stack);
    }
    
    public static void invalidateWandDataCache(ItemStack stack) {
        WAND_DATA_CACHE.invalidate(stack);
    }
    
    public static void clearAllCaches() {
        WAND_DATA_CACHE.invalidateAll();
    }
}
```

### 批量数据操作
```java
public class BatchDataOperations {
    public static void copyWandData(ItemStack source, ItemStack target) {
        if (source.has(ModDataComponents.WAND_DATA.get())) {
            Wand.WandData data = source.get(ModDataComponents.WAND_DATA.get());
            target.set(ModDataComponents.WAND_DATA.get(), data.copy());
        }
    }
    
    public static void mergeSpellSequences(ItemStack wand1, ItemStack wand2, ItemStack result) {
        Optional<Wand.WandData> data1 = DataCacheManager.getCachedWandData(wand1);
        Optional<Wand.WandData> data2 = DataCacheManager.getCachedWandData(wand2);
        
        if (data1.isPresent() && data2.isPresent()) {
            SpellSequence merged = new SpellSequence();
            merged.pushLeft(data1.get().spellSequence().clone());
            merged.pushLeft(data2.get().spellSequence().clone());
            
            Wand.WandData mergedData = new Wand.WandData(
                data1.get().currentMana().add(data2.get().currentMana()),
                merged,
                Math.max(data1.get().lastUsedTime(), data2.get().lastUsedTime())
            );
            
            result.set(ModDataComponents.WAND_DATA.get(), mergedData);
        }
    }
}
```

## 🔧 实际使用示例

### 数据迁移工具
```java
public class DataMigrationUtility {
    public static void migrateOldData(ItemStack oldStack, ItemStack newStack) {
        // 迁移魔杖数据
        if (oldStack.has(ModDataComponents.WAND_DATA.get())) {
            Wand.WandData oldData = oldStack.get(ModDataComponents.WAND_DATA.get());
            Wand.WandData newData = new Wand.WandData(
                oldData.currentMana(),
                oldData.spellSequence(),
                oldData.lastUsedTime()
            );
            newStack.set(ModDataComponents.WAND_DATA.get(), newData);
        }
        
        // 迁移插件数据
        if (oldStack.has(ModDataComponents.PLUGIN_DATA.get())) {
            List<PluginData> oldPlugins = oldStack.get(ModDataComponents.PLUGIN_DATA.get());
            newStack.set(ModDataComponents.PLUGIN_DATA.get(), new ArrayList<>(oldPlugins));
        }
    }
    
    public static boolean validateDataIntegrity(ItemStack stack) {
        if (stack.getItem() instanceof Wand) {
            Wand.WandData data = stack.get(ModDataComponents.WAND_DATA.get());
            if (data == null) {
                return false;
            }
            
            // 验证魔力值合理性
            if (data.currentMana().anyLessThan(new ModUtils.Mana(0, 0, 0, 0))) {
                return false;
            }
            
            // 验证法术序列完整性
            if (!validateSpellSequence(data.spellSequence())) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean validateSpellSequence(SpellSequence sequence) {
        // 验证法术序列的完整性
        SpellItemLogic current = sequence.head;
        while (current != null) {
            if (current.name == null || current.name.isEmpty()) {
                return false;
            }
            current = current.next;
        }
        return true;
    }
}
```

### 调试工具
```java
public class DataDebugTools {
    public static void printItemData(ItemStack stack) {
        System.out.println("=== Item Data Debug ===");
        System.out.println("Item: " + BuiltInRegistries.ITEM.getKey(stack.getItem()));
        
        if (stack.has(ModDataComponents.WAND_DATA.get())) {
            Wand.WandData data = stack.get(ModDataComponents.WAND_DATA.get());
            System.out.println("Wand Data:");
            System.out.println("  Current Mana: " + data.currentMana());
            System.out.println("  Spell Count: " + data.spellSequence().size());
            System.out.println("  Last Used: " + new Date(data.lastUsedTime()));
        }
        
        if (stack.has(ModDataComponents.SPELL_DATA.get())) {
            SpellData spellData = stack.get(ModDataComponents.SPELL_DATA.get());
            System.out.println("Spell Data: " + spellData);
        }
        
        if (stack.has(ModDataComponents.PLUGIN_DATA.get())) {
            List<PluginData> plugins = stack.get(ModDataComponents.PLUGIN_DATA.get());
            System.out.println("Plugin Count: " + plugins.size());
            for (int i = 0; i < plugins.size(); i++) {
                System.out.println("  Plugin " + i + ": " + plugins.get(i));
            }
        }
        
        System.out.println("NBT Size: " + (stack.hasTag() ? stack.getTag().toString().length() : 0) + " chars");
        System.out.println("=======================");
    }
}
```

---
*相关文档链接：*
- [物品系统架构](../../architecture/item-system.md)
- [配方系统设计](../../development/recipe-system.md)
- [数据组件最佳实践](../../development/data-components-best-practices.md)