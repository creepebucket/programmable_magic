package org.creepebucket.programmable_magic.gui.lib.api.widgets;

/**
 * Tick 接口：实现此接口的 Widget 可以在每个游戏 tick 执行逻辑。
 */
public interface Tickable {
    /**
     * 每 tick 调用一次。
     */
    void tick();
}
