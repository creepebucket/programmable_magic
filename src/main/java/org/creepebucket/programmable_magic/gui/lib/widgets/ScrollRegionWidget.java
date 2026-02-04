package org.creepebucket.programmable_magic.gui.lib.widgets;

import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;

/**
 * 滚动区域控件：在指定区域内响应鼠标滚轮事件，更新滚动值。
 */
public class ScrollRegionWidget extends Widget implements MouseScrollable {
    public Coordinate region;
    public final int valueMultiplier;
    public SyncedValue<Integer> value;

    public ScrollRegionWidget(Coordinate pos, Coordinate size, Coordinate region, int valueMultiplier, SyncedValue<Integer> value) {
        super(pos, size);
        this.region = region;
        this.value = value;
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
        int delta = scrollY < 0 ? -this.valueMultiplier : (scrollY > 0 ? this.valueMultiplier : 0);
        if (delta == 0) return true;

        // 计算新值并限制在有效范围内
        value.set(Math.clamp(value.get() + delta, region.toScreenX(), region.toScreenY()));
        return true;
    }
}
