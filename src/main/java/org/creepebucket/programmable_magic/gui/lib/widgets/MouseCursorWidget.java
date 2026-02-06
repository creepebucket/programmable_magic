package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

import static net.minecraft.util.Mth.hsvToRgb;

public class MouseCursorWidget extends Widget implements Renderable {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 傻逼ubuntu录屏不能在mc录鼠标指针, 只好自己写一个
        graphics.renderOutline(mouseX - 2, mouseY - 2, 5, 5, hsvToRgb((Minecraft.getInstance().level.getGameTime() * 0.03f) % 1, 1, 1) | 0xFF000000);
    }
}
