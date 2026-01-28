package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;

public class wandWidgetClient {
    public static class WandSubcategoryJumpButton extends ImageButtonWidget {
        public SyncedValue<Integer> deltaY;
        public int target, color;

        public WandSubcategoryJumpButton(Coordinate pos, Coordinate size, SyncedValue<Integer> deltaY, int target, Component tooltip, int color) {
            super(pos, size, null, null, () -> {
            }, tooltip);
            this.deltaY = deltaY;
            this.target = target;
            this.color = color;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (contains(mouseX, mouseY)) {
                graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(), color);
            } else {
                graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(),
                        (color & 16777215) | ((int) (((color >>> 24) * 0.6)) << 24));
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            // 检测点击是否在按钮范围内
            if (!contains(event.x(), event.y())) return false;
            this.deltaY.set(target);
            return true;
        }
    }
}
