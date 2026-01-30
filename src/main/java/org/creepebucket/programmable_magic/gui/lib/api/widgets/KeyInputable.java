package org.creepebucket.programmable_magic.gui.lib.api.widgets;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * 键盘输入接口：实现此接口的 Widget 可以响应键盘输入事件。
 */
public interface KeyInputable {
    /**
     * 按键按下事件。
     *
     * @return true 表示事件已被消费
     */
    default boolean keyPressed(KeyEvent event) {
        return false;
    }

    /**
     * 按键释放事件。
     *
     * @return true 表示事件已被消费
     */
    default boolean keyReleased(KeyEvent event) {
        return false;
    }

    /**
     * 字符输入事件。
     *
     * @return true 表示事件已被消费
     */
    default boolean charTyped(CharacterEvent event) {
        return false;
    }
}
