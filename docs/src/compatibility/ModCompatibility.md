# 模组兼容性系统文档

## 📋 概述

本文档详细介绍项目与第三方模组的兼容性系统，包括JEI集成、API适配和兼容性检测机制。

## 🔍 JEI集成系统

### JEI插件实现

**文件**: `src/main/java/org/creepebucket/programmable_magic/compat/jei/ProgrammableMagicJeiPlugin.java`

#### 核心插件结构
```java
@JeiPlugin
public class ProgrammableMagicJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = 
        new ResourceLocation(Programmable_magic.MODID, "jei_plugin");
    
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // 注册自定义JEI分类
        registration.addRecipeCategories(
            new WandCraftingCategory(registration.getJeiHelpers().getGuiHelper()),
            new SpellInfusionCategory(registration.getJeiHelpers().getGuiHelper()),
            new ManaCrystalCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 注册配方处理
        registerWandRecipes(registration);
        registerSpellRecipes(registration);
        registerManaRecipes(registration);
    }
    
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 注册配方催化剂（工作台、特殊方块等）
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.SPELL_INFUSER.get()), 
            SpellInfusionCategory.RECIPE_TYPE
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(Blocks.CRAFTING_TABLE), 
            WandCraftingCategory.RECIPE_TYPE
        );
    }
    
    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        // 注册自定义原料类型
        registerCustomIngredients(registration);
    }
    
    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        // 扩展原版JEI分类
        registration.getCraftingCategory().addCategoryExtension(
            WandCraftingRecipe.class, 
            WandCraftingExtension::new
        );
    }
}
```

### 自定义JEI分类

#### 魔杖合成分类
```java
public class WandCraftingCategory implements IRecipeCategory<WandCraftingRecipe> {
    public static final RecipeType<WandCraftingRecipe> RECIPE_TYPE = 
        RecipeType.create(Programmable_magic.MODID, "wand_crafting", WandCraftingRecipe.class);
    
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    
    public WandCraftingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
            new ResourceLocation(Programmable_magic.MODID, "textures/gui/jei/wand_crafting.png"),
            0, 0, 116, 54
        );
        this.icon = guiHelper.createDrawableIngredient(
            VanillaTypes.ITEM_STACK, 
            new ItemStack(ModItems.WAND.get())
        );
        this.title = Component.translatable("jei.category.programmable_magic.wand_crafting");
    }
    
    @Override
    public RecipeType<WandCraftingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
    
    @Override
    public Component getTitle() {
        return title;
    }
    
    @Override
    public IDrawable getBackground() {
        return background;
    }
    
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WandCraftingRecipe recipe, 
                         IFocusGroup focuses) {
        // 设置配方布局
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        
        // 输入槽位
        for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i % 3) * 18, 1 + (i / 3) * 18)
                   .addIngredients(ingredients.get(i));
        }
        
        // 输出槽位
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
               .addItemStack(recipe.getResultItem(null));
    }
    
    @Override
    public void draw(WandCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, 
                    GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 绘制额外的UI元素
        drawManaRequirements(guiGraphics, recipe);
        drawCraftingTime(guiGraphics, recipe);
    }
    
    private void drawManaRequirements(GuiGraphics guiGraphics, WandCraftingRecipe recipe) {
        // 绘制魔力需求信息
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, 
            Component.translatable("jei.tooltip.mana_required"), 
            5, 45, 0x808080);
    }
    
    private void drawCraftingTime(GuiGraphics guiGraphics, WandCraftingRecipe recipe) {
        // 绘制制作时间
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font,
            Component.literal("20s"), 
            95, 45, 0x808080);
    }
}
```

#### 法术注入分类
```java
public class SpellInfusionCategory implements IRecipeCategory<SpellInfusionRecipe> {
    public static final RecipeType<SpellInfusionRecipe> RECIPE_TYPE = 
        RecipeType.create(Programmable_magic.MODID, "spell_infusion", SpellInfusionRecipe.class);
    
    // 类似实现...
}
```

## 🧪 特殊配方系统

### BindWandItemPlaceholderRecipe 绑定配方

**文件**: `BindWandItemPlaceholderRecipe.java`

#### 配方实现
```java
public class BindWandItemPlaceholderRecipe implements CraftingRecipe {
    private final String group;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final boolean showNotification;
    
    public BindWandItemPlaceholderRecipe(String group, ItemStack result, 
                                       NonNullList<Ingredient> ingredients, 
                                       boolean showNotification) {
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        this.showNotification = showNotification;
    }
    
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        // 特殊匹配逻辑：检查是否包含魔杖和绑定物品
        return hasWandAndBindableItem(container) && 
               RecipeMatcher.findMatches(container, ingredients) != null;
    }
    
    @Override
    public ItemStack assemble(CraftingContainer container, HolderLookup.Provider registries) {
        // 获取魔杖和绑定物品
        ItemStack wand = getWandFromContainer(container);
        ItemStack bindableItem = getBindableItemFromContainer(container);
        
        if (wand.isEmpty() || bindableItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        // 创建绑定结果
        ItemStack resultCopy = result.copy();
        
        // 复制魔杖数据
        if (wand.has(ModDataComponents.WAND_DATA.get())) {
            Wand.WandData wandData = wand.get(ModDataComponents.WAND_DATA.get());
            resultCopy.set(ModDataComponents.WAND_DATA.get(), wandData.copy());
        }
        
        // 添加绑定信息
        addBindingData(resultCopy, bindableItem);
        
        return resultCopy;
    }
    
    private boolean hasWandAndBindableItem(CraftingContainer container) {
        boolean hasWand = false;
        boolean hasBindable = false;
        
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof Wand) {
                hasWand = true;
            } else if (isBindableItem(stack)) {
                hasBindable = true;
            }
        }
        
        return hasWand && hasBindable;
    }
    
    private ItemStack getWandFromContainer(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof Wand) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    private ItemStack getBindableItemFromContainer(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isBindableItem(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    private boolean isBindableItem(ItemStack stack) {
        // 定义可绑定物品的条件
        return stack.is(ModTagKeys.MAGIC_MATERIALS) || 
               stack.getItem() instanceof SpellItem ||
               stack.is(Items.ENCHANTED_BOOK);
    }
    
    private void addBindingData(ItemStack result, ItemStack bindableItem) {
        // 添加绑定相关信息到结果物品
        CompoundTag bindingTag = new CompoundTag();
        bindingTag.putString("bound_item", BuiltInRegistries.ITEM.getKey(bindableItem.getItem()).toString());
        bindingTag.putInt("binding_strength", calculateBindingStrength(bindableItem));
        
        result.addTagElement("binding_data", bindingTag);
    }
    
    private int calculateBindingStrength(ItemStack bindableItem) {
        // 根据绑定物品计算绑定强度
        if (bindableItem.is(ModTagKeys.MANA_CRYSTALS)) {
            return 100;
        } else if (bindableItem.getItem() instanceof SpellItem) {
            return 75;
        } else if (bindableItem.is(Items.ENCHANTED_BOOK)) {
            return 50;
        }
        return 25;
    }
    
    public static class Serializer implements RecipeSerializer<BindWandItemPlaceholderRecipe> {
        @Override
        public MapCodec<BindWandItemPlaceholderRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .flatXmap(
                            ing -> {
                                if (ing.size() < 1 || ing.size() > 9) {
                                    return DataResult.error(() -> "Ingredients must be 1 to 9");
                                }
                                return DataResult.success(NonNullList.copyOf(ing));
                            },
                            DataResult::success
                        ).forGetter(r -> r.ingredients),
                    Codec.BOOL.optionalFieldOf("show_notification", true)
                        .forGetter(r -> r.showNotification)
                ).apply(instance, BindWandItemPlaceholderRecipe::new)
            );
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BindWandItemPlaceholderRecipe> streamCodec() {
            return StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeUtf(recipe.group);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                    buf.writeCollection(recipe.ingredients, Ingredient.CONTENTS_STREAM_CODEC);
                    buf.writeBoolean(recipe.showNotification);
                },
                buf -> new BindWandItemPlaceholderRecipe(
                    buf.readUtf(),
                    ItemStack.STREAM_CODEC.decode(buf),
                    NonNullList.copyOf(buf.readList(Ingredient.CONTENTS_STREAM_CODEC)),
                    buf.readBoolean()
                )
            );
        }
    }
}
```

## 🔧 兼容性检测系统

### 运行时兼容性检查
```java
public class CompatibilityDetector {
    private static final Map<String, Boolean> modPresenceCache = new ConcurrentHashMap<>();
    
    public static boolean isModLoaded(String modId) {
        return modPresenceCache.computeIfAbsent(modId, ModList.get()::isLoaded);
    }
    
    public static void initializeCompatibility() {
        // 检测重要模组的存在
        detectJEI();
        detectREI();
        detectEMI();
        detectPatchouli();
        detectCurios();
        
        // 根据检测结果配置功能
        configureFeatures();
    }
    
    private static void detectJEI() {
        if (isModLoaded("jei")) {
            LOGGER.info("JEI detected, enabling JEI integration");
            enableJEIFeatures();
        }
    }
    
    private static void detectREI() {
        if (isModLoaded("roughlyenoughitems")) {
            LOGGER.info("REI detected, enabling REI integration");
            enableREIFeatures();
        }
    }
    
    private static void detectEMI() {
        if (isModLoaded("emi")) {
            LOGGER.info("EMI detected, enabling EMI integration");
            enableEMIFeatures();
        }
    }
    
    private static void detectPatchouli() {
        if (isModLoaded("patchouli")) {
            LOGGER.info("Patchouli detected, enabling guide book integration");
            enableGuideBookFeatures();
        }
    }
    
    private static void detectCurios() {
        if (isModLoaded("curios")) {
            LOGGER.info("Curios detected, enabling curios integration");
            enableCuriosFeatures();
        }
    }
}
```

### 功能配置系统
```java
public class FeatureConfigurator {
    public static void configureFeatures() {
        // 根据兼容性检测结果配置功能
        configureJEIIntegration();
        configureInventoryManagement();
        configureRecipeHandlers();
        configureGUIEnhancements();
    }
    
    private static void configureJEIIntegration() {
        if (CompatibilityDetector.isModLoaded("jei")) {
            // 启用JEI特定功能
            enableJEIRecipeLookup();
            enableJEIIngredientHighlighting();
            enableJEICategoryExtensions();
        }
    }
    
    private static void configureInventoryManagement() {
        if (CompatibilityDetector.isModLoaded("inventorysorter") || 
            CompatibilityDetector.isModLoaded("sortchest")) {
            // 启用库存排序兼容性
            enableInventorySortingSupport();
        }
    }
    
    private static void configureRecipeHandlers() {
        // 配置不同的配方处理系统
        if (CompatibilityDetector.isModLoaded("craftingtweaks")) {
            enableCraftingTweaksIntegration();
        }
        
        if (CompatibilityDetector.isModLoaded("fastbench")) {
            enableFastBenchIntegration();
        }
    }
    
    private static void configureGUIEnhancements() {
        if (CompatibilityDetector.isModLoaded("configured")) {
            enableConfiguredIntegration();
        }
        
        if (CompatibilityDetector.isModLoaded("catalogue")) {
            enableCatalogueIntegration();
        }
    }
}
```

## 🎯 API适配层

### 通用API适配器
```java
public class ApiAdapter {
    // JEI API适配
    public static class JeiAdapter {
        public static void addInfoRecipe(ItemStack ingredient, List<Component> description) {
            if (CompatibilityDetector.isModLoaded("jei")) {
                // JEI特定实现
                IJeiRuntime runtime = JeiRuntimeAccessor.getRuntime();
                if (runtime != null) {
                    IIngredientManager ingredientManager = runtime.getIngredientManager();
                    ingredientManager.addIngredientInfo(ingredient, VanillaTypes.ITEM_STACK, description);
                }
            }
        }
    }
    
    // REI API适配
    public static class ReiAdapter {
        public static void addInfoRecipe(ItemStack ingredient, List<Component> description) {
            if (CompatibilityDetector.isModLoaded("roughlyenoughitems")) {
                // REI特定实现
                RoughlyEnoughItemsCore.getMainWindow().ifPresent(window -> {
                    // REI实现逻辑
                });
            }
        }
    }
    
    // EMI API适配
    public static class EmiAdapter {
        public static void addInfoRecipe(ItemStack ingredient, List<Component> description) {
            if (CompatibilityDetector.isModLoaded("emi")) {
                // EMI特定实现
                EmiApi.addIngredientInfo(ingredient, description);
            }
        }
    }
}
```

## 🛡️ 版本兼容性

### 版本检测和适配
```java
public class VersionCompatibility {
    private static final Map<String, SemanticVersion> modVersions = new ConcurrentHashMap<>();
    
    public static SemanticVersion getModVersion(String modId) {
        return modVersions.computeIfAbsent(modId, VersionCompatibility::detectModVersion);
    }
    
    private static SemanticVersion detectModVersion(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isPresent()) {
            String versionString = modContainer.get().getModInfo().getVersion().toString();
            try {
                return SemanticVersion.parse(versionString);
            } catch (VersionParsingException e) {
                LOGGER.warn("Failed to parse version for mod {}: {}", modId, versionString);
            }
        }
        return null;
    }
    
    public static boolean isVersionCompatible(String modId, String requiredVersion) {
        SemanticVersion currentVersion = getModVersion(modId);
        if (currentVersion == null) return false;
        
        try {
            SemanticVersion required = SemanticVersion.parse(requiredVersion);
            return currentVersion.compareTo(required) >= 0;
        } catch (VersionParsingException e) {
            LOGGER.warn("Failed to parse required version: {}", requiredVersion);
            return true; // 如果无法解析，假设兼容
        }
    }
    
    public static void checkAllVersions() {
        // 检查所有依赖模组的版本兼容性
        checkJEIVersion();
        checkNeoForgeVersion();
        checkMinecraftVersion();
    }
    
    private static void checkJEIVersion() {
        if (isModLoaded("jei")) {
            if (!isVersionCompatible("jei", "17.0.0")) {
                LOGGER.warn("JEI version may be incompatible. Expected 17.0.0 or higher.");
            }
        }
    }
}
```

## 🔧 调试和诊断工具

### 兼容性诊断器
```java
public class CompatibilityDiagnostic {
    public static class DiagnosticResult {
        private final Map<String, CompatibilityStatus> modStatuses;
        private final List<String> issues;
        private final List<String> recommendations;
        
        public DiagnosticResult() {
            this.modStatuses = new HashMap<>();
            this.issues = new ArrayList<>();
            this.recommendations = new ArrayList<>();
        }
        
        // Getter和setter方法...
    }
    
    public static DiagnosticResult runFullDiagnostic() {
        DiagnosticResult result = new DiagnosticResult();
        
        // 检测所有相关模组
        checkModPresence(result);
        checkVersionCompatibility(result);
        checkRecipeConflicts(result);
        checkResourceConflicts(result);
        
        // 生成建议
        generateRecommendations(result);
        
        return result;
    }
    
    private static void checkModPresence(DiagnosticResult result) {
        String[] importantMods = {
            "jei", "rei", "emi", "patchouli", "curios", 
            "inventorysorter", "craftingtweaks"
        };
        
        for (String modId : importantMods) {
            boolean present = CompatibilityDetector.isModLoaded(modId);
            result.getModStatuses().put(modId, 
                present ? CompatibilityStatus.PRESENT : CompatibilityStatus.MISSING);
            
            if (!present) {
                result.getIssues().add("Missing recommended mod: " + modId);
            }
        }
    }
    
    private static void checkVersionCompatibility(DiagnosticResult result) {
        // 检查版本兼容性
        if (CompatibilityDetector.isModLoaded("jei")) {
            if (!VersionCompatibility.isVersionCompatible("jei", "17.0.0")) {
                result.getIssues().add("JEI version may be incompatible");
            }
        }
    }
    
    private static void generateRecommendations(DiagnosticResult result) {
        if (result.getIssues().isEmpty()) {
            result.getRecommendations().add("All compatibility checks passed!");
        } else {
            result.getRecommendations().add("Install missing recommended mods for better experience");
            result.getRecommendations().add("Check mod versions for compatibility");
        }
    }
}
```

---
*相关文档链接：*
- [配方系统设计](../../development/recipe-system.md)
- [JEI集成指南](../../development/jei-integration.md)
- [模组兼容性最佳实践](../../development/mod-compatibility-best-practices.md)