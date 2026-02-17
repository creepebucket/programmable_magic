package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.hsvToRgb;

public class MouseCursorWidget extends Widget implements Renderable, Clickable {
    public List<String> mb = List.of("LMB", "RMB", "MMB");
    public List<Boolean> downs = new ArrayList<>(List.of(false, false, false));

    public MouseCursorWidget() {
        super(Coordinate.ZERO, Coordinate.ZERO);
        renderInForeground = true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 傻逼ubuntu录屏不能在mc录鼠标指针, 只好自己写一个
        var color = hsvToRgb((Minecraft.getInstance().level.getGameTime() * 0.03f) % 1, 1, 1) | 0xFF000000;
        graphics.renderOutline(mouseX - 2, mouseY - 2, 5, 5, color);

        int dy = 0;
        if (downs.get(0)) {
            graphics.drawString(ClientUiContext.getFont(), Component.literal(mb.get(0)), mouseX + 5, mouseY + dy, color);
            dy += 9;
        }
        if (downs.get(1)) {
            graphics.drawString(ClientUiContext.getFont(), Component.literal(mb.get(1)), mouseX + 5, mouseY + dy, color);
            dy += 9;
        }
        if (downs.get(2)) {
            graphics.drawString(ClientUiContext.getFont(), Component.literal(mb.get(2)), mouseX + 5, mouseY + dy, color);
            dy += 9;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
        downs.set(event.button(), true);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        downs.set(event.button(), false);
        return false;
    }
}
