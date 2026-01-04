package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.api.SlotManipulationScreen;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvPacket;

/**
 * UI 屏幕基类：将 Minecraft 的输入/渲染生命周期桥接到 {@link UiRuntime}。
 */
public abstract class UiScreenBase<Menu extends UiMenuBase> extends SlotManipulationScreen<Menu> {

    /**
     * 创建 UI 屏幕。
     */
    public UiScreenBase(Menu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    /**
     * 初始化界面并绑定 c2s 发送通道，同时刷新一次拉取请求。
     */
    @Override
    protected void init() {
        super.init();

        updateUiBounds();

        this.menu.ui().bindSendToServer((k, v) -> {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SimpleKvPacket(k, v)));
        });

        this.menu.ui().flushPullRequests();
    }

    /**
     * 处理窗口缩放：同步更新 UI 边界信息。
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        updateUiBounds();
    }

    /**
     * 每 tick 驱动 UI 运行时更新。
     */
    @Override
    protected void containerTick() {
        super.containerTick();
        this.menu.ui().tick();
    }

    /**
     * 渲染原生容器界面后，再渲染 UI 运行时控件。
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.menu.ui().render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * 该基类不绘制背景；具体背景交由子类或控件自行实现。
     */
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    }

    /**
     * 优先交由 UI 运行时处理鼠标点击事件。
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        return this.menu.ui().mouseClicked(event, fromMouse) || super.mouseClicked(event, fromMouse);
    }

    /**
     * 优先交由 UI 运行时处理鼠标释放事件。
     */
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return this.menu.ui().mouseReleased(event) || super.mouseReleased(event);
    }

    /**
     * 优先交由 UI 运行时处理鼠标拖拽事件。
     */
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return this.menu.ui().mouseDragged(event, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
    }

    /**
     * 优先交由 UI 运行时处理滚轮事件。
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.menu.ui().mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 优先交由 UI 运行时处理按键按下事件。
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.menu.ui().keyPressed(event) || super.keyPressed(event);
    }

    /**
     * 优先交由 UI 运行时处理按键释放事件。
     */
    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.menu.ui().keyReleased(event) || super.keyReleased(event);
    }

    /**
     * 优先交由 UI 运行时处理字符输入事件。
     */
    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.menu.ui().charTyped(event) || super.charTyped(event);
    }

    /**
     * 将当前窗口尺寸与 GUI 偏移同步到 {@link UiRuntime}。
     */
    private void updateUiBounds() {
        var window = Minecraft.getInstance().getWindow();
        this.menu.ui().setBounds(window.getGuiScaledWidth(), window.getGuiScaledHeight(), this.leftPos, this.topPos);
    }
}
