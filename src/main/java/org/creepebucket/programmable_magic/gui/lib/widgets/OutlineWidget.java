package org.creepebucket.programmable_magic.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphics;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;

public class OutlineWidget extends Widget implements Renderable {

    public int color;

    public OutlineWidget(Coordinate pos, Coordinate size, int color) {
        this.pos = pos;
        this.size = size;
        this.color = color;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.renderOutline(pos.toScreenX(), pos.toScreenY(), size.toScreenX(), size.toScreenY(), color);
    }
}
