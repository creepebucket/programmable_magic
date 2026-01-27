package org.creepebucket.programmable_magic.gui.lib.api.widgets;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Tooltip 接口：实现此接口的 Widget 可以显示 tooltip。
 */
public interface Tooltipable {
    /**
     * 渲染 tooltip。
     * @return true 表示 tooltip 已被渲染（将阻止 Screen 渲染默认 tooltip）
     */
    boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY);
}
