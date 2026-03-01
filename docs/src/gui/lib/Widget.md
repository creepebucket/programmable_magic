# Widget 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/gui/lib/Widget.java`  
**包名**: `org.creepebucket.programmable_magic.gui.lib`  
**继承关系**: `Object` → `Widget` (抽象类)  
**设计模式**: 组件模式 + 响应式布局

## 🎯 类设计目的

`Widget` 是整个GUI系统的**基础组件抽象类**，提供了现代化UI框架的核心功能。它实现了类似React/Vue的组件化思想，支持声明式UI构建、响应式更新和动画效果。

## 🏗️ 核心数据结构

### 坐标和尺寸系统
```java
public abstract class Widget {
    // 坐标定义系统
    protected Coordinate coordinateX;    // X轴坐标定义
    protected Coordinate coordinateY;    // Y轴坐标定义
    protected Coordinate coordinateWidth; // 宽度定义
    protected Coordinate coordinateHeight; // 高度定义
    
    // 实际渲染坐标（计算后的绝对坐标）
    protected int x, y, width, height;
    
    // 布局相关
    protected boolean visible = true;     // 可见性控制
    protected float alpha = 1.0f;         // 透明度 (0.0-1.0)
}
```

### 坐标定义系统详解
```java
// Coordinate类支持多种定位方式
public enum CoordinateType {
    ABSOLUTE,        // 绝对坐标 (x,y)
    RELATIVE,        // 相对坐标 (相对于父容器的比例)
    FROM_TOP_LEFT,   // 距离左上角偏移
    FROM_BOTTOM_RIGHT, // 距离右下角偏移
    FROM_CENTER,     // 相对于中心点偏移
    PARENT_WIDTH,    // 父容器宽度比例
    PARENT_HEIGHT    // 父容器高度比例
}

// 使用示例
Coordinate.fromTopLeft(10, 20);     // 距离左上角(10,20)像素
Coordinate.fromBottomRight(-5, -5); // 距离右下角内侧5像素
Coordinate.fromCenter(0, 0);        // 居中对齐
Coordinate.relative(0.5f, 0.3f);    // 父容器50%宽度，30%高度位置
```

### 动画和过渡系统
```java
// 平滑值动画支持
protected SmoothedValue smoothedX = new SmoothedValue(0);
protected SmoothedValue smoothedY = new SmoothedValue(0);
protected SmoothedValue smoothedWidth = new SmoothedValue(0);
protected SmoothedValue smoothedHeight = new SmoothedValue(0);
protected SmoothedValue smoothedAlpha = new SmoothedValue(1.0);

// 预定义动画序列
protected List<Animation> animations = new ArrayList<>();
protected boolean animationPlaying = false;
```

## 🔧 核心方法详解

### 生命周期方法

#### `onInitialize()` - 初始化回调
```java
public void onInitialize() {
    // 组件创建后的初始化逻辑
    // 通常在这里设置初始状态、创建子组件
    
    // 示例：初始化动画
    smoothedX.setSpeed(5.0);
    smoothedY.setSpeed(5.0);
    
    // 示例：设置初始坐标
    setCoordinate(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(100, 30));
}
```

#### `render()` - 渲染方法
```java
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) return;
    
    // 执行动画步骤
    doAnimationStep();
    
    // 应用变换矩阵
    PoseStack poseStack = graphics.pose();
    poseStack.pushPose();
    
    try {
        // 应用平滑变换
        poseStack.translate(smoothedX.getCurrent(), smoothedY.getCurrent(), 0);
        poseStack.scale(
            (float)(smoothedWidth.getCurrent() / width),
            (float)(smoothedHeight.getCurrent() / height),
            1.0f
        );
        
        // 应用透明度
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, smoothedAlpha.getCurrent());
        
        // 执行具体渲染逻辑
        renderSelf(graphics, mouseX, mouseY, partialTick);
        
        // 渲染子组件
        renderChildren(graphics, mouseX, mouseY, partialTick);
        
    } finally {
        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
```

#### `tick()` - 更新方法
```java
public void tick(double deltaTime) {
    // 每帧更新逻辑
    doAnimationStep(deltaTime);
    
    // 更新子组件
    for (Widget child : children) {
        if (child.visible) {
            child.tick(deltaTime);
        }
    }
    
    // 执行自定义更新逻辑
    onUpdate(deltaTime);
}
```

### 坐标和布局方法

#### `setCoordinate()` - 设置坐标系统
```java
public void setCoordinate(Coordinate xCoord, Coordinate yCoord) {
    this.coordinateX = xCoord;
    this.coordinateY = yCoord;
    markDirty(); // 标记需要重新计算布局
}

public void setCoordinate(Coordinate xCoord, Coordinate yCoord, 
                         Coordinate widthCoord, Coordinate heightCoord) {
    this.coordinateX = xCoord;
    this.coordinateY = yCoord;
    this.coordinateWidth = widthCoord;
    this.coordinateHeight = heightCoord;
    markDirty();
}

// 链式调用支持
public Widget setCoordinate(Coordinate x, Coordinate y) {
    setCoordinate(x, y);
    return this;
}
```

#### `calculateLayout()` - 布局计算
```java
protected void calculateLayout() {
    if (parent == null) return;
    
    // 计算X坐标
    x = calculateCoordinate(coordinateX, parent.getWidth(), parent.getX());
    
    // 计算Y坐标
    y = calculateCoordinate(coordinateY, parent.getHeight(), parent.getY());
    
    // 计算宽度
    if (coordinateWidth != null) {
        width = calculateCoordinate(coordinateWidth, parent.getWidth(), 0);
    }
    
    // 计算高度
    if (coordinateHeight != null) {
        height = calculateCoordinate(coordinateHeight, parent.getHeight(), 0);
    }
    
    layoutDirty = false;
}

private int calculateCoordinate(Coordinate coord, int parentSize, int parentPos) {
    return switch (coord.type) {
        case ABSOLUTE -> coord.value1;
        case RELATIVE -> parentPos + (int)(parentSize * coord.value1);
        case FROM_TOP_LEFT -> parentPos + coord.value1;
        case FROM_BOTTOM_RIGHT -> parentPos + parentSize - coord.value1;
        case FROM_CENTER -> parentPos + parentSize / 2 + coord.value1;
        case PARENT_WIDTH -> (int)(parentSize * coord.value1);
        case PARENT_HEIGHT -> (int)(parentSize * coord.value1);
    };
}
```

### 事件处理方法

#### 鼠标事件处理
```java
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!isVisible() || !isMouseOver(mouseX, mouseY)) {
        return false;
    }
    
    // 处理子组件事件（从后往前，确保正确的z-order）
    for (int i = children.size() - 1; i >= 0; i--) {
        Widget child = children.get(i);
        if (child.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
    }
    
    // 处理自身事件
    return onMouseClicked(mouseX, mouseY, button);
}

public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
    if (!isVisible() || !isMouseOver(mouseX, mouseY)) {
        return false;
    }
    
    // 传递给子组件
    for (int i = children.size() - 1; i >= 0; i--) {
        Widget child = children.get(i);
        if (child.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
            return true;
        }
    }
    
    return onMouseScrolled(mouseX, mouseY, deltaX, deltaY);
}
```

#### 键盘事件处理
```java
public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    // 传递给子组件
    for (Widget child : children) {
        if (child.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
    }
    
    return onKeyPressed(keyCode, scanCode, modifiers);
}
```

### 动画系统方法

#### `addAnimation()` - 添加动画
```java
public void addAnimation(Animation animation, double delaySeconds) {
    if (delaySeconds > 0) {
        // 延迟执行的动画
        delayedAnimations.add(new DelayedAnimation(animation, delaySeconds));
    } else {
        // 立即执行的动画
        animations.add(animation);
        animation.initialize(this);
    }
    animationPlaying = true;
}

// 预定义动画快捷方法
public void fadeIn(double duration) {
    addAnimation(new Animation.FadeIn(duration), 0);
}

public void fadeOut(double duration) {
    addAnimation(new Animation.FadeOut(duration), 0);
}

public void scaleTo(double targetScale, double duration) {
    addAnimation(new Animation.Scale(1.0, targetScale, duration), 0);
}

public void moveTo(int targetX, int targetY, double duration) {
    addAnimation(new Animation.Move(
        getX(), getY(), targetX, targetY, duration
    ), 0);
}
```

#### `doAnimationStep()` - 动画执行
```java
protected void doAnimationStep(double deltaTime) {
    // 处理延迟动画
    processDelayedAnimations(deltaTime);
    
    if (animations.isEmpty()) {
        animationPlaying = false;
        return;
    }
    
    // 执行活动动画
    Iterator<Animation> iterator = animations.iterator();
    while (iterator.hasNext()) {
        Animation animation = iterator.next();
        
        if (animation.isFinished()) {
            iterator.remove();
            continue;
        }
        
        animation.update(deltaTime);
    }
    
    animationPlaying = !animations.isEmpty();
}
```

## 🎯 子组件管理系统

### 容器功能
```java
protected List<Widget> children = new ArrayList<>();
protected Widget parent;

public void addChild(Widget child) {
    if (child.parent != null) {
        child.parent.removeChild(child);
    }
    
    children.add(child);
    child.parent = this;
    child.onAddedToParent(this);
    
    markDirty(); // 重新计算布局
}

public void removeChild(Widget child) {
    if (children.remove(child)) {
        child.parent = null;
        child.onRemovedFromParent(this);
        markDirty();
    }
}

public void clearChildren() {
    for (Widget child : children) {
        child.parent = null;
        child.onRemovedFromParent(this);
    }
    children.clear();
    markDirty();
}
```

### 渲染顺序管理
```java
// z-index支持
protected int zIndex = 0;

public void setZIndex(int zIndex) {
    this.zIndex = zIndex;
    if (parent != null) {
        parent.sortChildrenByZIndex();
    }
}

private void sortChildrenByZIndex() {
    children.sort(Comparator.comparingInt(w -> w.zIndex));
}
```

## 🔧 实际使用示例

### 创建自定义按钮组件
```java
public class CustomButtonWidget extends Widget {
    private Component text;
    private Runnable onClickHandler;
    private boolean hovered = false;
    
    public CustomButtonWidget(Component text) {
        this.text = text;
        setCoordinate(
            Coordinate.fromCenter(-50, -15),
            Coordinate.fromCenter(50, 15)
        );
    }
    
    @Override
    public void onInitialize() {
        // 初始化动画
        smoothedAlpha.setSpeed(10.0);
        addAnimation(new Animation.FadeIn(0.3), 0);
    }
    
    @Override
    protected void renderSelf(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 检测鼠标悬停
        hovered = isMouseOver(mouseX, mouseY);
        
        // 绘制按钮背景
        int backgroundColor = hovered ? 0xFF555555 : 0xFF333333;
        graphics.fill(x, y, x + width, y + height, backgroundColor);
        
        // 绘制边框
        graphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
        
        // 绘制文字
        Font font = Minecraft.getInstance().font;
        int textX = x + (width - font.width(text)) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, 0xFFFFFF);
    }
    
    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && onClickHandler != null) { // 左键点击
            onClickHandler.run();
            
            // 点击反馈动画
            addAnimation(new Animation.Scale(1.0, 0.9, 0.1), 0);
            addAnimation(new Animation.Scale(0.9, 1.0, 0.1), 0.1);
            
            return true;
        }
        return false;
    }
    
    public CustomButtonWidget onClick(Runnable handler) {
        this.onClickHandler = handler;
        return this;
    }
}
```

### 创建复合组件
```java
public class SpellSlotWidget extends Widget {
    private SlotWidget slot;
    private TextWidget label;
    private ImageButtonWidget removeButton;
    
    public SpellSlotWidget(int slotIndex) {
        // 设置整体尺寸
        setCoordinate(
            Coordinate.fromTopLeft(0, slotIndex * 40),
            Coordinate.fromTopLeft(200, 35)
        );
    }
    
    @Override
    public void onInitialize() {
        // 创建子组件
        slot = new SlotWidget(0, 5, 5);
        label = new TextWidget(Component.literal("法术槽位"))
            .setCoordinate(Coordinate.fromTopLeft(40, 10));
        removeButton = new ImageButtonWidget(Textures.REMOVE_ICON)
            .setCoordinate(Coordinate.fromTopLeft(170, 5))
            .setSize(20, 20)
            .onClick(this::onRemoveClicked);
        
        // 添加子组件
        addChild(slot);
        addChild(label);
        addChild(removeButton);
        
        // 初始化动画
        setAlpha(0);
        addAnimation(new Animation.FadeIn(0.5), 0);
        addAnimation(new Animation.Move(-20, 0, 0, 0, 0.5), 0);
    }
    
    private void onRemoveClicked() {
        // 删除确认动画
        addAnimation(new Animation.Shake(0.3), 0);
        // 实际删除逻辑...
    }
}
```

## ⚡ 性能优化特性

### 批量渲染优化
```java
public class RenderBatch {
    private final List<RenderOperation> operations = new ArrayList<>();
    
    public void addOperation(RenderOperation op) {
        operations.add(op);
    }
    
    public void execute(GuiGraphics graphics) {
        // 批量执行渲染操作，减少状态切换
        RenderSystem.enableBlend();
        for (RenderOperation op : operations) {
            op.render(graphics);
        }
        RenderSystem.disableBlend();
        operations.clear();
    }
}
```

### 脏矩形优化
```java
private Rectangle dirtyRegion = new Rectangle();

public void markDirty() {
    layoutDirty = true;
    // 计算脏矩形区域用于局部重绘
    dirtyRegion.setBounds(
        getX(), getY(), 
        getWidth(), getHeight()
    );
}

public boolean needsRedraw() {
    return layoutDirty || animationPlaying || isDirty();
}
```

## 🛡️ 错误处理和调试

### 调试辅助功能
```java
public class WidgetDebugger {
    private boolean debugMode = false;
    private Set<Widget> highlightedWidgets = new HashSet<>();
    
    public void highlightWidget(Widget widget) {
        highlightedWidgets.add(widget);
    }
    
    public void renderDebugInfo(GuiGraphics graphics, Widget widget) {
        if (!debugMode || !highlightedWidgets.contains(widget)) return;
        
        // 绘制调试边框
        graphics.renderOutline(
            widget.getX() - 1, widget.getY() - 1,
            widget.getWidth() + 2, widget.getHeight() + 2,
            0xFFFF0000
        );
        
        // 显示调试信息
        Font font = Minecraft.getInstance().font;
        String debugInfo = String.format(
            "Widget[%s] Pos:(%d,%d) Size:(%d×%d)",
            widget.getClass().getSimpleName(),
            widget.getX(), widget.getY(),
            widget.getWidth(), widget.getHeight()
        );
        
        graphics.drawString(font, debugInfo, 
            widget.getX(), widget.getY() - 12, 0xFFFF0000);
    }
}
```

---
*相关文档链接：*
- [Screen 系统详解](Screen.md)
- [动画系统设计](AnimationSystem.md)
- [坐标系统详解](CoordinateSystem.md)
- [GUI开发指南](../../../development/gui-development.md)