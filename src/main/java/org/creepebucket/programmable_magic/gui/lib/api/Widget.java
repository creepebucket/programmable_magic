package org.creepebucket.programmable_magic.gui.lib.api;

import org.creepebucket.programmable_magic.gui.lib.ui.Screen;

/**
 * UI 控件基类：提供位置和尺寸的基础支持，通过实现功能接口来获得能力。
 */
public abstract class Widget {
    /**
     * 控件位置
     */
    protected Coordinate pos;
    /** 控件宽度 */
    /**
     * 控件高度
     */
    protected Coordinate size;

    /**
     * 默认构造函数。
     */
    protected Widget() {
    }

    /**
     * 带位置的构造函数。
     */
    protected Widget(Coordinate pos) {
        this.pos = pos;
    }

    public Screen<?> screen;

    /**
     * 带位置和尺寸的构造函数。
     */
    protected Widget(Coordinate pos, Coordinate size) {
        this.pos = pos;
        this.size = size;
    }

    /**
     * 判断鼠标坐标是否在指定矩形区域内（静态工具方法）。
     */
    protected static boolean isInBounds(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /**
     * 获取控件位置。
     */
    public Coordinate getPos() {
        return this.pos;
    }

    /**
     * 获取控件宽度。
     */
    public int getWidth() {
        return this.size.toScreenX();
    }

    /**
     * 获取控件高度。
     */
    public int getHeight() {
        return this.size.toScreenY();
    }

    /**
     * 判断鼠标坐标是否在当前控件范围内（实例方法）。
     */
    public boolean contains(double mx, double my) {
        if (this.pos == null) return false;
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();
        return isInBounds(mx, my, this.pos.toScreenX(), this.pos.toScreenY(), w, h);
    }
}
