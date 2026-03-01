# 实体渲染器系统文档

## 📋 概述

本文档详细介绍项目中的自定义实体渲染系统，包括法术实体渲染器、渲染状态管理和API扩展机制。

## 🎨 法术实体渲染系统

### SpellEntityRenderer 法术实体渲染器

**文件**: `SpellEntityRenderer.java`

#### 核心渲染实现
```java
public class SpellEntityRenderer extends EntityRenderer<SpellEntity> {
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(Programmable_magic.MODID, "textures/entity/spell_entity.png");
    
    public SpellEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(SpellEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 获取实体渲染状态
        SpellEntityRenderState renderState = getRenderState(entity);
        
        // 应用变换
        poseStack.pushPose();
        applyTransformations(poseStack, renderState, partialTicks);
        
        // 渲染视觉效果
        renderVisualEffects(poseStack, bufferSource, renderState, packedLight);
        
        // 渲染调试信息（调试模式下）
        if (renderState.isDebugMode()) {
            renderDebugInfo(poseStack, bufferSource, renderState);
        }
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    
    @Override
    public SpellEntityRenderState createRenderState() {
        return new SpellEntityRenderState();
    }
    
    @Override
    public void extractRenderState(SpellEntity entity, SpellEntityRenderState renderState, 
                                  float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        
        // 提取渲染所需的状态信息
        renderState.setPosition(entity.position());
        renderState.setCurrentSpell(entity.getCurrentSpell());
        renderState.setDebugMode(entity.isDebugMode());
        renderState.setMana(entity.getMana());
        renderState.setDelayTicks(entity.getDelayTicks());
    }
}
```

### SpellEntityRenderState 渲染状态类

**文件**: `SpellEntityRenderState.java`

#### 状态数据结构
```java
public class SpellEntityRenderState extends EntityRenderState {
    private Vec3 position;
    private SpellItemLogic currentSpell;
    private boolean debugMode;
    private ModUtils.Mana mana;
    private int delayTicks;
    
    // 粒子效果相关
    private List<ParticleEffect> activeParticles;
    private float particleIntensity;
    
    // 视觉效果参数
    private float scale;
    private float alpha;
    private int[] colorComponents;
    
    // Getter和Setter方法
    public Vec3 getPosition() { return position; }
    public void setPosition(Vec3 position) { this.position = position; }
    
    public SpellItemLogic getCurrentSpell() { return currentSpell; }
    public void setCurrentSpell(SpellItemLogic currentSpell) { this.currentSpell = currentSpell; }
    
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    
    public ModUtils.Mana getMana() { return mana; }
    public void setMana(ModUtils.Mana mana) { this.mana = mana; }
    
    public int getDelayTicks() { return delayTicks; }
    public void setDelayTicks(int delayTicks) { this.delayTicks = delayTicks; }
}
```

## 🔧 渲染API扩展

### 渲染器注册系统
```java
@Mod.EventBusSubscriber(modid = Programmable_magic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererRegistry {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册法术实体渲染器
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);
        
        // 注册其他自定义实体渲染器
        registerCustomEntityRenderers(event);
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册自定义模型层定义
        event.registerLayerDefinition(
            new ModelLayerLocation(
                new ResourceLocation(Programmable_magic.MODID, "spell_effect"), 
                "main"
            ),
            SpellEffectModel::createBodyLayer
        );
    }
    
    private static void registerCustomEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 可以在这里注册其他实体的渲染器
        // 例如：魔力节点实体、粒子效果实体等
    }
}
```

## ✨ 视觉效果系统

### 粒子效果渲染
```java
public class VisualEffectRenderer {
    private static final ParticleEngine particleEngine = 
        Minecraft.getInstance().particleEngine;
    
    public static void renderSpellParticleEffects(PoseStack poseStack, 
                                                MultiBufferSource bufferSource,
                                                SpellEntityRenderState renderState,
                                                int packedLight) {
        Vec3 position = renderState.getPosition();
        SpellItemLogic currentSpell = renderState.getCurrentSpell();
        ModUtils.Mana mana = renderState.getMana();
        
        if (currentSpell != null) {
            // 根据法术类型渲染不同效果
            renderSpellSpecificEffects(poseStack, bufferSource, currentSpell, position, mana, packedLight);
        }
        
        // 渲染魔力指示效果
        renderManaIndicators(poseStack, bufferSource, position, mana, packedLight);
        
        // 渲染执行状态效果
        renderExecutionStatusEffects(poseStack, bufferSource, renderState, packedLight);
    }
    
    private static void renderSpellSpecificEffects(PoseStack poseStack,
                                                 MultiBufferSource bufferSource,
                                                 SpellItemLogic spell,
                                                 Vec3 position,
                                                 ModUtils.Mana mana,
                                                 int packedLight) {
        // 根据法术类型选择效果
        if (spell instanceof SpellItemLogic.ComputeMod) {
            renderComputationEffects(poseStack, bufferSource, position, mana, packedLight);
        } else if (spell instanceof SpellItemLogic.ControlMod) {
            renderControlEffects(poseStack, bufferSource, position, packedLight);
        } else if (spell instanceof SpellItemLogic.BaseSpell) {
            renderBaseEffects(poseStack, bufferSource, position, packedLight);
        }
    }
    
    private static void renderManaIndicators(PoseStack poseStack,
                                           MultiBufferSource bufferSource,
                                           Vec3 position,
                                           ModUtils.Mana mana,
                                           int packedLight) {
        // 渲染四维魔力指示器
        float[] colors = getManaColors(mana);
        
        for (int i = 0; i < 4; i++) {
            float intensity = getManaIntensity(mana, i);
            if (intensity > 0.1f) {
                renderManaOrb(poseStack, bufferSource, position, colors[i * 3], 
                            colors[i * 3 + 1], colors[i * 3 + 2], intensity, packedLight);
            }
        }
    }
    
    private static float[] getManaColors(ModUtils.Mana mana) {
        return new float[]{
            1.0f, 0.0f, 0.0f,  // 辐射 - 红色
            1.0f, 0.5f, 0.0f,  // 温度 - 橙色
            0.0f, 0.5f, 1.0f,  // 动量 - 蓝色
            0.5f, 0.0f, 1.0f   // 压力 - 紫色
        };
    }
    
    private static float getManaIntensity(ModUtils.Mana mana, int dimension) {
        return switch (dimension) {
            case 0 -> (float) (mana.radiation() / 100.0);
            case 1 -> (float) (mana.temperature() / 100.0);
            case 2 -> (float) (mana.momentum() / 100.0);
            case 3 -> (float) (mana.pressure() / 100.0);
            default -> 0.0f;
        };
    }
}
```

## 🎯 调试渲染系统

### 调试信息可视化
```java
public class DebugRenderer {
    private static final Font font = Minecraft.getInstance().font;
    
    public static void renderDebugInfo(PoseStack poseStack, 
                                     MultiBufferSource bufferSource,
                                     SpellEntityRenderState renderState) {
        Vec3 position = renderState.getPosition();
        
        // 转换到屏幕坐标
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Vec3 screenPos = position.subtract(camPos);
        
        // 应用相机变换
        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        
        // 渲染调试文本
        renderDebugText(poseStack, bufferSource, renderState, screenPos);
        
        // 渲染调试框
        renderDebugBoundingBox(poseStack, bufferSource, renderState);
        
        poseStack.popPose();
    }
    
    private static void renderDebugText(PoseStack poseStack,
                                      MultiBufferSource bufferSource,
                                      SpellEntityRenderState renderState,
                                      Vec3 screenPos) {
        List<String> debugLines = new ArrayList<>();
        
        // 添加调试信息
        SpellItemLogic currentSpell = renderState.getCurrentSpell();
        if (currentSpell != null) {
            debugLines.add("Spell: " + currentSpell.name);
            debugLines.add("Precedence: " + currentSpell.precedence);
        }
        
        debugLines.add("Delay: " + renderState.getDelayTicks() + " ticks");
        debugLines.add("Position: " + formatVec3(renderState.getPosition()));
        
        // 渲染每一行文本
        for (int i = 0; i < debugLines.size(); i++) {
            String line = debugLines.get(i);
            int yOffset = i * (font.lineHeight + 1);
            
            font.drawInBatch(
                line,
                (float) screenPos.x + 10,
                (float) screenPos.y - 20 + yOffset,
                0xFFFFFF,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                0xF000F0
            );
        }
    }
    
    private static void renderDebugBoundingBox(PoseStack poseStack,
                                             MultiBufferSource bufferSource,
                                             SpellEntityRenderState renderState) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        
        // 绘制实体包围盒
        poseStack.pushPose();
        poseStack.translate(-0.5, 0, -0.5);
        
        LevelRenderer.renderLineBox(
            poseStack,
            consumer,
            0, 0, 0, 1, 1, 1,
            1.0f, 0.0f, 0.0f, 1.0f  // 红色边框
        );
        
        poseStack.popPose();
    }
    
    private static String formatVec3(Vec3 vec) {
        return String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
    }
}
```

## ⚡ 性能优化策略

### 渲染批处理
```java
public class RenderBatchOptimizer {
    private static final List<RenderOperation> batchedOperations = new ArrayList<>();
    private static int batchSize = 0;
    private static final int MAX_BATCH_SIZE = 100;
    
    public static void queueRenderOperation(RenderOperation operation) {
        batchedOperations.add(operation);
        batchSize++;
        
        if (batchSize >= MAX_BATCH_SIZE) {
            flushBatch();
        }
    }
    
    public static void flushBatch() {
        if (batchedOperations.isEmpty()) return;
        
        // 批量执行渲染操作
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        for (RenderOperation operation : batchedOperations) {
            operation.execute();
        }
        
        RenderSystem.disableBlend();
        
        batchedOperations.clear();
        batchSize = 0;
    }
    
    @FunctionalInterface
    public interface RenderOperation {
        void execute();
    }
}
```

### LOD（细节层次）系统
```java
public class LevelOfDetailManager {
    private static final double NEAR_DISTANCE = 16.0;
    private static final double MID_DISTANCE = 32.0;
    private static final double FAR_DISTANCE = 64.0;
    
    public static RenderDetailLevel getDetailLevel(Vec3 entityPos, Vec3 cameraPos) {
        double distance = entityPos.distanceTo(cameraPos);
        
        if (distance <= NEAR_DISTANCE) {
            return RenderDetailLevel.HIGH;
        } else if (distance <= MID_DISTANCE) {
            return RenderDetailLevel.MEDIUM;
        } else if (distance <= FAR_DISTANCE) {
            return RenderDetailLevel.LOW;
        } else {
            return RenderDetailLevel.MINIMAL;
        }
    }
    
    public enum RenderDetailLevel {
        HIGH,    // 高细节：完整效果
        MEDIUM,  // 中等细节：简化效果
        LOW,     // 低细节：基础效果
        MINIMAL  // 最低细节：仅基本渲染
    }
    
    public static void applyDetailLevel(RenderDetailLevel level, 
                                      SpellEntityRenderState renderState) {
        switch (level) {
            case HIGH:
                renderState.setParticleIntensity(1.0f);
                renderState.setScale(1.0f);
                break;
            case MEDIUM:
                renderState.setParticleIntensity(0.7f);
                renderState.setScale(0.8f);
                break;
            case LOW:
                renderState.setParticleIntensity(0.4f);
                renderState.setScale(0.6f);
                break;
            case MINIMAL:
                renderState.setParticleIntensity(0.1f);
                renderState.setScale(0.3f);
                break;
        }
    }
}
```

## 🔧 自定义渲染扩展

### 渲染器插件系统
```java
public class RenderPluginSystem {
    private static final Map<String, RenderPlugin> plugins = new ConcurrentHashMap<>();
    
    public interface RenderPlugin {
        void onPreRender(SpellEntity entity, SpellEntityRenderState renderState, 
                        PoseStack poseStack, MultiBufferSource bufferSource);
        
        void onPostRender(SpellEntity entity, SpellEntityRenderState renderState, 
                         PoseStack poseStack, MultiBufferSource bufferSource);
        
        boolean shouldRender(SpellEntity entity, SpellEntityRenderState renderState);
    }
    
    public static void registerPlugin(String id, RenderPlugin plugin) {
        plugins.put(id, plugin);
    }
    
    public static void applyPluginsPreRender(SpellEntity entity, 
                                           SpellEntityRenderState renderState,
                                           PoseStack poseStack, 
                                           MultiBufferSource bufferSource) {
        for (RenderPlugin plugin : plugins.values()) {
            if (plugin.shouldRender(entity, renderState)) {
                plugin.onPreRender(entity, renderState, poseStack, bufferSource);
            }
        }
    }
    
    public static void applyPluginsPostRender(SpellEntity entity, 
                                            SpellEntityRenderState renderState,
                                            PoseStack poseStack, 
                                            MultiBufferSource bufferSource) {
        for (RenderPlugin plugin : plugins.values()) {
            if (plugin.shouldRender(entity, renderState)) {
                plugin.onPostRender(entity, renderState, poseStack, bufferSource);
            }
        }
    }
}
```

## 🧪 调试和测试工具

### 渲染测试工具
```java
public class RenderTestTools {
    public static void testSpellRendering(SpellItemLogic spell) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null) return;
        
        // 创建测试实体
        SpellEntity testEntity = new SpellEntity(
            ModEntityTypes.SPELL_ENTITY.get(),
            player.level()
        );
        
        // 设置测试参数
        testEntity.setPos(player.position().add(0, 2, 0));
        testEntity.setCurrentSpell(spell);
        testEntity.setDebugMode(true);
        
        // 添加到世界进行测试
        player.level().addFreshEntity(testEntity);
        
        // 输出测试信息
        player.sendSystemMessage(Component.literal(
            "Rendering test started for spell: " + spell.name
        ));
    }
    
    public static void visualizeRenderBounds(Entity entity) {
        // 在客户端可视化实体渲染边界
        if (entity.level().isClientSide) {
            Vec3 pos = entity.position();
            for (int i = 0; i < 20; i++) {
                entity.level().addParticle(
                    ParticleTypes.FLAME,
                    pos.x + (entity.level().random.nextDouble() - 0.5) * 2.0,
                    pos.y + entity.level().random.nextDouble() * 2.0,
                    pos.z + (entity.level().random.nextDouble() - 0.5) * 2.0,
                    0, 0, 0
                );
            }
        }
    }
}
```

---
*相关文档链接：*
- [实体系统架构](../../architecture/entity-system.md)
- [粒子效果系统](../../development/particle-effects.md)
- [客户端渲染优化](../../development/client-rendering-optimization.md)