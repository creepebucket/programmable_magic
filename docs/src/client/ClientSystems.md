# 客户端系统综合文档

## 📋 概述

本文档整合了项目中所有客户端相关的小组件系统，包括粒子效果、客户端事件处理、UI上下文和HUD显示等。

## 🎨 粒子系统

### FastDustParticle 粒子实现

**文件组成**:
- `FastDustParticleOptions.java` - 粒子配置选项
- `FastDustParticleType.java` - 粒子类型定义

#### 核心设计
```java
// 粒子选项配置类
public class FastDustParticleOptions implements ParticleOptions {
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final float scale;
    
    // StreamCodec用于网络传输
    public static final StreamCodec<RegistryFriendlyByteBuf, FastDustParticleOptions> STREAM_CODEC = 
        StreamCodec.composite(
            ByteBufCodecs.FLOAT, FastDustParticleOptions::red,
            ByteBufCodecs.FLOAT, FastDustParticleOptions::green,
            ByteBufCodecs.FLOAT, FastDustParticleOptions::blue,
            ByteBufCodecs.FLOAT, FastDustParticleOptions::alpha,
            ByteBufCodecs.FLOAT, FastDustParticleOptions::scale,
            FastDustParticleOptions::new
        );
}

// 粒子类型注册
public class FastDustParticleType extends ParticleType<FastDustParticleOptions> {
    public FastDustParticleType() {
        super(false); // 不需要视角计算
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FastDustParticleOptions> codec() {
        return FastDustParticleOptions.STREAM_CODEC;
    }
}
```

#### 粒子渲染实现
```java
// 客户端粒子工厂
public class FastDustParticleProvider implements ParticleProvider<FastDustParticleOptions> {
    @Override
    public Particle createParticle(FastDustParticleOptions options, 
                                 ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed) {
        return new FastDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options);
    }
}

// 粒子渲染类
public class FastDustParticle extends TextureSheetParticle {
    private final FastDustParticleOptions options;
    
    public FastDustParticle(ClientLevel level, double x, double y, double z,
                          double xSpeed, double ySpeed, double zSpeed,
                          FastDustParticleOptions options) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.options = options;
        
        // 设置粒子属性
        this.rCol = options.red();
        this.gCol = options.green();
        this.bCol = options.blue();
        this.alpha = options.alpha();
        this.quadSize = options.scale();
        this.lifetime = 20 + this.random.nextInt(10);
        this.gravity = 0.1f;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 淡出效果
        if (this.age > this.lifetime * 0.75f) {
            this.alpha = this.options.alpha() * (1.0f - (this.age - this.lifetime * 0.75f) / (this.lifetime * 0.25f));
        }
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
```

## 🖱️ 客户端事件处理

### ClientEventHandler 客户端事件管理器

**文件**: `ClientEventHandler.java`

#### 核心功能
```java
@Mod.EventBusSubscriber(modid = Programmable_magic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            handleClientTick();
        }
    }
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            renderCustomOverlays(event.getGuiGraphics());
        }
    }
    
    @SubscribeEvent
    public static void onClientPlayerNetworkEvent(ClientPlayerNetworkEvent.LoggingIn event) {
        // 玩家登录时的客户端初始化
        ClientUiContext.initialize();
    }
    
    private static void handleClientTick() {
        // 处理客户端每帧逻辑
        updateParticleEffects();
        handleKeyBindings();
        updateGuiAnimations();
    }
    
    private static void renderCustomOverlays(GuiGraphics graphics) {
        // 渲染自定义HUD元素
        WandAutoChargeHud.render(graphics);
        renderDebugInfo(graphics);
    }
}
```

### ClientUiContext UI上下文管理

**文件**: `ClientUiContext.java`

#### 简洁的上下文管理
```java
public class ClientUiContext {
    private static Minecraft minecraft;
    private static LocalPlayer localPlayer;
    private static ClientLevel clientLevel;
    
    public static void initialize() {
        minecraft = Minecraft.getInstance();
    }
    
    public static Minecraft getMinecraft() {
        return minecraft;
    }
    
    public static LocalPlayer getLocalPlayer() {
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player;
        }
        return null;
    }
    
    public static ClientLevel getClientLevel() {
        if (minecraft != null) {
            return minecraft.level;
        }
        return null;
    }
    
    public static boolean isClientReady() {
        return minecraft != null && minecraft.player != null && minecraft.level != null;
    }
}
```

## 📊 HUD显示系统

### WandAutoChargeHud 魔杖自动充能显示

**文件**: `WandAutoChargeHud.java`

#### HUD渲染系统
```java
public class WandAutoChargeHud {
    private static final int HUD_WIDTH = 182;
    private static final int HUD_HEIGHT = 5;
    
    public static void render(GuiGraphics graphics) {
        LocalPlayer player = ClientUiContext.getLocalPlayer();
        if (player == null) return;
        
        // 检查玩家是否持有魔杖
        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof Wand)) {
            return;
        }
        
        Wand wand = (Wand) mainHand.getItem();
        Wand.WandMana mana = wand.getWandMana(mainHand);
        
        // 计算HUD位置
        Window window = ClientUiContext.getMinecraft().getWindow();
        int x = window.getGuiScaledWidth() / 2 - HUD_WIDTH / 2;
        int y = window.getGuiScaledHeight() - 35;
        
        // 渲染背景
        renderManaBarBackground(graphics, x, y);
        
        // 渲染魔力条
        renderManaBars(graphics, x, y, mana);
        
        // 渲染数值显示
        renderManaValues(graphics, x, y, mana);
    }
    
    private static void renderManaBarBackground(GuiGraphics graphics, int x, int y) {
        // 渲染四维魔力背景条
        for (int i = 0; i < 4; i++) {
            graphics.blit(TEXTURE, x, y + i * 6, 0, 0, HUD_WIDTH, 5, 256, 256);
        }
    }
    
    private static void renderManaBars(GuiGraphics graphics, int x, int y, Wand.WandMana mana) {
        ModUtils.Mana current = mana.getCurrentMana();
        ModUtils.Mana max = mana.getMaxMana();
        
        // 辐射系魔力 (红色)
        int radiationWidth = (int) (HUD_WIDTH * current.radiation() / max.radiation());
        graphics.fill(x, y, x + radiationWidth, y + 4, 0xFFCC0000);
        
        // 温度系魔力 (橙色)
        int temperatureWidth = (int) (HUD_WIDTH * current.temperature() / max.temperature());
        graphics.fill(x, y + 6, x + temperatureWidth, y + 10, 0xFFFF8800);
        
        // 动量系魔力 (蓝色)
        int momentumWidth = (int) (HUD_WIDTH * current.momentum() / max.momentum());
        graphics.fill(x, y + 12, x + momentumWidth, y + 16, 0xFF3399FF);
        
        // 压力系魔力 (紫色)
        int pressureWidth = (int) (HUD_WIDTH * current.pressure() / max.pressure());
        graphics.fill(x, y + 18, x + pressureWidth, y + 22, 0xFF9933FF);
    }
    
    private static void renderManaValues(GuiGraphics graphics, int x, int y, Wand.WandMana mana) {
        Font font = ClientUiContext.getMinecraft().font;
        ModUtils.Mana current = mana.getCurrentMana();
        ModUtils.Mana max = mana.getMaxMana();
        
        // 渲染数值
        graphics.drawString(font, 
            String.format("%.0f/%.0f", current.radiation(), max.radiation()),
            x + HUD_WIDTH + 5, y, 0xFFFFFF);
        graphics.drawString(font,
            String.format("%.0f/%.0f", current.temperature(), max.temperature()),
            x + HUD_WIDTH + 5, y + 6, 0xFFFFFF);
        graphics.drawString(font,
            String.format("%.0f/%.0f", current.momentum(), max.momentum()),
            x + HUD_WIDTH + 5, y + 12, 0xFFFFFF);
        graphics.drawString(font,
            String.format("%.0f/%.0f", current.pressure(), max.pressure()),
            x + HUD_WIDTH + 5, y + 18, 0xFFFFFF);
    }
}
```

## 🔧 工具提示系统

### ItemTooltipHandler 物品提示处理器

**文件**: `ItemTooltipHandler.java`

#### 智能提示系统
```java
@Mod.EventBusSubscriber(modid = Programmable_magic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemTooltipHandler {
    
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        
        // 魔杖物品特殊提示
        if (stack.getItem() instanceof Wand) {
            addWandTooltip(stack, tooltip);
        }
        
        // 法术物品特殊提示
        if (stack.getItem() instanceof SpellItem) {
            addSpellTooltip(stack, tooltip);
        }
        
        // 调试模式额外信息
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            addAdvancedInfo(stack, tooltip);
        }
    }
    
    private static void addWandTooltip(ItemStack wandStack, List<Component> tooltip) {
        Wand wand = (Wand) wandStack.getItem();
        Wand.WandMana mana = wand.getWandMana(wandStack);
        
        // 添加魔力信息
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.wand.mana").withStyle(ChatFormatting.GOLD));
        
        ModUtils.Mana current = mana.getCurrentMana();
        ModUtils.Mana max = mana.getMaxMana();
        
        tooltip.add(Component.literal(String.format(
            "辐射: %.0f/%.0f", current.radiation(), max.radiation()
        )).withStyle(ChatFormatting.RED));
        
        tooltip.add(Component.literal(String.format(
            "温度: %.0f/%.0f", current.temperature(), max.temperature()
        )).withStyle(ChatFormatting.GOLD));
        
        tooltip.add(Component.literal(String.format(
            "动量: %.0f/%.0f", current.momentum(), max.momentum()
        )).withStyle(ChatFormatting.BLUE));
        
        tooltip.add(Component.literal(String.format(
            "压力: %.0f/%.0f", current.pressure(), max.pressure()
        )).withStyle(ChatFormatting.DARK_PURPLE));
        
        // 添加存储的法术信息
        SpellSequence sequence = wand.getStoredSpellSequence(wandStack);
        if (!sequence.isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.wand.stored_spells")
                .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal(String.valueOf(sequence.size()))
                .withStyle(ChatFormatting.WHITE));
        }
    }
    
    private static void addSpellTooltip(ItemStack spellStack, List<Component> tooltip) {
        SpellItem spellItem = (SpellItem) spellStack.getItem();
        SpellItemLogic spellLogic = spellItem.getLogic();
        
        // 添加法术类型信息
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.spell.type")
            .withStyle(ChatFormatting.GRAY));
        
        // 根据法术类型添加不同的样式
        if (spellLogic instanceof SpellItemLogic.ComputeMod) {
            tooltip.add(Component.translatable("tooltip.spell.type.compute")
                .withStyle(ChatFormatting.GREEN));
        } else if (spellLogic instanceof SpellItemLogic.ControlMod) {
            tooltip.add(Component.translatable("tooltip.spell.type.control")
                .withStyle(ChatFormatting.YELLOW));
        } else if (spellLogic instanceof SpellItemLogic.BaseSpell) {
            tooltip.add(Component.translatable("tooltip.spell.type.base")
                .withStyle(ChatFormatting.WHITE));
        }
        
        // 添加魔力消耗信息
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ModUtils.Mana cost = spellLogic.getManaCost(player, null, null, null);
            if (!cost.isEmpty()) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("tooltip.spell.mana_cost")
                    .withStyle(ChatFormatting.DARK_GRAY));
                addManaCostLines(tooltip, cost);
            }
        }
    }
    
    private static void addManaCostLines(List<Component> tooltip, ModUtils.Mana cost) {
        if (cost.radiation() > 0) {
            tooltip.add(Component.literal(String.format("辐射: %.1f", cost.radiation()))
                .withStyle(ChatFormatting.RED));
        }
        if (cost.temperature() > 0) {
            tooltip.add(Component.literal(String.format("温度: %.1f", cost.temperature()))
                .withStyle(ChatFormatting.GOLD));
        }
        if (cost.momentum() > 0) {
            tooltip.add(Component.literal(String.format("动量: %.1f", cost.momentum()))
                .withStyle(ChatFormatting.BLUE));
        }
        if (cost.pressure() > 0) {
            tooltip.add(Component.literal(String.format("压力: %.1f", cost.pressure()))
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
    
    private static void addAdvancedInfo(ItemStack stack, List<Component> tooltip) {
        // 添加物品ID
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tooltip.add(Component.literal(itemId.toString())
            .withStyle(ChatFormatting.DARK_GRAY));
        
        // 添加NBT大小信息
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tooltip.add(Component.literal(String.format("NBT Size: %d bytes", 
                estimateTagSize(tag)))
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    
    private static int estimateTagSize(CompoundTag tag) {
        int size = 0;
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value != null) {
                size += key.length() + estimateTagValueSize(value);
            }
        }
        return size;
    }
    
    private static int estimateTagValueSize(Tag tag) {
        return switch (tag.getId()) {
            case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG, 
                 Tag.TAG_FLOAT, Tag.TAG_DOUBLE -> 8;
            case Tag.TAG_STRING -> ((StringTag) tag).getAsString().length();
            case Tag.TAG_LIST -> {
                ListTag list = (ListTag) tag;
                int total = 4; // 列表头大小
                for (int i = 0; i < list.size(); i++) {
                    total += estimateTagValueSize(list.get(i));
                }
                yield total;
            }
            case Tag.TAG_COMPOUND -> {
                CompoundTag compound = (CompoundTag) tag;
                int total = 4; // 复合标签头大小
                for (String key : compound.getAllKeys()) {
                    Tag value = compound.get(key);
                    if (value != null) {
                        total += key.length() + estimateTagValueSize(value);
                    }
                }
                yield total;
            }
            default -> 0;
        };
    }
}
```

## ⚡ 性能优化考虑

### 渲染优化策略
```java
public class ClientPerformanceOptimizer {
    private static final Object2IntMap<ItemStack> RENDER_CACHE = new Object2IntOpenHashMap<>();
    private static long lastCacheCleanup = 0;
    
    public static void optimizeRendering() {
        // 定期清理渲染缓存
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheCleanup > 30000) { // 30秒清理一次
            RENDER_CACHE.clear();
            lastCacheCleanup = currentTime;
        }
        
        // 批量渲染操作
        batchRenderOperations();
    }
    
    private static void batchRenderOperations() {
        // 将相似的渲染操作合并
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 执行批量渲染
        performBatchRendering();
        
        RenderSystem.disableBlend();
    }
}
```

### 内存管理
```java
public class ClientMemoryManager {
    private static final List<WeakReference<Object>> TRACKED_OBJECTS = new ArrayList<>();
    
    public static void trackObject(Object obj) {
        TRACKED_OBJECTS.add(new WeakReference<>(obj));
    }
    
    public static void cleanupUnusedObjects() {
        TRACKED_OBJECTS.removeIf(ref -> ref.get() == null);
    }
    
    public static int getTrackedObjectCount() {
        return TRACKED_OBJECTS.size();
    }
}
```

## 🔧 实际使用示例

### 自定义粒子效果
```java
public class ParticleEffectExamples {
    public static void spawnManaParticles(Level level, BlockPos pos, ModUtils.Mana manaType) {
        if (level.isClientSide) {
            // 根据魔力类型选择颜色
            float[] color = getManaColor(manaType);
            
            // 生成粒子效果
            for (int i = 0; i < 10; i++) {
                level.addParticle(
                    ModParticleTypes.FAST_DUST.get(),
                    pos.getX() + 0.5 + (level.random.nextDouble() - 0.5),
                    pos.getY() + 0.5 + level.random.nextDouble(),
                    pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                    (level.random.nextDouble() - 0.5) * 0.1,
                    level.random.nextDouble() * 0.1,
                    (level.random.nextDouble() - 0.5) * 0.1,
                    new FastDustParticleOptions(color[0], color[1], color[2], 1.0f, 0.5f)
                );
            }
        }
    }
    
    private static float[] getManaColor(ModUtils.Mana manaType) {
        // 根据主要魔力类型返回对应颜色
        if (manaType.radiation() > manaType.temperature() && 
            manaType.radiation() > manaType.momentum() && 
            manaType.radiation() > manaType.pressure()) {
            return new float[]{1.0f, 0.0f, 0.0f}; // 红色
        } else if (manaType.temperature() > manaType.momentum() && 
                   manaType.temperature() > manaType.pressure()) {
            return new float[]{1.0f, 0.5f, 0.0f}; // 橙色
        } else if (manaType.momentum() > manaType.pressure()) {
            return new float[]{0.0f, 0.5f, 1.0f}; // 蓝色
        } else {
            return new float[]{0.5f, 0.0f, 1.0f}; // 紫色
        }
    }
}
```

### HUD自定义扩展
```java
public class CustomHudExtensions {
    public static void addCustomHudElement(GuiGraphics graphics, String text, 
                                         int x, int y, int color) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, text, x, y, color, true);
    }
    
    public static void drawBorderedRect(GuiGraphics graphics, int x, int y, 
                                      int width, int height, int borderColor, int fillColor) {
        // 绘制边框
        graphics.fill(x, y, x + width, y + 1, borderColor);           // 上边框
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor); // 下边框
        graphics.fill(x, y, x + 1, y + height, borderColor);          // 左边框
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);  // 右边框
        
        // 填充内部
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fillColor);
    }
}
```

---
*相关文档链接：*
- [GUI系统架构](../../architecture/gui-system.md)
- [粒子系统设计](../../development/particle-effects.md)
- [客户端优化指南](../../development/client-optimization.md)