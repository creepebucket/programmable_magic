package org.creepebucket.programmable_magic.gui.lib.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * UI 控件基类：提供生命周期、渲染与输入事件的默认空实现。
 */
public abstract class Widget {

    /**
     * 控件加入 UI 运行时时调用。
     */
    public void onInitialize() {}

    /**
     * 控件从 UI 运行时移除时调用。
     */
    public void onRemoved() {}

    /**
     * 屏幕尺寸变化时调用。
     */
    public void onResize(int width, int height) {}

    /**
     * 每 tick 调用一次。
     */
    public void onTick() {}

    /**
     * 渲染控件。
     */
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    /**
     * 鼠标点击事件（无额外来源信息版本）。
     */
    public boolean mouseClicked(MouseButtonEvent event) { return false; }

    /**
     * 鼠标点击事件（可区分是否来自真实鼠标输入）。
     */
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) { return false; }

    /**
     * 鼠标释放事件。
     */
    public boolean mouseReleased(MouseButtonEvent event) { return false; }

    /**
     * 鼠标拖拽事件（事件对象版本）。
     */
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) { return false; }

    /**
     * 鼠标拖拽事件（传统参数版本）。
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }

    /**
     * 鼠标滚轮事件。
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }

    /**
     * 按键按下事件（事件对象版本）。
     */
    public boolean keyPressed(KeyEvent event) { return false; }

    /**
     * 按键释放事件（事件对象版本）。
     */
    public boolean keyReleased(KeyEvent event) { return false; }

    /**
     * 字符输入事件（事件对象版本）。
     */
    public boolean charTyped(CharacterEvent event) { return false; }

    /**
     * 按键按下事件（传统参数版本）。
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    /**
     * 按键释放事件（传统参数版本）。
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }

    /**
     * 字符输入事件（传统参数版本）。
     */
    public boolean charTyped(char codePoint, int modifiers) { return false; }
}
