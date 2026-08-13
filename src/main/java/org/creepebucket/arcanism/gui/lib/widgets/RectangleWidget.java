package org.creepebucket.arcanism.gui.lib.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.creepebucket.arcanism.gui.lib.api.Coordinate;
import org.creepebucket.arcanism.gui.lib.api.Widget;
import org.creepebucket.arcanism.gui.lib.api.widgets.Renderable;

public class RectangleWidget extends Widget implements Renderable {

    public RectangleWidget(Coordinate pos, Coordinate size) {
        super(pos, size);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left(), top(), right(), bottom(), mainColorInt());
    }
}
