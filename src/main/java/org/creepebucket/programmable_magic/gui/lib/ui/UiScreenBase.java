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

public abstract class UiScreenBase<Menu extends UiMenuBase> extends SlotManipulationScreen<Menu> {

    public UiScreenBase(Menu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();
        this.menu.ui().setBounds(sw, sh, this.leftPos, this.topPos);

        this.menu.ui().bindSendToServer((k, v) -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) connection.send(new ServerboundCustomPayloadPacket(new SimpleKvPacket(k, v)));
        });

        this.menu.ui().flushPullRequests();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();
        this.menu.ui().setBounds(sw, sh, this.leftPos, this.topPos);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.menu.ui().tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.menu.ui().render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        if (this.menu.ui().mouseClicked(event, fromMouse)) return true;
        return super.mouseClicked(event, fromMouse);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.menu.ui().mouseReleased(event)) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.menu.ui().mouseDragged(event, dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.menu.ui().mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.menu.ui().keyPressed(event)) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (this.menu.ui().keyReleased(event)) return true;
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.menu.ui().charTyped(event)) return true;
        return super.charTyped(event);
    }
}
