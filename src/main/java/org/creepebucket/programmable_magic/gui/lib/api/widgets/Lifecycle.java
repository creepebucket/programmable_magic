package org.creepebucket.programmable_magic.gui.lib.api.widgets;

/**
 * 生命周期接口：实现此接口的 Widget 可以响应生命周期事件。
 */
public interface Lifecycle {
    /**
     * 控件加入 UI 运行时时调用。
     */
    default void onInitialize() {
    }

    /**
     * 控件从 UI 运行时移除时调用。
     */
    default void onDestroy() {
    }

    /**
     * 屏幕尺寸变化时调用。
     */
    default void onResize(int width, int height) {
    }
}
