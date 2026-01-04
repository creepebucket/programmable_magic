package org.creepebucket.programmable_magic.gui.lib.ui;

/**
 * UI 定义入口：用于在菜单/界面创建时构建控件树与数据绑定。
 */
public interface AbstractUi {

    /**
     * 将 UI 定义构建到指定的运行时环境中。
     */
    void build(UiRuntime ui);
}
