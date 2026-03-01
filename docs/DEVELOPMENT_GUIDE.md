# 可编程魔法模组 - 开发指南

## 🚀 快速开始

### 环境准备

#### 必需软件
- **Java Development Kit 17+**
- **Git**
- **IDE** (推荐 IntelliJ IDEA)
- **Python 3.8+** (用于资源构建)

#### 项目克隆
```bash
git clone <repository-url>
cd programmable_magic
```

#### 环境变量设置
```bash
# Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:PATH += ";$env:JAVA_HOME\bin"

# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 构建项目

#### 首次构建
```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

#### 开发模式运行
```bash
# 启动客户端
./gradlew runClient

# 启动服务端
./gradlew runServer

# 生成数据
./gradlew runData
```

## 📚 核心开发概念

### 1. 法术开发

#### 创建新法术的基本步骤

##### 1.1 定义法术逻辑类
```java
public class MyCustomSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {
    public MyCustomSpell() {
        name = "my_custom_spell";
        subCategory = "spell." + MODID + ".subcategory.my_category";
        inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.VECTOR3));
        outputTypes = List.of(List.of(SpellValueType.ENTITY));
        precedence = 2; // 运算优先级
    }

    @Override
    public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                              List<Object> paramsList, SpellEntity spellEntity) {
        // 获取参数
        Double numberParam = (Double) paramsList.get(0);
        Vec3 vectorParam = (Vec3) paramsList.get(1);
        
        // 执行逻辑
        // ... 你的法术逻辑
        
        // 返回结果
        return ExecutionResult.RETURNED(this, List.of(resultEntity), 
                                       List.of(SpellValueType.ENTITY));
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, 
                         List<Object> paramsList, SpellEntity spellEntity) {
        // 检查是否可以执行
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, 
                                    List<Object> paramsList, SpellEntity spellEntity) {
        // 返回魔力消耗
        return new ModUtils.Mana(10.0, 5.0, 0.0, 0.0);
    }
}
```

##### 1.2 注册法术
在 `SpellRegistry.java` 中添加：
```java
public static void registerSpells(IEventBus eventBus) {
    // ... 现有法术
    
    // 注册新法术
    registerSpell(MyCustomSpell::new);
    
    // ... 其他法术
}
```

##### 1.3 添加本地化文本
在 `lang/en_us.json` 中添加：
```json
{
    "item.programmable_magic.spell_display_my_custom_spell": "My Custom Spell",
    "spell.programmable_magic.subcategory.my_category": "My Category"
}
```

### 2. 插件开发

#### 创建魔杖插件

##### 2.1 服务端逻辑
```java
public class MyWandPluginLogic extends WandPluginLogic {
    @Override
    public void adjustWandValues(ModUtils.WandValues values, ItemStack pluginStack) {
        // 调整魔杖属性
        values.manaMult = 1.2;        // 1.2倍魔力
        values.chargeRateW = 50.0;    // 50W充能功率
        values.spellSlots = 1024;     // 1024个法术槽位
    }
    
    @Override
    public void beforeSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell,
                                   Map<String, Object> spellData, SpellSequence spellSequence,
                                   List<Object> spellParams) {
        // 法术执行前的处理
    }
    
    @Override
    public void afterSpellExecution(SpellEntity spellEntity, SpellItemLogic currentSpell,
                                  Map<String, Object> spellData, SpellSequence spellSequence,
                                  List<Object> spellParams) {
        // 法术执行后的处理
    }
}
```

##### 2.2 客户端UI组件
```java
public class MyPluginWidget extends BasePlugin {
    private Widget myCustomWidget;
    
    @Override
    public void onAdd(WandScreen screen) {
        // 创建自定义UI组件
        myCustomWidget = new Widget.BlankWidget(
            Coordinate.fromTopRight(0, 0), 
            Coordinate.fromTopLeft(100, 50)
        );
        
        // 添加到屏幕
        screen.addTopbar(myCustomWidget);
        
        // 添加交互元素
        myCustomWidget.addChild(new TextWidget(
            Coordinate.fromTopLeft(0, 0),
            Component.literal("My Plugin")
        ));
    }
    
    @Override
    public void onRemove(WandScreen screen) {
        // 清理资源
        myCustomWidget.removeMyself();
    }
    
    @Override
    public Component function() {
        return Component.literal("Provides custom functionality");
    }
}
```

##### 2.3 注册插件
```java
// 服务端注册
public static void registerPlugins(IEventBus eventBus) {
    registerPlugin("my_custom_plugin", MyWandPluginLogic::new);
}

// 客户端注册
public static void registerClientPlugins() {
    registerClientPlugin("my_custom_plugin", MyPluginWidget::new);
}
```

### 3. GUI组件开发

#### 创建自定义控件

##### 3.1 基础控件
```java
public class MyCustomWidget extends Widget implements Renderable, Lifecycle {
    private String displayText;
    private Color backgroundColor;
    
    public MyCustomWidget(Coordinate pos, Coordinate size, String text) {
        super(pos, size);
        this.displayText = text;
        this.backgroundColor = new Color(0xFF000000);
    }
    
    @Override
    public void onInitialize() {
        // 初始化逻辑
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        graphics.fill(x(), y(), x() + w(), y() + h(), backgroundColor.toArgb());
        
        // 渲染文本
        graphics.drawString(
            Minecraft.getInstance().font,
            displayText,
            x() + 5,
            y() + 5,
            0xFFFFFF
        );
    }
}
```

##### 3.2 带动画的控件
```java
public class AnimatedWidget extends Widget {
    private final SmoothedValue alpha = new SmoothedValue(0);
    
    public AnimatedWidget(Coordinate pos, Coordinate size) {
        super(pos, size);
        smoothedValues.add(alpha);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int currentAlpha = (int) (alpha.get() * 255);
        graphics.fill(
            x(), y(), x() + w(), y() + h(),
            (currentAlpha << 24) | 0x00FFFFFF
        );
    }
    
    public void fadeIn() {
        addAnimation(new Animation.FadeIn(0.3), 0);
        alpha.set(1.0);
    }
    
    public void fadeOut() {
        addAnimation(new Animation.FadeOut(0.3), 0);
        alpha.set(0.0);
    }
}
```

### 4. 网络通信

#### 自定义数据包

##### 4.1 定义数据包
```java
public class MyCustomPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, MyCustomPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MyCustomPacket::message,
            MyCustomPacket::new
        );
    
    private final String message;
    
    public MyCustomPacket(String message) {
        this.message = message;
    }
    
    public String message() {
        return message;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // 处理数据包逻辑
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // 服务端处理
            } else {
                // 客户端处理
            }
        });
    }
}
```

##### 4.2 注册和发送
```java
// 注册
public static void registerPayloads(BusNetworkRegistration registry) {
    registry.playToClient(
        Type.create("my_custom_packet"),
        MyCustomPacket.STREAM_CODEC,
        MyCustomPacket::handle
    );
}

// 发送
public static void sendToServer(MyCustomPacket packet) {
    ClientPlayNetworking.send(packet);
}

public static void sendToPlayer(ServerPlayer player, MyCustomPacket packet) {
    ServerPlayNetworking.send(player, packet);
}
```

## 🛠️ 开发工具和最佳实践

### 代码规范

#### 命名约定
```java
// 类名 - 驼峰命名法
public class SpellItemLogic {}
public class WandMenu {}

// 常量 - 全大写加下划线
public static final String MODID = "programmable_magic";
public static final int MAX_SPELL_SLOTS = 1024;

// 方法名 - 驼峰命名法
public ExecutionResult runWithCheck() {}
public boolean canRun() {}

// 变量名 - 驼峰命名法
private SpellItemLogic currentSpell;
private List<Object> paramList;
```

#### 注释规范
```java
/**
 * 法术执行的主要方法
 * @param caster 法术施法者
 * @param spellSequence 法术序列
 * @param paramsList 参数列表
 * @param spellEntity 法术实体
 * @return 执行结果
 */
@Override
public ExecutionResult run(Player caster, SpellSequence spellSequence, 
                          List<Object> paramsList, SpellEntity spellEntity) {
    // 实现逻辑
}
```

### 调试技巧

#### 日志输出
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
    
    public void myMethod() {
        LOGGER.info("Method called with parameters: {}", someParameter);
        LOGGER.debug("Debug information: {}", debugInfo);
        LOGGER.warn("Warning: {}", warningMessage);
        LOGGER.error("Error occurred: ", exception);
    }
}
```

#### 调试模式
```java
// 启用调试模式
if (ModUtils.DEBUG_MODE) {
    // 调试专用代码
    System.out.println("Debug: " + debugInfo);
}

// 条件编译
#ifdef DEBUG
    // 调试代码
#endif
```

### 性能优化

#### 资源管理
```java
// 正确的资源关闭方式
try (Image image = ImageIO.read(file)) {
    // 使用image
} catch (IOException e) {
    LOGGER.error("Failed to load image", e);
}

// 避免内存泄漏
@Override
public void onDestroy() {
    if (resource != null) {
        resource.close();
        resource = null;
    }
}
```

#### 渲染优化
```java
// 批量渲染
public void renderBatch(GuiGraphics graphics) {
    // 收集所有需要渲染的元素
    List<Renderable> renderables = collectRenderables();
    
    // 一次性渲染
    for (Renderable renderable : renderables) {
        renderable.render(graphics, mouseX, mouseY, partialTick);
    }
}

// 避免不必要的计算
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // 缓存计算结果
    if (needsRecalculation) {
        recalculatePositions();
        needsRecalculation = false;
    }
    
    // 使用缓存的结果渲染
    renderCached(graphics);
}
```

## 🧪 测试指南

### 单元测试
```java
public class SpellLogicTest {
    @Test
    public void testSpellExecution() {
        // 准备测试数据
        Player mockPlayer = Mockito.mock(Player.class);
        SpellSequence sequence = new SpellSequence();
        
        // 执行测试
        MyCustomSpell spell = new MyCustomSpell();
        ExecutionResult result = spell.run(mockPlayer, sequence, params, spellEntity);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(ExecutionResult.Type.SUCCESS, result.getType());
    }
}
```

### 集成测试
```java
public class WandIntegrationTest {
    @Test
    public void testWandFunctionality() {
        // 创建测试环境
        Level level = createTestLevel();
        Player player = createTestPlayer(level);
        
        // 测试魔杖功能
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        // ... 测试逻辑
        
        // 验证结果
        assertTrue(wand.hasTag());
    }
}
```

## 📦 发布准备

### 版本管理
```properties
# gradle.properties
mod_version=1.0.0
minecraft_version=1.21.1
neo_version=21.1.72
```

### 构建发布版本
```bash
# 清理并构建
./gradlew clean build

# 生成发布包
./gradlew jar

# 验证构建结果
ls build/libs/
```

### 文档更新
- 更新 CHANGELOG.md
- 更新 README.md
- 更新 API 文档
- 准备发布说明

## ❓ 常见问题解答

### Q: 如何添加新的法术类型？
A: 继承 `SpellItemLogic` 并实现必要的抽象方法，在 `SpellRegistry` 中注册。

### Q: GUI组件如何响应鼠标事件？
A: 重写 `Widget` 类的 `onMouseClick` 等方法，或使用现有的交互组件。

### Q: 如何优化大型法术序列的性能？
A: 考虑使用法术编译缓存，避免重复的类型检查和参数验证。

### Q: 插件间如何通信？
A: 通过 `SpellEntity` 的 `spellData` Map 共享数据，或使用自定义事件系统。

---
*文档版本：1.0 | 更新时间：2026-03-01*