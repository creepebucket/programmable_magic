package org.creepebucket.programmable_magic.gui.lib.api.widgets;

/**
 * 鼠标滚轮接口：实现此接口的 Widget 可以响应鼠标滚轮事件。
 */
public interface MouseScrollable {
    /**
     * 鼠标滚轮事件。
     * @return true 表示事件已被消费
     */
    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);
}
