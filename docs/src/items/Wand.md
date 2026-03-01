# Wand 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/items/Wand.java`  
**包名**: `org.creepebucket.programmable_magic.items`  
**继承关系**: `Item` → `Wand`  
**物品类型**: 功能性工具物品

## 🎯 类设计目的

`Wand` 是整个可编程魔法系统的核心**交互入口物品**，它既是法术的载体，也是玩家与魔法系统交互的主要界面。设计上融合了现代UI理念和游戏机制，提供了直观的法术编程体验。

## 🏗️ 核心数据结构

### 物品属性定义
```java
public class Wand extends Item {
    // 基础属性
    public static final int MAX_SPELL_SLOTS = 9;      // 最大法术槽数量
    public static final int MAX_MANA_CAPACITY = 1000; // 最大魔力容量
    
    // NBT标签键名定义
    private static final String TAG_SPELL_SLOTS = "SpellSlots";
    private static final String TAG_CURRENT_MANA = "CurrentMana";
    private static final String TAG_PLUGIN_DATA = "PluginData";
    private static final String TAG_DEBUG_MODE = "DebugMode";
}
```

### 魔力系统集成
```java
// 四维魔力数据结构
public static class WandMana {
    private ModUtils.Mana currentMana;    // 当前魔力
    private ModUtils.Mana maxMana;        // 最大魔力容量
    private long lastRegenTime;           // 上次回复时间戳
    
    public boolean consumeMana(ModUtils.Mana cost) {
        if (currentMana.anyLessThan(cost)) {
            return false; // 魔力不足
        }
        currentMana.subtract(cost);
        markDirty();
        return true;
    }
    
    public void regenerateMana() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastRegenTime;
        
        if (timePassed >= MANA_REGEN_INTERVAL_MS) {
            ModUtils.Mana regenAmount = calculateRegenAmount(timePassed);
            currentMana.add(regenAmount.min(maxMana.subtract(currentMana)));
            lastRegenTime = currentTime;
            markDirty();
        }
    }
}
```

## 🔧 核心方法详解

### 物品交互方法

#### `use()` - 主要使用逻辑
```java
@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack wandStack = player.getItemInHand(hand);
    
    // 调试模式检测
    if (player.isShiftKeyDown()) {
        return openWandInterface(player, wandStack, true);  // 调试模式
    }
    
    // 正常施法模式
    if (player.getCooldowns().isOnCooldown(this)) {
        return InteractionResultHolder.fail(wandStack);
    }
    
    return castStoredSpell(player, wandStack);
}

private InteractionResultHolder<ItemStack> openWandInterface(Player player, 
                                                           ItemStack wandStack, 
                                                           boolean debugMode) {
    if (player.level().isClientSide) {
        // 客户端打开GUI
        Minecraft.getInstance().setScreen(new WandScreen(wandStack, debugMode));
    }
    return InteractionResultHolder.success(wandStack);
}
```

#### `castStoredSpell()` - 法术施放逻辑
```java
private InteractionResultHolder<ItemStack> castStoredSpell(Player player, ItemStack wandStack) {
    // 获取存储的法术序列
    SpellSequence spellSequence = getStoredSpellSequence(wandStack);
    
    if (spellSequence.isEmpty()) {
        // 没有存储法术
        player.displayClientMessage(
            Component.translatable("message.wand.no_spell_stored"), true
        );
        return InteractionResultHolder.fail(wandStack);
    }
    
    // 检查魔力
    ModUtils.Mana requiredMana = calculateTotalManaCost(spellSequence, player);
    WandMana wandMana = getWandMana(wandStack);
    
    if (!wandMana.consumeMana(requiredMana)) {
        player.displayClientMessage(
            Component.translatable("message.wand.insufficient_mana"), true
        );
        return InteractionResultHolder.fail(wandStack);
    }
    
    // 应用冷却时间
    player.getCooldowns().addCooldown(this, getCooldownDuration(wandStack));
    
    // 创建法术实体执行
    if (!player.level().isClientSide) {
        SpellEntity.createAndLaunch(
            player.level(),
            player,
            spellSequence.clone(),
            getSpellData(wandStack),
            wandMana.getCurrentMana(),
            getActivePlugins(wandStack),
            isDebugMode(wandStack)
        );
    }
    
    // 播放视觉效果
    playCastEffects(player, wandStack);
    
    return InteractionResultHolder.success(wandStack);
}
```

### NBT数据管理

#### `getStoredSpellSequence()` - 获取存储的法术序列
```java
public SpellSequence getStoredSpellSequence(ItemStack wandStack) {
    CompoundTag tag = wandStack.getOrCreateTag();
    
    if (!tag.contains(TAG_SPELL_SLOTS)) {
        return new SpellSequence(); // 返回空序列
    }
    
    ListTag slotsTag = tag.getList(TAG_SPELL_SLOTS, Tag.TAG_COMPOUND);
    SpellSequence sequence = new SpellSequence();
    
    // 从NBT重建法术序列
    for (int i = 0; i < slotsTag.size(); i++) {
        CompoundTag slotTag = slotsTag.getCompound(i);
        if (slotTag.contains("SpellId")) {
            String spellId = slotTag.getString("SpellId");
            SpellItemLogic spell = SpellRegistry.getSpellById(spellId);
            if (spell != null) {
                sequence.pushRight(spell.clone());
            }
        }
    }
    
    return sequence;
}

public void setStoredSpellSequence(ItemStack wandStack, SpellSequence sequence) {
    CompoundTag tag = wandStack.getOrCreateTag();
    ListTag slotsTag = new ListTag();
    
    // 将法术序列序列化到NBT
    SpellItemLogic current = sequence.head;
    while (current != null) {
        CompoundTag slotTag = new CompoundTag();
        slotTag.putString("SpellId", current.name);
        // 存储参数和其他数据
        slotsTag.add(slotTag);
        current = current.next;
    }
    
    tag.put(TAG_SPELL_SLOTS, slotsTag);
    wandStack.setTag(tag);
}
```

### 插件系统集成

#### `getActivePlugins()` - 获取激活的插件
```java
public List<WandPluginLogic> getActivePlugins(ItemStack wandStack) {
    CompoundTag tag = wandStack.getOrCreateTag();
    List<WandPluginLogic> plugins = new ArrayList<>();
    
    if (tag.contains(TAG_PLUGIN_DATA)) {
        CompoundTag pluginData = tag.getCompound(TAG_PLUGIN_DATA);
        
        // 遍历所有可能的插件槽位
        for (int i = 0; i < WandPluginLogic.MAX_PLUGINS; i++) {
            String pluginKey = "Plugin" + i;
            if (pluginData.contains(pluginKey)) {
                CompoundTag pluginTag = pluginData.getCompound(pluginKey);
                WandPluginLogic plugin = createPluginFromTag(pluginTag);
                if (plugin != null) {
                    plugins.add(plugin);
                }
            }
        }
    }
    
    return plugins;
}

private WandPluginLogic createPluginFromTag(CompoundTag pluginTag) {
    String pluginId = pluginTag.getString("Id");
    WandPluginLogic plugin = PluginRegistry.createPlugin(pluginId);
    
    if (plugin != null) {
        // 恢复插件状态
        plugin.deserializeNBT(pluginTag.getCompound("Data"));
    }
    
    return plugin;
}
```

## 🎨 GUI交互系统

### 右键菜单支持
```java
@Override
public void appendHoverText(ItemStack stack, TooltipContext context, 
                           List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    
    // 显示魔力状态
    WandMana mana = getWandMana(stack);
    tooltip.add(Component.translatable("tooltip.wand.mana")
        .append(": ")
        .append(formatMana(mana.getCurrentMana()))
        .append("/")
        .append(formatMana(mana.getMaxMana())));
    
    // 显示存储的法术数量
    SpellSequence sequence = getStoredSpellSequence(stack);
    if (!sequence.isEmpty()) {
        tooltip.add(Component.translatable("tooltip.wand.stored_spells")
            .append(": ")
            .append(String.valueOf(sequence.getSize())));
    }
    
    // 显示激活的插件
    List<WandPluginLogic> plugins = getActivePlugins(stack);
    if (!plugins.isEmpty()) {
        tooltip.add(Component.translatable("tooltip.wand.plugins")
            .append(": ")
            .append(String.valueOf(plugins.size())));
    }
    
    // 调试模式指示
    if (isDebugMode(stack)) {
        tooltip.add(Component.translatable("tooltip.wand.debug_mode")
            .withStyle(ChatFormatting.GOLD));
    }
}
```

### 物品栏渲染自定义
```java
@Override
public void inventoryTick(ItemStack stack, Level level, Entity entity, 
                         int slotId, boolean isSelected) {
    super.inventoryTick(stack, level, entity, slotId, isSelected);
    
    if (!(entity instanceof Player player)) return;
    
    // 选中时的特殊效果
    if (isSelected && level.isClientSide) {
        renderSelectionEffects(stack, player, level);
    }
    
    // 魔力回复
    if (!level.isClientSide && level.getGameTime() % 20 == 0) {
        WandMana mana = getWandMana(stack);
        mana.regenerateMana();
    }
}

@OnlyIn(Dist.CLIENT)
private void renderSelectionEffects(ItemStack stack, Player player, Level level) {
    // 粒子效果
    if (player.tickCount % 10 == 0) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 particlePos = player.position()
            .add(0, player.getEyeHeight(), 0)
            .add(lookVec.scale(1.5));
            
        level.addParticle(
            ParticleTypes.WITCH,
            particlePos.x, particlePos.y, particlePos.z,
            lookVec.x * 0.1, lookVec.y * 0.1, lookVec.z * 0.1
        );
    }
}
```

## ⚡ 性能优化特性

### 对象池管理
```java
public class WandObjectPool {
    private static final ObjectPool<WandMana> manaPool = 
        new ObjectPool<>(() -> new WandMana());
    
    public static WandMana acquireMana() {
        return manaPool.acquire();
    }
    
    public static void releaseMana(WandMana mana) {
        mana.reset();
        manaPool.release(mana);
    }
}
```

### 数据缓存机制
```java
public class WandDataManager {
    private static final Map<UUID, CachedWandData> dataCache = new ConcurrentHashMap<>();
    
    public static CachedWandData getCachedData(ItemStack wandStack, Player player) {
        UUID cacheKey = createCacheKey(wandStack, player);
        return dataCache.computeIfAbsent(cacheKey, k -> 
            new CachedWandData(wandStack, player)
        );
    }
    
    private static class CachedWandData {
        public final SpellSequence spellSequence;
        public final List<WandPluginLogic> plugins;
        public final ModUtils.Mana totalManaCost;
        public long lastUpdateTime;
        
        public CachedWandData(ItemStack wandStack, Player player) {
            this.spellSequence = getStoredSpellSequence(wandStack);
            this.plugins = getActivePlugins(wandStack);
            this.totalManaCost = calculateTotalManaCost(spellSequence, player);
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public boolean isStale() {
            return System.currentTimeMillis() - lastUpdateTime > CACHE_TIMEOUT_MS;
        }
    }
}
```

## 🛡️ 安全性保障

### 使用权限检查
```java
public class WandSecurity {
    public static boolean canPlayerUseWand(Player player, ItemStack wandStack) {
        // 维度限制检查
        if (!isAllowedInDimension(player.level())) {
            player.displayClientMessage(
                Component.translatable("message.wand.dimension_restricted"), true
            );
            return false;
        }
        
        // 玩家状态检查
        if (player.isSpectator() || player.isCreative()) {
            return true; // 创造模式和旁观者模式特殊处理
        }
        
        // 经验等级要求
        if (player.experienceLevel < getRequiredLevel(wandStack)) {
            player.displayClientMessage(
                Component.translatable("message.wand.insufficient_level"), true
            );
            return false;
        }
        
        return true;
    }
    
    private static boolean isAllowedInDimension(Level level) {
        return level.dimension() != Level.NETHER; // 禁止在下界使用
    }
}
```

### 防滥用机制
```java
public class WandAntiAbuse {
    private static final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
    private static final long MIN_USE_INTERVAL_MS = 100; // 最小使用间隔
    
    public static boolean checkUseInterval(Player player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastUse = lastUseTimes.get(playerId);
        
        if (lastUse != null && currentTime - lastUse < MIN_USE_INTERVAL_MS) {
            return false; // 使用过于频繁
        }
        
        lastUseTimes.put(playerId, currentTime);
        return true;
    }
    
    // 自动清理过期记录
    public static void cleanupOldRecords() {
        long cutoffTime = System.currentTimeMillis() - 60000; // 1分钟前
        lastUseTimes.entrySet().removeIf(entry -> entry.getValue() < cutoffTime);
    }
}
```

## 🔧 实际使用示例

### 基础法术施放
```java
// 玩家使用魔杖的基本流程
public class WandUsageExample {
    public static void demonstrateBasicUsage(Player player) {
        // 1. 获取魔杖
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        
        // 2. 设置法术序列
        SpellSequence sequence = new SpellSequence();
        sequence.pushRight(new DebugPrintSpell());
        Wand.setStoredSpellSequence(wand, sequence);
        
        // 3. 设置初始魔力
        Wand.setWandMana(wand, new ModUtils.Mana(50, 50, 50, 50));
        
        // 4. 玩家使用魔杖
        wand.use(player.level(), player, InteractionHand.MAIN_HAND);
    }
}
```

### 高级插件配置
```java
public class AdvancedWandConfiguration {
    public static ItemStack createAdvancedWand(Player player) {
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        
        // 配置高性能插件
        List<WandPluginLogic> plugins = Arrays.asList(
            new AutoReloadPlugin(),      // 自动回复插件
            new SpellOptimizerPlugin(),  // 法术优化插件
            new DebugHelperPlugin()      // 调试辅助插件
        );
        
        Wand.setActivePlugins(wand, plugins);
        
        // 设置最大魔力容量
        Wand.setMaxMana(wand, new ModUtils.Mana(200, 200, 200, 200));
        
        // 启用调试模式
        Wand.setDebugMode(wand, player.isCreative());
        
        return wand;
    }
}
```

---
*相关文档链接：*
- [WandScreen GUI系统](../gui/WandScreen.md)
- [插件系统详解](../../development/plugin-system.md)
- [魔力系统设计](../../concepts/mana-system.md)
- [物品系统架构](../../architecture/item-system.md)