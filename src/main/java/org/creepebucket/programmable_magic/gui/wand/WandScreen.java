package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.ui.UiScreenBase;

public class WandScreen extends UiScreenBase<WandMenu> {
    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        super.init();
        reportScreenSize();
        this.menu.rebuildUi();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        reportScreenSize();
        this.menu.rebuildUi();
    }

    private void reportScreenSize() {
        var win = Minecraft.getInstance().getWindow();
        sendMenuData(WandMenu.KEY_SCREEN_WIDTH, win.getGuiScaledWidth());
        sendMenuData(WandMenu.KEY_SCREEN_HEIGHT, win.getGuiScaledHeight());
    }

    public void sendMenuData(String key, Object value) {
        this.menu.sendMenuData(key, value);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { }
}
