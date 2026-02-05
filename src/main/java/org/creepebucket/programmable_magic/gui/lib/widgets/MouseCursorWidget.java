package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class MouseCursorWidget extends Widget implements Renderable {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 傻逼ubuntu录屏不能在mc录鼠标指针, 只好自己写一个
        graphics.renderOutline(mouseX - 2, mouseY - 2, 5, 5, hsvToRgb((Minecraft.getInstance().level.getGameTime() * 3) % 360, 1, 1));
    }

    public static int hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        // 确保色相在 0-360 之间
        float h60 = h / 60.0f;
        int i = (int) Math.floor(h60);
        float f = h60 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }
        // 将 0-1 的浮点数转换为 0-255 的整数并合并
        int red = Math.round(r * 255);
        int green = Math.round(g * 255);
        int blue = Math.round(b * 255);
        // 合并为 0xRRGGBB 格式
        return (red << 16) | (green << 8) | blue | 0xFF000000;
    }
}
