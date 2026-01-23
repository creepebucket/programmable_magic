package org.creepebucket.programmable_magic.gui.lib.api;

/**
 * UI 控件基类：空壳基类，通过实现功能接口来获得能力。
 */
public abstract class Widget {

    /**
     * 判断鼠标坐标是否在指定矩形区域内。
     */
    protected static boolean isInBounds(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
