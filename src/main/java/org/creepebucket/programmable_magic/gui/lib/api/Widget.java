package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;

import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.ModUtils.now;

    /**
     * UI 控件基类：提供位置和尺寸的基础支持，通过实现功能接口来获得能力。
     */
public abstract class Widget {
    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum VerticalAlign {
        TOP,
        CENTER,
        BOTTOM

    }

    public Coordinate originalPos, originalSize;
    public SmoothedValue dx = new SmoothedValue(0), dy = new SmoothedValue(0), dw = new SmoothedValue(0), dh = new SmoothedValue(0);
    public Color originalMainColor, originalBgColor, originalTextColor;
    public Component tooltip;
    public boolean doShowTooltip = false, renderInForeground = false, enabled = true;
    public Align align = Align.LEFT;
    public VerticalAlign verticalAlign = VerticalAlign.TOP;
    public List<Widget> children = new ArrayList<>();
    public Widget parent;
    public Screen<? extends Menu> screen;
    public List<Animation> animations = new ArrayList<>();
    public List<SmoothedValue> smoothedValues = new ArrayList<>();
    public List<Widget> childrenCache;
    public List<Runnable> clickBehaviors = new ArrayList<>();

    public Widget(Coordinate pos, Coordinate size) {
        this.originalPos = pos;
        this.originalSize = size;

        smoothedValues.addAll(List.of(dx, dy, dw, dh));
    }

    public Widget leftAlign() {
        align = Align.LEFT;
        return this;
    }

    public Widget centerAlign() {
        align = Align.CENTER;
        return this;
    }

    public Widget rightAlign() {
        align = Align.RIGHT;
        return this;
    }

    public Widget topAlignY() {
        verticalAlign = VerticalAlign.TOP;
        return this;
    }

    public Widget centerAlignY() {
        verticalAlign = VerticalAlign.CENTER;
        return this;
    }

    public Widget bottomAlignY() {
        verticalAlign = VerticalAlign.BOTTOM;
        return this;
    }

    public void renderWidget(GuiGraphicsExtractor graphics, int mx, int my, float partialTick, double dt, boolean isForeground) {
        childrenCache = null;

        if (isForeground == renderInForeground) {

            // 动画的step
            for (Animation animation : animations) animation.step(dt);

            // 自己的渲染逻辑
            if (this instanceof Renderable renderable && enabled) {
                renderable.render(graphics, mx, my, partialTick);
            }

            // 检查动画过期/启动状态
            var expiredAnimations = animations.stream().filter(Animation::isExpired).toList();
            var startedAnimations = animations.stream().filter(Animation::isStarted).toList();

            for (Animation animation : expiredAnimations) {
                animation.onExpire(this);
                animations.remove(animation);
            }

            for (Animation animation : startedAnimations) {
                animation.onStart(this);
            }

            // 平滑逻辑
            for (SmoothedValue value : smoothedValues) value.doStep(dt);
        }

        // 孩子的渲染逻辑
        if (enabled) for (Widget child : List.copyOf(children)) child.renderWidget(graphics, mx, my, partialTick, dt, isForeground);
    }

    public Widget enable() {
        enabled = true;
        return this;
    }

    public Widget disable() {
        enabled = false;
        return this;
    }

    public Widget addAnimation(Animation animation, double delay) {
        animation.start = now() + delay;
        animations.add(animation);
        return this;
    }

    /**
     * 注意: 返回的是孩子
     */
    public Widget addChild(Widget widget) {
        children.add(widget);
        widget.parent = this;
        widget.screen = screen;
        for (Widget w : widget.allChild()) w.screen = screen;
        if (widget instanceof Lifecycle lifecycle) lifecycle.onInitialize();
        return widget;
    }

    public List<Widget> allChild() {
        // 先尝试读取缓存
        if (childrenCache != null) return childrenCache;

        // 未命中则动态更新缓存
        List<Widget> list = new ArrayList<>();
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            list.add(widget);
            list.addAll(widget.allChild());
        }
        childrenCache = list;
        return list;
    }

    public void removeChild(Widget widget) {
        children.remove(widget);
        if (widget instanceof Lifecycle lifecycle) lifecycle.onDestroy();
    }

    public void removeMyself() {
        if (parent != null) {
            parent.removeChild(this);
        }
        if (this instanceof Lifecycle lifecycle) lifecycle.onDestroy();
    }

    public Widget dx(SmoothedValue value) {
        smoothedValues.remove(dx);
        dx = value;
        if (!dx.bound) {
            smoothedValues.add(dx);
            dx.bound = true;
        }
        return this;
    }

    public Widget dy(SmoothedValue value) {
        smoothedValues.remove(dy);
        dy = value;
        if (!dy.bound) {
            smoothedValues.add(dy);
            dy.bound = true;
        }
        return this;
    }

    public Widget dw(SmoothedValue value) {
        smoothedValues.remove(dw);
        dw = value;
        if (!dw.bound) {
            smoothedValues.add(dw);
            dw.bound = true;
        }
        return this;
    }

    public Widget dh(SmoothedValue value) {
        smoothedValues.remove(dh);
        dh = value;
        if (!dh.bound) {
            smoothedValues.add(dh);
            dh.bound = true;
        }
        return this;
    }

    public Widget mainColor(Color color) {
        originalMainColor = color;
        return this;
    }

    public Widget mainColor(int color) {
        return mainColor(new Color(color));
    }

    public Widget bgColor(Color color) {
        originalBgColor = color;
        return this;
    }

    public Widget bgColor(int color) {
            return bgColor(new Color(color));
    }

    public Widget textColor(Color color) {
        originalTextColor = color;
        return this;
    }

    public Widget testColor(int color) {
            return textColor(new Color(color));
    }

    public Widget color(Color color) {
        return mainColor(color).textColor(color);
    }

    public Widget allColor(Color color) {
        return mainColor(color).textColor(color).bgColor(color);
    }

    public Widget addClickBehavior(Runnable r) {
        clickBehaviors.add(r);
        return this;
    }

    public Widget tooltip(Component tooltip) {
        this.tooltip = tooltip;
        doShowTooltip = tooltip != null;
        return this;
    }

    public int originalX() {
        double x = originalPos.x.apply(parent.w(), parent.h());
        if (align == Align.CENTER) x -= w() / 2.0;
        else if (align == Align.RIGHT) x -= w();
        return (int) Math.round(x);
    }

    public int originalY() {
        double y = originalPos.y.apply(parent.w(), parent.h());
        if (verticalAlign == VerticalAlign.CENTER) y -= h() / 2.0;
        else if (verticalAlign == VerticalAlign.BOTTOM) y -= h();
        return (int) Math.round(y);
    }

    public int originalW() {
        return originalSize.x.apply(parent.w(), parent.h());
    }

    public int originalH() {
        return originalSize.y.apply(parent.w(), parent.h());
    }

    public int getScreenW() {
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            parent = parent.parent;
        }

        return parent.w();
    }
    public int getScreenH() {
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            parent = parent.parent;
        }

        return parent.h();
    }

    public int x() {
        double x = originalPos.x.apply(parent.w(), parent.h()) + dx.getInt() + parent.x();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) x += animation.dx;
        }
        if (align == Align.CENTER) x -= w() / 2.0;
        else if (align == Align.RIGHT) x -= w();
        return (int) Math.round(x);
    }

    public int y() {
        double y = originalPos.y.apply(parent.w(), parent.h()) + dy.getInt() + parent.y();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) y += animation.dy;
        }
        if (verticalAlign == VerticalAlign.CENTER) y -= h() / 2.0;
        else if (verticalAlign == VerticalAlign.BOTTOM) y -= h();
        return (int) Math.round(y);
    }

    public int w() {
        double w = originalSize.x.apply(parent.w(), parent.h()) + dw.getInt();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) w += animation.dw;
        }
        return (int) Math.round(w);
    }

    public int h() {
        double h = originalSize.y.apply(parent.w(), parent.h()) + dh.getInt();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) h += animation.dh;
        }
        return (int) Math.round(h);
    }

    public int menuX() {
        double x = originalPos.x.apply(parent.w(), parent.h()) + dx.getInt() + parent.menuX();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) x += animation.dx;
        }
        if (align == Align.CENTER) x -= w() / 2.0;
        else if (align == Align.RIGHT) x -= w();
        return (int) Math.round(x);
    }

    public int menuY() {
        double y = originalPos.y.apply(parent.w(), parent.h()) + dy.getInt() + parent.menuY();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) y += animation.dy;
        }
        if (verticalAlign == VerticalAlign.CENTER) y -= h() / 2.0;
        else if (verticalAlign == VerticalAlign.BOTTOM) y -= h();
        return (int) Math.round(y);
    }

    public int mainColorInt() {
        double mult = 1;
        Widget p = this;
        while (!(p instanceof Widget.Root)) {
            for (Animation animation : p.animations) {
                if (animation.isActive()) mult *= animation.alphaMultMain;
            }
            p = p.parent;
        }

        Widget colorWidget = this;
        while (colorWidget.originalMainColor == null) colorWidget = colorWidget.parent;
        return colorWidget.originalMainColor.toArgbWithAlphaMult(mult);
    }

    public Color mainColor() {
        return new Color(mainColorInt());
    }

    public int bgColorInt() {
        double mult = 1;
        Widget p = this;
        while (!(p instanceof Widget.Root)) {
            for (Animation animation : p.animations) {
                if (animation.isActive()) mult *= animation.alphaMultBg;
            }
            p = p.parent;
        }

        Widget colorWidget = this;
        while (colorWidget.originalBgColor == null) colorWidget = colorWidget.parent;
        return colorWidget.originalBgColor.toArgbWithAlphaMult(mult);
    }

    public Color bgColor() {
        return new Color(bgColorInt());
    }

    public int textColorInt() {
        double mult = 1;
        Widget p = this;
        while (!(p instanceof Widget.Root)) {
            for (Animation animation : p.animations) {
                if (animation.isActive()) mult *= animation.alphaMultText;
            }
            p = p.parent;
        }

        Widget colorWidget = this;
        while (colorWidget.originalTextColor == null) colorWidget = colorWidget.parent;
        return colorWidget.originalTextColor.toArgbWithAlphaMult(mult);
    }

    public Color textColor() {
        return new Color(textColorInt());
    }

    public int left() {
        return x();
    }

    public int right() {
        return x() + w();
    }

    public int top() {
        return y();
    }

    public int bottom() {
        return y() + h();
    }

    public boolean isInBounds(double x, double y) {
        return x >= left() && right() > x && y >= top() && bottom() > y;
    }

    public boolean isIn(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && x + w > mx && my >= y && y + h > my;
    }

    public static class Root extends Widget {
        public Root() {
            super(Coordinate.fromTopLeft(0, 0), Coordinate.fromTopLeft(0, 0));

            this.originalMainColor = new Color(-1);
            this.originalBgColor = new Color(0, 0, 0, 128);
            this.originalTextColor = new Color(-1);
        }

        @Override
        public int x() {
            return 0;
        }

        @Override
        public int y() {
            return 0;
        }

        @Override
        public int w() {
            return Coordinate.getScreenWidth();
        }

        @Override
        public int h() {
            return Coordinate.getScreenHeight();
        }

        @Override
        public int menuX() {
            return 0;
        }

        @Override
        public int menuY() {
            return 0;
        }
    }

    public static class BlankWidget extends Widget {
        public BlankWidget(Coordinate pos, Coordinate size) {
            super(pos, size);
        }
    }
}
