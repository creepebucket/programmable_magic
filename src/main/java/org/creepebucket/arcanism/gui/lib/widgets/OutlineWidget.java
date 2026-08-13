package org.creepebucket.arcanism.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.Widget;
import org.creepebucket.arcanism.gui.lib.api.widgets.Renderable;

public class OutlineWidget extends Widget implements Renderable {
    public OutlineWidget(Coordinate pos, Coordinate size) {
        super(pos, size);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.outline(x(), y(), w(), h(), mainColorInt());
    }
}
