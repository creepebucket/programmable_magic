package org.creepebucket.programmable_magic.gui.lib.api.widgets;

import net.minecraft.client.input.MouseButtonEvent;

/**
 * 点击接口：实现此接口的 Widget 可以响应鼠标点击和释放事件。
 */
public interface Clickable {
    /**
     * 鼠标点击事件。
     * @return true 表示事件已被消费
     */
    default boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        return false;
    }

    /**
     * 鼠标释放事件。
     * @return true 表示事件已被消费
     */
    default boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }
}
