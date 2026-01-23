package org.creepebucket.programmable_magic.gui.lib.api.widgets;

import net.minecraft.client.input.MouseButtonEvent;

/**
 * 鼠标拖拽接口：实现此接口的 Widget 可以响应鼠标拖拽事件。
 */
public interface MouseDraggable {
    /**
     * 鼠标拖拽事件。
     * @return true 表示事件已被消费
     */
    boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY);
}
