package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class RectangleWidget extends Widget implements Renderable {
    public int color;

    public RectangleWidget(Coordinate pos, Coordinate size, int color) {
        this.pos = pos;
        this.size = size;
        this.color = color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(), color);
    }
}
