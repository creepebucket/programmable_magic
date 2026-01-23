package org.creepebucket.programmable_magic.gui.lib.widgets;

import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * 滚动区域控件：在指定区域内响应鼠标滚轮事件，更新滚动值。
 */
public class ScrollRegionWidget extends Widget implements MouseScrollable {
    /** 区域左上角坐标 */
    private final Coordinate pos;
    /** 区域宽度提供器 */
    private final IntSupplier width;
    /** 区域高度提供器 */
    private final IntSupplier height;
    /** 当前滚动值获取器 */
    private final IntSupplier getValue;
    /** 最大滚动值获取器 */
    private final IntSupplier maxValue;
    /** 滚动值设置器 */
    private final IntConsumer setValue;

    public ScrollRegionWidget(Coordinate pos, IntSupplier width, IntSupplier height, IntSupplier getValue, IntSupplier maxValue, IntConsumer setValue) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.getValue = getValue;
        this.maxValue = maxValue;
        this.setValue = setValue;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 检测鼠标是否在滚动区域内
        int x = this.pos.toScreenX();
        int y = this.pos.toScreenY();
        int w = this.width.getAsInt();
        int h = this.height.getAsInt();
        if (!isInBounds(mouseX, mouseY, x, y, w, h)) return false;

        // 计算滚动方向：向上滚动减少值，向下滚动增加值
        int delta = scrollY > 0 ? -1 : (scrollY < 0 ? 1 : 0);
        if (delta == 0) return true;

        // 计算新值并限制在有效范围内
        int current = this.getValue.getAsInt();
        int max = Math.max(0, this.maxValue.getAsInt());
        int next = Math.max(0, Math.min(max, current + delta));
        if (next == current) return true;

        this.setValue.accept(next);
        return true;
    }
}
