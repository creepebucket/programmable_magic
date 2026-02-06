package org.creepebucket.programmable_magic.gui.lib.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.ClientSlotManager;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SlotManipulationScreen;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.*;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.network.dataPackets.HookTriggerPacket;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvPacket;

public class Screen<M extends Menu> extends SlotManipulationScreen<M> {

    private float lastPartialTick = 0.0F;

    public Screen(M menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {

        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Lifecycle lifecycle) {
                lifecycle.onRemoved();
            }
        }
        this.menu.widgets.clear();

        this.imageWidth = this.width;
        this.imageHeight = this.height;
        super.init();

        // 1. 把屏幕位置同步给 Menu
        this.menu.guiLeft = this.leftPos;
        this.menu.guiTop = this.topPos;
        Coordinate.updateContext(this.width, this.height, this.leftPos, this.topPos);
        ClientUiContext.setFont(this.font);

        // 2. 绑定发包
        this.menu.dataManager.bindServerSender((key, value) -> {
            var packet = new ServerboundCustomPayloadPacket(new SimpleKvPacket(key, value));
            Minecraft.getInstance().getConnection().send(packet);
        });
        this.menu.dataManager.flushPullRequests();

        this.menu.hooks.bindServerSender((hookId, args) -> {
            var packet = new ServerboundCustomPayloadPacket(new HookTriggerPacket(hookId, args));
            Minecraft.getInstance().getConnection().send(packet);
        });

        // 3. 关键点：调用 Menu 的 reportScreenSize
        // 这会触发 Menu 里所有控件的 onInitialize()，让它们根据新的 guiLeft/Top 计算位置
        this.menu.reportScreenSize(this.width, this.height);
    }

    @Override
    public void resize(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        super.resize(width, height);

        // 窗口大小变了，更新 Menu 变量并通知控件重算位置
        this.menu.guiLeft = this.leftPos;
        this.menu.guiTop = this.topPos;
        Coordinate.updateContext(width, height, this.leftPos, this.topPos);
        ClientUiContext.setFont(this.font);
        this.menu.reportScreenSize(width, height);
    }

    @Override
    public void removed() {
        // 界面关闭时，通知 Menu 里的控件被移除了
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Lifecycle lifecycle) {
                lifecycle.onRemoved();
            }
        }
        super.removed();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // 遍历 menu.widgets 进行逻辑更新
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Tickable tickable) {
                tickable.tick();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastPartialTick = partialTick;
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.nextStratum();
        for (Widget widget : this.menu.widgets)
            if (widget instanceof ImageButtonWidget imageButtonWidget)
                imageButtonWidget.render(graphics, mouseX, mouseY, partialTick);

        for (int i = this.menu.widgets.size() - 1; i >= 0; i--) {
            Widget widget = this.menu.widgets.get(i);
            if (widget instanceof Tooltipable tooltipable) {
                if (tooltipable.renderTooltip(graphics, mouseX, mouseY)) return;
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ClientSlotManager.clearAll();
        // 遍历 menu.widgets 进行渲染
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Renderable renderable && !(widget instanceof ImageButtonWidget)) {
                renderable.render(graphics, mouseX, mouseY, this.lastPartialTick);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    // --- 下面全是把输入事件转发给 menu.widgets ---

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Clickable clickable) {
                if (clickable.mouseClicked(event, fromMouse)) return true;
            }
        }
        return super.mouseClicked(event, fromMouse);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof Clickable clickable) {
                if (clickable.mouseReleased(event)) return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof MouseDraggable draggable) {
                if (draggable.mouseDragged(event, dragX, dragY)) return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof MouseScrollable scrollable) {
                if (scrollable.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof KeyInputable inputable) {
                if (inputable.keyPressed(event)) return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof KeyInputable inputable) {
                if (inputable.keyReleased(event)) return true;
            }
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Widget widget : this.menu.widgets) {
            if (widget instanceof KeyInputable inputable) {
                if (inputable.charTyped(event)) return true;
            }
        }
        return super.charTyped(event);
    }

    public void addWidget(Widget widget) {
        widget.screen = this;
        this.menu.widgets.add(widget);
    }
}
