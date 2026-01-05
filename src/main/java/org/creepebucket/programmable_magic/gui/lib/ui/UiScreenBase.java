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
public class UiScreenBase<Menu extends UiMenuBase> extends SlotManipulationScreen<Menu> {
    private float lastPartialTick = 0.0F;

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
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        super.init();

        updateUiBounds();
        reportScreenSize();

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
        this.imageWidth = width;
        this.imageHeight = height;
        super.resize(width, height);
        updateUiBounds();
        reportScreenSize();
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
     * 渲染原生容器界面后，再渲染 UI 前景控件。
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastPartialTick = partialTick;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * 在标签层（renderLabels）渲染 UI 前景控件，确保其处于原生控件之后、tooltip 之前。
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.menu.ui().renderForeground(graphics, mouseX, mouseY, this.lastPartialTick);
    }

    /**
     * 默认在背景层渲染 UI 背景控件。
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

    private void reportScreenSize() {
        var window = Minecraft.getInstance().getWindow();
        this.menu.reportScreenSize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { }
}
