package org.creepebucket.programmable_magic.gui.lib.widgets;

import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;

/**
 * 滚动区域控件：在指定区域内响应鼠标滚轮事件，更新滚动值。
 */
public class ScrollRegionWidget extends Widget implements MouseScrollable {
    /**
     * 最大滚动值
     */
    public final int maxValue;
    /**
     * 滚动值倍数
     */
    public final int valueMultiplier;
    /**
     * 当前滚动值
     */
    public int currentValue;

    public ScrollRegionWidget(Coordinate pos, Coordinate size, int currentValue, int maxValue, int valueMultiplier) {
        super(pos, size);
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.valueMultiplier = valueMultiplier;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 检测鼠标是否在滚动区域内
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        int w = this.size.toScreenX();
        int h = this.size.toScreenY();
        if (!isInBounds(mouseX, mouseY, x, y, w, h)) return false;

        // 计算滚动方向：向上滚动减少值，向下滚动增加值
        int delta = scrollY > 0 ? -this.valueMultiplier : (scrollY < 0 ? this.valueMultiplier : 0);
        if (delta == 0) return true;

        // 计算新值并限制在有效范围内
        int next = this.currentValue + delta;
        if (next < 0) next = 0;
        if (next > this.maxValue) next = this.maxValue;
        if (next == this.currentValue) return true;

        this.currentValue = next;
        return true;
    }
}
