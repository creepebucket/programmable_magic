package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.creepebucket.programmable_magic.gui.lib.api.DataInstance;
import org.creepebucket.programmable_magic.gui.lib.api.DataManager;
import org.creepebucket.programmable_magic.gui.lib.api.DataType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * UI 的运行时容器：负责数据同步、控件生命周期与事件分发。
 */
public class UiRuntime {

    private final DataManager data = new DataManager();
    private final List<Widget> widgets = new ArrayList<>();

    private UiBounds bounds = new UiBounds(0, 0, 0, 0);

    /**
     * 申请一个带类型与同步模式的数据键，并返回对应的实例句柄。
     */
    public DataInstance data(String key, DataType type, SyncMode syncMode, Object initialValue) {
        return this.data.request(key, type, syncMode, initialValue);
    }

    /**
     * 绑定客户端 -> 服务端的数据发送函数。
     */
    public void bindSendToServer(BiConsumer<String, Object> sender) { this.data.bindSendToServer(sender); }

    /**
     * 绑定服务端 -> 客户端的数据发送函数。
     */
    public void bindSendToClient(BiConsumer<String, Object> sender) { this.data.bindSendToClient(sender); }

    /**
     * 获取当前 UI 的边界与偏移信息。
     */
    public UiBounds bounds() { return this.bounds; }

    /**
     * 设置 UI 的边界与偏移信息。
     */
    public void setBounds(int sw, int sh, int guiLeft, int guiTop) { this.bounds = new UiBounds(sw, sh, guiLeft, guiTop); }

    /**
     * 将暂存的拉取请求一次性发送给对端。
     */
    public void flushPullRequests() { this.data.flushPullRequests(); }

    /**
     * 处理客户端 -> 服务端的键值对同步。
     */
    public boolean handleC2S(String key, Object value) { return this.data.handleC2S(key, value); }

    /**
     * 处理服务端 -> 客户端的键值对同步。
     */
    public boolean handleS2C(String key, Object value) { return this.data.handleS2C(key, value); }

    /**
     * 添加一个控件并触发其初始化回调。
     */
    public void addWidget(Widget widget) {
        this.widgets.add(widget);
        widget.onInitialize();
    }

    /**
     * 移除一个控件并触发其移除回调。
     */
    public void removeWidget(Widget widget) {
        this.widgets.remove(widget);
        widget.onRemoved();
    }

    /**
     * 每 tick 驱动所有控件的逻辑更新。
     */
    public void tick() {
        for (var w : this.widgets) w.onTick();
    }

    /**
     * 渲染所有控件。
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (var w : this.widgets) w.onRender(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * 分发鼠标点击事件；任一控件消费则停止继续分发。
     */
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        for (var w : this.widgets) if (w.mouseClicked(event, fromMouse)) return true;
        return false;
    }

    /**
     * 分发鼠标释放事件；任一控件消费则停止继续分发。
     */
    public boolean mouseReleased(MouseButtonEvent event) {
        for (var w : this.widgets) if (w.mouseReleased(event)) return true;
        return false;
    }

    /**
     * 分发鼠标拖拽事件；任一控件消费则停止继续分发。
     */
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (var w : this.widgets) if (w.mouseDragged(event, dragX, dragY)) return true;
        return false;
    }

    /**
     * 分发鼠标滚轮事件；任一控件消费则停止继续分发。
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (var w : this.widgets) if (w.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return false;
    }

    /**
     * 分发按键按下事件；任一控件消费则停止继续分发。
     */
    public boolean keyPressed(KeyEvent event) {
        for (var w : this.widgets) if (w.keyPressed(event)) return true;
        return false;
    }

    /**
     * 分发按键释放事件；任一控件消费则停止继续分发。
     */
    public boolean keyReleased(KeyEvent event) {
        for (var w : this.widgets) if (w.keyReleased(event)) return true;
        return false;
    }

    /**
     * 分发字符输入事件；任一控件消费则停止继续分发。
     */
    public boolean charTyped(CharacterEvent event) {
        for (var w : this.widgets) if (w.charTyped(event)) return true;
        return false;
    }
}
