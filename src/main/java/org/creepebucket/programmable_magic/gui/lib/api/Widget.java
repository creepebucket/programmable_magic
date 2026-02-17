package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.gui.GuiGraphics;
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
    public Coordinate originalPos, originalSize;
    public SmoothedValue dx = new SmoothedValue(0), dy = new SmoothedValue(0), dw = new SmoothedValue(0), dh = new SmoothedValue(0);
    public Color originalMainColor = new Color(-1), originalBgColor = new Color(-2147483647), originalTextColor = new Color(-1);
    public Component tooltip;
    public boolean doShowTooltip = false, renderInForeground = false;
    public List<Widget> children = new ArrayList<>();
    public Widget parent;
    public Screen<? extends Menu> screen;
    public List<Animation> animations = new ArrayList<>();
    public List<SmoothedValue> smoothedValues = new ArrayList<>();

    public Widget(Coordinate pos, Coordinate size) {
        this.originalPos = pos;
        this.originalSize = size;

        smoothedValues.addAll(List.of(dx, dy, dw, dh));
    }

    public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick, double dt, boolean isForeground) {
        if (isForeground == renderInForeground) {

            // 动画的step
            for (Animation animation : animations) animation.step(dt);

            // 自己的渲染逻辑
            if (this instanceof Renderable renderable) {
                renderable.render(graphics, mx, my, partialTick);
            }

            // 检查动画过期状态
            var expiredAnimations = animations.stream().filter(Animation::isExpired).toList();

            for (Animation animation : animations) {
                if (animation.isExpired()) {
                    animation.onExpire(this);
                }
            }

            for (Animation animation : expiredAnimations) {
                animations.remove(animation);
            }

            // 平滑逻辑
            for (SmoothedValue value : smoothedValues) value.doStep(dt);
        }

        // 孩子的渲染逻辑
        for (Widget child : List.copyOf(children)) child.renderWidget(graphics, mx, my, partialTick, dt, isForeground);
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
        List<Widget> list = new ArrayList<>(children);
        for (Widget widget : children) {
            list.addAll(widget.allChild());
        }
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

    public Widget bgColor(Color color) {
        originalBgColor = color;
        return this;
    }

    public Widget textColor(Color color) {
        originalTextColor = color;
        return this;
    }

    public Widget color(Color color) {
        return mainColor(color).textColor(color);
    }

    public Widget allColor(Color color) {
        return mainColor(color).textColor(color).bgColor(color);
    }

    public Widget tooltip(Component tooltip) {
        this.tooltip = tooltip;
        doShowTooltip = true;
        return this;
    }

    public int x() {
        double x = originalPos.toScreenX() + dx.getInt() + parent.x();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) x += animation.dx;
        }
        return (int) Math.round(x);
    }

    public int y() {
        double y = originalPos.toScreenY() + dy.getInt() + parent.y();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) y += animation.dy;
        }
        return (int) Math.round(y);
    }

    public int w() {
        double w = originalSize.toScreenX() + dw.getInt();
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
        double h = originalSize.toScreenY() + dh.getInt();
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
        double x = originalPos.toScreenX() + dx.getInt() + parent.menuX();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) x += animation.dx;
        }
        return (int) Math.round(x);
    }

    public int menuY() {
        double y = originalPos.toScreenY() + dy.getInt() + parent.menuY();
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) {
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) y += animation.dy;
        }
        return (int) Math.round(y);
    }

    public int mainColor() {
        double mult = 1;
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) { // 统计自根以来的alphaMult
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) mult *= animation.alphaMultMain;
        }
        return originalMainColor.toArgbWithAlphaMult(mult);
    }

    public int bgColor() {
        double mult = 1;
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) { // 统计自根以来的alphaMult
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) mult *= animation.alphaMultBg;
        }
        return originalBgColor.toArgbWithAlphaMult(mult);
    }

    public int textColor() {
        double mult = 1;
        var allAnimations = new ArrayList<Animation>();
        var parent = this;

        while (!(parent instanceof Widget.Root)) { // 统计自根以来的alphaMult
            allAnimations.addAll(parent.animations);
            parent = parent.parent;
        }

        for (Animation animation : allAnimations) {
            if (animation.isActive()) mult *= animation.alphaMultText;
        }
        return originalTextColor.toArgbWithAlphaMult(mult);
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
            return 0;
        }

        @Override
        public int h() {
            return 0;
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
